package org.freenetproject.plugin.infocalypse_webui.ui.web;

import freenet.client.HighLevelSimpleClient;
import org.apache.velocity.VelocityContext;
import org.freenetproject.plugin.infocalypse_webui.main.InfocalypseL10n;

/**
 * Plugin homepage / dashboard.
 */
public class Homepage extends VelocityToadlet {

    public Homepage(HighLevelSimpleClient client, InfocalypseL10n l10n) {
        super(client, l10n, "homepage.vm", "/infocalypse/", "Infocalypse.Menu");
    }

    @Override
    void updateContext(VelocityContext context) {
        context.put("greetings", new String[] { "Hello", "Hi", "Howdy", "How'da do", "What's up", "How's it hanging"});
    }
}
