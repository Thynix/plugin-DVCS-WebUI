package org.freenetproject.plugin.infocalypse_webui.ui.web;

import freenet.client.HighLevelSimpleClient;
import org.apache.velocity.VelocityContext;

/**
 * Plugin homepage / dashboard.
 */
public class Homepage extends VelocityToadlet {

    public Homepage(HighLevelSimpleClient client) {
        super(client, "homepage.vm", "/infocalypse/");
    }

    @Override
    void updateContext(VelocityContext context) {
        context.put("greetings", new String[] { "Hello", "Hi", "Howdy", "How'da do", "What's up", "How's it hanging"});
    }
}
