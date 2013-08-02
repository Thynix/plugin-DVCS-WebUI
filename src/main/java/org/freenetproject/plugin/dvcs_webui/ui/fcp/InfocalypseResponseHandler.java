package org.freenetproject.plugin.dvcs_webui.ui.fcp;

import freenet.support.SimpleFieldSet;

/**
 * Replies to a to a query response from Infocalypse with an ACK with matching SequenceID.
 */
public abstract class InfocalypseResponseHandler implements MessageHandler
{
    @Override
    public SimpleFieldSet reply(SimpleFieldSet params) {
        SimpleFieldSet sfs = new SimpleFieldSet(true);
        sfs.putOverwrite("Message", "ACK");
        sfs.putOverwrite("SequenceID", params.get("SequenceID"));
        sfs.putAllOverwrite(handle(params));
        return sfs;
    }

    /**
     * Handle an Infocalypse query result. InfocalypseResponseHandler handles perpetuating SessionID. In a conflict
     * between the values determined here and those in the superclass, those determined here are used.
     * @param params result to handle.
     * @return Anything additional
     */
    public abstract SimpleFieldSet handle(SimpleFieldSet params);
}
