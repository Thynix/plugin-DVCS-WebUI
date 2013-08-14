package org.freenetproject.plugin.dvcs_webui.ui.fcp;

import freenet.support.SimpleFieldSet;

/**
 * Classes implementing this interface and pushed as part of a Query will be called with the result of the query.
 */
public interface ResultListener {
	/**
	 * Perform actions based on a query result.
	 */
	public void handle(SimpleFieldSet params);

	/**
	 * TODO: Used to allow registering a listener with the correct QueryResult MessageHandler. Is this appropriate?
	 *
	 * @return Name of the response this listener handles.
	 */
	public String handles();
}
