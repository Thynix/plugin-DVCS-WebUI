package org.freenetproject.plugin.dvcs_webui.ui.fcp.messages;

import freenet.support.SimpleFieldSet;

/**
 * Replies to an unknown message type with an error.
 */
public class Unknown implements MessageHandler {

	@Override
	public SimpleFieldSet reply(SimpleFieldSet params) {
		SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "Error");
		sfs.putOverwrite("Description", "Unrecognized message type '" + params.get("Message") + "'.");
		return sfs;
	}
}
