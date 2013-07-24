package org.freenetproject.plugin.infocalypse_webui.ui.fcp;

import freenet.support.SimpleFieldSet;

/**
 * Replies with an error to an unhandled ClearToSend.
 */
public class Overload implements MessageHandler {
    @Override
    public SimpleFieldSet reply(SimpleFieldSet params) {
        SimpleFieldSet sfs = new SimpleFieldSet(true);
        sfs.putOverwrite("Message", "Error");
        // In principle they could be, but supporting only a single one is easier for initial implementation.
        // See comments on FCPHandler.
        sfs.putOverwrite("Description", "Multiple simultaneous ClearToSends are not currently supported.");
        return sfs;
    }
}
