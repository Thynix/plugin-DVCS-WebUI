package org.freenetproject.plugin.dvcs_webui.ui.fcp.messages;

import freenet.support.SimpleFieldSet;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.FCPHandler;

/**
 * Replies to a Ready with a UI event, once one occurs.
 */
public class Ready implements MessageHandler {
    /*
     * TODO: If the connection has been closed between the time this was received and the request was sent as a
     * response, how to avoid losing the request? This message may have to get feedback on whether the sending was
     * successful?
     *
     * Possible commands sent to Infocalypse:
     * * ListLocalRepos
     * * GetRepoDir
     * * SetRepoDir
     */

	private final FCPHandler fcpHandler;

	public Ready(FCPHandler fcp) {
		fcpHandler = fcp;
	}

	@Override /* Queries sent to the DVCS are actually replies to Ready. */
	public SimpleFieldSet reply(SimpleFieldSet params) {
		return fcpHandler.takeQuery().query;
	}
}
