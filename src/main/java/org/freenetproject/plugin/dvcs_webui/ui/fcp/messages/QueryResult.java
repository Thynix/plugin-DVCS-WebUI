package org.freenetproject.plugin.dvcs_webui.ui.fcp.messages;

import freenet.support.SimpleFieldSet;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.ResultListener;

import java.util.HashMap;

/**
 * Reply to a to a query result with an Ack. Distributes results to listeners registered by QueryIdentifier.
 *
 * There is a different instance for each result type.
 */
public class QueryResult implements MessageHandler {

	/**
	 * An exchange uses the same QueryIdentifier between the Ready, query, response, and Ack.
	 */
	private final HashMap<String, ResultListener> listeners;

	public QueryResult() {
		listeners = new HashMap<String, ResultListener>();
	}

	/**
	 * Register a listener for the result with a given QueryIdentifier.
	 *
	 * @param queryIdentifier QueryIdentifier to use.
	 */
	public void register(String queryIdentifier, ResultListener listener) {
		listeners.put(queryIdentifier, listener);
	}

	@Override
	/**
	 * Send an Ack with the same QueryIdentifier. Give result to registered listener.
	 */
	public SimpleFieldSet reply(SimpleFieldSet params) {
		final String queryIdentifier = params.get("QueryIdentifier");

		// TODO: What if a listener takes a while?
		listeners.get(queryIdentifier).handle(params);

		SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "Ack");
		sfs.putOverwrite("QueryIdentifier", queryIdentifier);
		return sfs;
	}
}
