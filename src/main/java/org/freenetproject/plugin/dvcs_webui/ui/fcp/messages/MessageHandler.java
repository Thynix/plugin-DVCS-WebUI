package org.freenetproject.plugin.dvcs_webui.ui.fcp.messages;

import freenet.support.SimpleFieldSet;

/**
 * Implemented by handlers for specific FCP messages.
 * <p/>
 * TODO: It would be possible, at the expense of odd code in FCPHandler, to avoid each implementer needing to create
 * and return its own SFS instance, which contributes to boilerplate. This would be a void reply() that modifies a
 * passed-in SFS. Is that worth it?
 * // TODO: The interface is named "MessageHandler", yet its method is named "reply()". That seems confusing.
 */
public interface MessageHandler {
	/**
	 * @param params message parameters
	 */
	public SimpleFieldSet reply(SimpleFieldSet params);
}
