package org.freenetproject.plugin.dvcs_webui.ui.fcp.messages;

import freenet.support.SimpleFieldSet;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.FCPHandler;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.Query;

/**
 * Replies to a Ready with a UI event, once one occurs.
 */
public class Ready implements MessageHandler {
	private final FCPHandler fcpHandler;

	public Ready(FCPHandler fcp) {
		fcpHandler = fcp;
	}

	@Override /* Queries sent to the DVCS are actually replies to Ready. */
	public SimpleFieldSet reply(SimpleFieldSet params) {
		Query query = fcpHandler.takeQuery();
		fcpHandler.registerListener(query.listener, params.get("QueryIdentifier"));
		return query.query;
	}
}
