package org.freenetproject.plugin.dvcs_webui.ui.fcp.messages;

import freenet.support.SimpleFieldSet;

/**
 * Replies to a Ping with a Pong.
 */
public class Ping implements MessageHandler {
	@Override
	public SimpleFieldSet reply(SimpleFieldSet params) {
		SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "Pong");
		return sfs;
	}
}
