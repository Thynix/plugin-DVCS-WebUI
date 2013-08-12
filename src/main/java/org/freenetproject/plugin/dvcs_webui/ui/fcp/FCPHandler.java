package org.freenetproject.plugin.dvcs_webui.ui.fcp;

import freenet.pluginmanager.FredPluginFCP;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginReplySender;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.messages.MessageHandler;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.messages.Ping;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.messages.Ready;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.messages.Unknown;

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
 * The UI event code currently handles a single event at a time, and treats more than one simultaneous ClearToSend
 * as an error. This is simpler to get things started, but is likely to be slow. Asynchronous code is a good place to
 * look for initial performance improvements.
 */
public class FCPHandler implements FredPluginFCP {

    /*
     * TODO: Where to keep state, like which sessions are still considered connected? Hm, that should be any message.
     * Still, where to keep state when it is needed? Per message type doesn't seem to make sense. It depends on the
     * type of state and where it's needed, so perhaps that remains to be seen when actual functionality comes into
     * the fold.
     *
     * Is connectedness only a UI thing? It would make sense if any message causes reconnection,
     * and there isn't yet an apparent reason why a session would carry its own state.
     *
     * So Infocalypse sends Ping, which merits no response, and ClearToSend, which replies with whatever user request
     * from the UI?
     *
     * TODO: Does it make sense to only allow one session at once? Is there a less ugly way to implement a timeout?
     */

	/**
	 * Seconds before an FCP connection to Infocalypse is considered timed out.
	 */
	public static final long fcpTimeout = 5;

	private final ScheduledThreadPoolExecutor executor;
	private Future timeout;

	// TODO: Timeout or Disconnect releases.
	// TODO: How best to track session state?
	private final Semaphore connected;
	// TODO: Is this an appropriate place to keep a query list?
	private final BlockingQueue<InfocalypseQuery> queries;

	private final HashMap<String, MessageHandler> handlers;

	public FCPHandler() {
		executor = new ScheduledThreadPoolExecutor(1);
		queries = new LinkedBlockingQueue<InfocalypseQuery>();
		connected = new Semaphore(1);

		handlers = new HashMap<String, MessageHandler>();
		handlers.put("Ping", new Ping());
		handlers.put("Ready", new Ready(this));
	}

	@Override
	public void handle(PluginReplySender replySender, SimpleFieldSet params, Bucket data, int accessType) {
		// TODO: What to do with accessType?
		// TODO: Switching on strings would be nice given Java 7 or up. Also consider a map of pre-constructed handler
		// classes or reflection like LCWoT's FCP interface.
		// TODO: Will the flow resulting from having the response out here be desirable?
		final String message = params.get("Message");
		SimpleFieldSet response = new SimpleFieldSet(true);
		if (message.equals("Hello")) {
			if (connected.tryAcquire()) {
				// TODO: Check supported queries. Probably should be an error if not enough are supported?
				response.putOverwrite("Message", "HiThere");
				startTimeout();
			} else {
				response.putOverwrite("Message", "Error");
				response.putOverwrite("Description", "Another DVCS is already connected.");
			}
		} else {
			// A message was received - restart the timeout.
			timeout.cancel(true);
			startTimeout();

			// TODO: Is this appropriate for query replies, though? Sure, why not. Must sort out by identifier.
			if (handlers.containsKey(message)) {
				response = handlers.get(message).reply(params);
			} else {
				response = new Unknown().reply(params);
			}
		}

		try {
			replySender.send(response);
		} catch (PluginNotFoundException e) {
			System.err.println("Cannot find plugin / connection closed: " + e);
		}
	}

	private void startTimeout() {
		timeout = executor.schedule(new Runnable() {
			@Override
			public void run() {
				connected.release();
			}
		}, fcpTimeout, TimeUnit.SECONDS);
	}

	public void pushQuery(InfocalypseQuery query) {
		queries.add(query);
	}

	/**
	 * Block until there is something.
	 * @return the next query in the queue.
	 */
	public InfocalypseQuery takeQuery() {
		InfocalypseQuery query = null;

		do {
			try {
				query = queries.take();
			} catch (InterruptedException e) {
			}
			// Check because take() might have been interrupted.
		} while (query == null);

		return query;
	}
}
