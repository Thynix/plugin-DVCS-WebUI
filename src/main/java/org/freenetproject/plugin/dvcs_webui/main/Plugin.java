package org.freenetproject.plugin.dvcs_webui.main;

import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContainer;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginFCP;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginReplySender;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.FCPHandler;
import org.freenetproject.plugin.dvcs_webui.ui.web.Homepage;

/**
 * Registers the plugin with the Freenet node: pages and for FCP.
 * TODO: Is there no way to have only a different class implement FredPluginFCP, or must it be this one?
 */
public class Plugin implements FredPlugin, FredPluginThreadless, FredPluginVersioned, FredPluginFCP {

	private PluginRespirator pluginRespirator;
	private ToadletContainer tc;

	private FCPHandler fcpHandler;
	private Homepage homepage;

	private static final String MENU_NAME = "Menu";

	public static final String ENCODING = "UTF-8";

	/**
	 * Cookie namespace for sessions. Used to find WoT logins.
	 */
	private static final String SESSION_NAMESPACE = "WebOfTrust";

	public String getVersion() {
		return "0.1-SNAPSHOT";
	}

	@Override
	public void runPlugin(PluginRespirator pr) {
		pluginRespirator = pr;
		tc = pluginRespirator.getToadletContainer();

		L10n l10n = new L10n();
		WoTConnector woTConnector = new WoTConnector(pr);
		SessionManager sessionManager = pr.getSessionManager(SESSION_NAMESPACE);

		fcpHandler = new FCPHandler(pr.getNode().random);
		homepage = new Homepage(pr.getHLSimpleClient(), l10n, fcpHandler, woTConnector, sessionManager);

		pluginRespirator.getPageMaker().addNavigationCategory(homepage.path(), MENU_NAME, MENU_NAME, l10n);
		tc.register(homepage, MENU_NAME, homepage.path(), true, MENU_NAME, MENU_NAME, false, homepage);
	}

	@Override
	public void terminate() {
		tc.unregister(homepage);

		pluginRespirator.getPageMaker().removeNavigationCategory(MENU_NAME);
	}

	@Override
	public void handle(PluginReplySender replysender, SimpleFieldSet params, Bucket data, int accesstype) {
		fcpHandler.handle(replysender, params, data, accesstype);
	}
}
