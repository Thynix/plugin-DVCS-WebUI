package org.freenetproject.plugin.dvcs_webui.main;

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

	private static final String menuName = "Infocalypse.Menu";

	public static final String encoding = "UTF-8";

	public String getVersion() {
		return "0.1-SNAPSHOT";
	}

	@Override
	public void runPlugin(PluginRespirator pr) {
		pluginRespirator = pr;
		tc = pluginRespirator.getToadletContainer();

		InfocalypseL10n l10n = new InfocalypseL10n();
		fcpHandler = new FCPHandler();
		homepage = new Homepage(pr.getHLSimpleClient(), l10n, fcpHandler);

		pluginRespirator.getPageMaker().addNavigationCategory(homepage.path(), menuName, menuName, l10n);
		tc.register(homepage, menuName, homepage.path(), true, menuName, menuName, false, homepage);
	}

	@Override
	public void terminate() {
		tc.unregister(homepage);

		pluginRespirator.getPageMaker().removeNavigationCategory(menuName);
	}

	@Override
	public void handle(PluginReplySender replysender, SimpleFieldSet params, Bucket data, int accesstype) {
		fcpHandler.handle(replysender, params, data, accesstype);
	}
}
