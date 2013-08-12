package org.freenetproject.plugin.dvcs_webui.ui.web;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.PageNode;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.api.HTTPRequest;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.freenetproject.plugin.dvcs_webui.main.L10n;
import org.freenetproject.plugin.dvcs_webui.main.Plugin;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Properties;

/**
 * Uses Velocity templates to render a Toadlet.
 */
public abstract class VelocityToadlet extends Toadlet implements LinkEnabledCallback {

	private final String path;
	private final String templateName;
	private final String title;
	private final L10n l10n;

	/**
	 * @param client       required by Toadlet.
	 * @param l10n         used to localize page and title.
	 * @param templateName template filename to use.
	 * @param path         path to register to.
	 * @param title        page title localization key.
	 */
	public VelocityToadlet(HighLevelSimpleClient client, L10n l10n, String templateName, String path,
	                       String title) {
		super(client);
		this.l10n = l10n;
		this.path = path;
		this.templateName = templateName;
		this.title = l10n.get(title);

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

		context.put("t", l10n);

		updateContext(context);

		StringWriter writer = new StringWriter();
		Velocity.mergeTemplate(getTemplate(), Plugin.encoding, context, writer);

		PageNode pageNode = ctx.getPageMaker().getPageNode("Infocalypse", ctx);
		pageNode.content.addChild("%", writer.toString());

		writeReply(ctx, 200, "text/html", "OK", pageNode.outer.generate());
	}

	/**
	 * @return templateName along with the qualifiers for the classpath loader to find it.
	 */
	String getTemplate() {
		return "/templates/" + templateName;
	}

	/**
	 * Puts additional variables, properties, and methods in the context as required.
	 *
	 * @param context context to modify
	 */
	abstract void updateContext(VelocityContext context);
}
