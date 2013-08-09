package org.freenetproject.plugin.dvcs_webui.ui.fcp;

import freenet.pluginmanager.FredPluginFCP;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginReplySender;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

import java.util.ArrayDeque;
import java.util.concurrent.ScheduledFuture;
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

	private String connectedIdentifier;
	private final ScheduledThreadPoolExecutor executor;

	// TODO: Timeout or Disconnect releases.
	private final Semaphore connected;
	// TODO: Replace with BlockingQueue. Where best to put it?
	// http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/BlockingQueue.html
	private final ArrayDeque<InfocalypseQuery> queries;

	public FCPHandler() {
		executor = new ScheduledThreadPoolExecutor(1);
		queries = new ArrayDeque<InfocalypseQuery>();
		connected = new Semaphore(1);
	}

	@Override
	public void handle(PluginReplySender replySender, SimpleFieldSet params, Bucket data, int accessType) {
		// TODO: What to do with accessType?
		// TODO: Switching on strings would be nice given Java 7 or up. Also consider a map of pre-constructed handler
		// classes or reflection like LCWoT's FCP interface.
		// TODO: Will the flow resulting from having the response out here be desirable?
		SimpleFieldSet response = new SimpleFieldSet(true);
		if (params.get("Message").equals("Hello")) {
			if (connected.tryAcquire()) {
				// TODO: Check supported queries. Probably should be an error if not enough are supported?
				response.putOverwrite("Message", "HiThere");
			} else {
				response.putOverwrite("Message", "Error");
				response.putOverwrite("Description", "Another DVCS is already connected.");
			}
		}
		try {
			replySender.send(response);
		} catch (PluginNotFoundException e) {
			System.err.println("Cannot find plugin / connection closed: " + e);
		}
	}

	/**
	 * @return the identifier of the connected session, or null if no session is connected.
	 */
	public String getConnectedIdentifier() {
		synchronized (executor) {
			return connectedIdentifier;
		}
	}

	public void pushQuery(InfocalypseQuery query) {
		synchronized (queries) {
			queries.addLast(query);
			// ClearToSend might be waiting.
			notifyAll();
		}
	}

	/**
	 * @return the next query in the queue, or null if the queue is empty.
	 */
	InfocalypseQuery peekQuery() {
		synchronized (queries) {
			return queries.peekFirst();
		}
	}
}
