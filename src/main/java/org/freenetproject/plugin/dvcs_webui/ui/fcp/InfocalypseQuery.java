package org.freenetproject.plugin.dvcs_webui.ui.fcp;

import freenet.support.SimpleFieldSet;

/**
 * A query to Infocalypse for information.
 */
public class InfocalypseQuery {
    public final SimpleFieldSet query;
    public final InfocalypseResponseHandler handler;

    public InfocalypseQuery(SimpleFieldSet sfs, InfocalypseResponseHandler responseHandler) {
        this.query = sfs;
        this.handler = responseHandler;
    }
}
