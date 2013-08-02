package org.freenetproject.plugin.dvcs_webui.ui.fcp;

import freenet.support.SimpleFieldSet;

/**
 * Replies to a ClearToSend with a UI event, once one occurs.
 */
public class ClearToSend implements MessageHandler {
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

    public ClearToSend(FCPHandler fcp) {
        fcpHandler = fcp;
    }

    @Override /* queries to infocalypse are actually replies to ClearToSend */ 
    public SimpleFieldSet reply(SimpleFieldSet params) {
        InfocalypseQuery query;
        while ( (query = fcpHandler.peekQuery()) == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                // Time to check again.
            }
        }
        return query.query;
    }
}
