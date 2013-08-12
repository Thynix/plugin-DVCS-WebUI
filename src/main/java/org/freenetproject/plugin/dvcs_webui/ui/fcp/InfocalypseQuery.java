package org.freenetproject.plugin.dvcs_webui.ui.fcp;

import freenet.support.SimpleFieldSet;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.messages.QueryResponse;

/**
 * A query to Infocalypse for information.
 */
public class InfocalypseQuery {
	public final SimpleFieldSet query;
	public final QueryResponse handler;

	public InfocalypseQuery(SimpleFieldSet sfs, QueryResponse responseHandler) {
		this.query = sfs;
		this.handler = responseHandler;
	}
}
