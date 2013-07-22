package org.freenetproject.plugin.infocalypse_webui.ui.web;

import freenet.client.HighLevelSimpleClient;
import org.apache.velocity.VelocityContext;
import org.freenetproject.plugin.infocalypse_webui.main.InfocalypseL10n;
import org.freenetproject.plugin.infocalypse_webui.ui.fcp.FCPHandler;

/**
 * Plugin homepage / dashboard.
 */
public class Homepage extends VelocityToadlet {

    private final FCPHandler fcpHandler;

    public Homepage(HighLevelSimpleClient client, InfocalypseL10n l10n, FCPHandler handler) {
        super(client, l10n, "homepage.vm", "/infocalypse/", "Infocalypse.Menu");
        fcpHandler = handler;
    }

    @Override
    void updateContext(VelocityContext context) {
        context.put("greetings", new String[] { "Hello", "Hi", "Howdy", "How'da do", "What's up", "How's it hanging"});
        context.put("connected", fcpHandler.isConnected());
    }
}
