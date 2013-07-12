package org.freenetproject.plugin.infocalypse_webui.ui.web;

import freenet.client.HighLevelSimpleClient;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.freenetproject.plugin.infocalypse_webui.main.InfocalypsePlugin;

import java.io.StringWriter;

/**
 * Plugin homepage / dashboard.
 */
public class Homepage extends VelocityToadlet {

    public Homepage(HighLevelSimpleClient client) {
        super(client, "homepage.vm", "/infocalypse/");
    }

    @Override
    String render(VelocityContext context) {
        context.put("greetings", new String[] { "Hello", "Hi", "Howdy", "How'da do", "What's up", "How's it hanging"});

        StringWriter writer = new StringWriter();
        Velocity.mergeTemplate(getTemplate(), InfocalypsePlugin.encoding, context, writer);

        return writer.toString();
    }
}
