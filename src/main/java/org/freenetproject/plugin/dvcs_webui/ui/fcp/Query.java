package org.freenetproject.plugin.dvcs_webui.ui.fcp;

import freenet.support.SimpleFieldSet;

/**
 * A query to a DVCS for information.
 */
public class Query {
	public final SimpleFieldSet query;
	public final ResultListener listener;

	public Query(SimpleFieldSet sfs, ResultListener resultListener) {
		this.query = sfs;
		this.listener = resultListener;
	}
}
