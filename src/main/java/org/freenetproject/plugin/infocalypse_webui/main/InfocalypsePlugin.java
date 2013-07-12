package org.freenetproject.plugin.infocalypse_webui.main;

import freenet.clients.http.ToadletContainer;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginRespirator;
import org.freenetproject.plugin.infocalypse_webui.ui.web.Homepage;

/**
 * Registers the plugin with the Freenet node.
 */
public class InfocalypsePlugin implements FredPlugin, FredPluginThreadless, FredPluginVersioned {

    private PluginRespirator pluginRespirator;
    private ToadletContainer tc;

    private Homepage homepage;

    private final String menuName = "Infocalypse.Menu";
    public static final String encoding = "UTF-8";

    public String getVersion() {
        return "0.1-SNAPSHOT";
    }

    @Override
    public void runPlugin(PluginRespirator pr) {
        pluginRespirator = pr;
        tc = pluginRespirator.getToadletContainer();

        FredPluginL10n l10n = new InfocalypseL10n();
        homepage = new Homepage(pr.getHLSimpleClient());

        pluginRespirator.getPageMaker().addNavigationCategory(homepage.path(), menuName, menuName, l10n);
        tc.register(homepage, menuName, homepage.path(), true, menuName, menuName, false, homepage);
    }

    @Override
    public void terminate() {
        tc.unregister(homepage);

        pluginRespirator.getPageMaker().removeNavigationCategory(menuName);
    }
}
