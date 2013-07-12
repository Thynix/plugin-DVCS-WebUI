package org.freenetproject.plugin.infocalypse_webui.ui.web;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.PageNode;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.api.HTTPRequest;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

/**
 * Uses Velocity templates to render a Toadlet.
 */
public abstract class VelocityToadlet extends Toadlet implements LinkEnabledCallback {

    private final String path;
    private final String templateName;

    public VelocityToadlet(HighLevelSimpleClient client, String templateName, String path) {
        super(client);
        this.path = path;
        this.templateName = templateName;

        // Templates are stored in jars on the classpath.
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader" +
                ".ClasspathResourceLoader");

        Velocity.init(properties);
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public boolean isEnabled(ToadletContext ctx) {
        return true;
    }

    public void handleMethodGET(URI uri, HTTPRequest request, ToadletContext ctx) throws
            ToadletContextClosedException, IOException {

        VelocityContext context = new VelocityContext();
        for (String key : request.getParameterNames()) {
            context.put(key, request.getParam(key));
        }

        PageNode pageNode = ctx.getPageMaker().getPageNode("Infocalypse", ctx);
        pageNode.content.addChild("%", render(context));

        writeReply(ctx, 200, "text/html", "OK", pageNode.outer.generate());
    }

    /**
     * @return templateName along with the qualifiers for the classpath loader to find it.
     */
    String getTemplate() {
        return "/templates/" + templateName;
    }

    abstract String render(VelocityContext context);
}
