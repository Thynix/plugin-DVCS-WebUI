package org.freenetproject.plugin.dvcs_webui.ui.fcp;

import freenet.crypt.RandomSource;
import freenet.pluginmanager.FredPluginFCP;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginReplySender;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.messages.MessageHandler;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.messages.Ping;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.messages.QueryResult;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.messages.Ready;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.messages.Unknown;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Tracks session state and dispatches messages to specific handlers.
 * <p/>
 * The UI event code currently handles a single event at a time This is simpler to get things started, but will be slow.
 * Asynchronous code is a good place to look for initial performance improvements.
 */
public class FCPHandler implements FredPluginFCP {
	/**
	 * Seconds before an FCP connection to Infocalypse is considered timed out.
	 */
	public static final long fcpTimeout = 5;

	private final ScheduledThreadPoolExecutor executor;
	private Future timeout;

	/**
	 * Contains a single permit which is taken by a client when it connects and
	 * released when it disconnects or times out.
	 */
	private final Semaphore connected;
	/**
	 * Token given to the connected DVCS instance.
	 */
	private String sessionToken;

	// TODO: Is this an appropriate place to keep a query list?
	private final BlockingQueue<Query> queries;

	private final HashMap<String, MessageHandler> handlers;
	private final HashMap<String, QueryResult> resultHandlers;

	private final RandomSource random;

	public FCPHandler(RandomSource randomSource) {
		executor = new ScheduledThreadPoolExecutor(1);
		queries = new LinkedBlockingQueue<Query>();
		connected = new Semaphore(1);
		sessionToken = null;
		random = randomSource;

		handlers = new HashMap<String, MessageHandler>();
		handlers.put("Ping", new Ping());
		handlers.put("Ready", new Ready(this));

		resultHandlers = new HashMap<String, QueryResult>();
		// TODO: Require that all query names end with "Query" and result names with "Result"?
		for (String resultType : new String[] {"VoidResult", "LocalRepoResult"}) {
			resultHandlers.put(resultType, new QueryResult());
		}

		// Result handlers are a subset of handlers. They are separate to allow registering listeners.
		handlers.putAll(resultHandlers);
	}

	public boolean isConnected() {
		/*
		 * sessionToken is null <--> nothing is connected.
		 * sessionToken is not null <--> something is connected.
		 */
		assert (sessionToken == null && connected.availablePermits() == 1) ||
		       (sessionToken != null && connected.availablePermits() == 0);
		return sessionToken != null;
	}

	@Override
	public void handle(PluginReplySender replySender, SimpleFieldSet params, Bucket data, int accessType) {
		// TODO: What to do with accessType?
		try {
			SimpleFieldSet sfs = getResponse(params);
			replySender.send(sfs);
		} catch (PluginNotFoundException e) {
			System.err.println("Cannot find plugin / connection closed: " + e);
		}
	}

	/**
	 * Internal response handler. Control flow should be easier to follow than otherwise because of the ability to
	 * use return statements.
	 *
	 * @param params received message.
	 * @return response to the message.
	 */
	private SimpleFieldSet getResponse(SimpleFieldSet params) {
		final String message = params.get("Message");
		SimpleFieldSet response = new SimpleFieldSet(true);

		// Something is trying to connect.
		if (message.equals("Hello")) {
			if (connected.tryAcquire()) {
				// If the semaphore is releases the session token should be unset. See disconnect().
				assert sessionToken == null;

				// TODO: Check supported queries. Give an error if not enough are supported?
				// TODO: Also check version number.
				response.putOverwrite("Message", "HiThere");

				sessionToken = Long.toString(random.nextLong());
				response.putOverwrite("SessionToken", sessionToken);

				startTimeout();
			} else {
				response.putOverwrite("Message", "Error");
				response.putOverwrite("Description", "Another DVCS is already connected.");
			}
			return response;
		}

		// The message is not Hello, yet nothing is connected.
		if (sessionToken == null) {
			assert connected.availablePermits() == 1;
			response.putOverwrite("Message", "Error");
			response.putOverwrite("Description", "Hello must be the first message. Nothing is connected currently.");
			return response;
		}

		// The message is something sent after "Hello", so make sure the session token matches.
		if (!sessionToken.equals(params.get("SessionToken"))) {
			response.putOverwrite("Message", "Error");

			if (params.get("SessionToken") == null) {
				response.putOverwrite("Description", "Missing SessionToken");
			} else {
				response.putOverwrite("Description", "Incorrect SessionToken");
			}

			return response;
		}

		if (message.equals("Disconnect")) {
			disconnect();
			response.putOverwrite("Message", "Bye");
			return response;
		}

		if (!message.equals("Ping")) {
			System.err.println(params);
		}

		// A message with the connected token was received - restart the timeout.
		timeout.cancel(true);
		startTimeout();

		// TODO: Is this appropriate for query replies, though? Sure, why not. Must sort out by queryIdentifier.
		// Find a handler or reply with an "Unrecognized message" error.
		if (handlers.containsKey(message)) {
			return handlers.get(message).reply(params);
		} else {
			return new Unknown().reply(params);
		}
	}

	public void registerListener(ResultListener listener, String queryIdentifier) {
		resultHandlers.get(listener.handles()).register(queryIdentifier, listener);
	}

	public void pushQuery(Query query) {
		queries.add(query);
	}

	/**
	 * Block until there is something.
	 * @return the next query in the queue.
	 */
	public Query takeQuery() {
		Query query = null;

		do {
			try {
				query = queries.take();
			} catch (InterruptedException e) {
			}
			// Check because take() might have been interrupted. Note that a BlockingQueue does not accept null values.
		} while (query == null);

		return query;
	}

	private void startTimeout() {
		timeout = executor.schedule(new Runnable() {
			@Override
			public void run() {
				disconnect();
			}
		}, fcpTimeout, TimeUnit.SECONDS);
	}

	private void disconnect() {
		// TODO: Reset application state. (In pages.)
		queries.clear();

		// The disconnect might be client-prompted, in which case there should not be a timeout.
		// TODO: Is locking needed here to prevent race conditions?
		timeout.cancel(false);

		sessionToken = null;

		connected.release();
		System.out.printf("%s: Disconnected.\n", new Date());
	}
}
