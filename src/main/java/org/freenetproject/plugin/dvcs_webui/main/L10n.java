package org.freenetproject.plugin.dvcs_webui.main;

import freenet.l10n.BaseL10n;
import freenet.l10n.PluginL10n;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginL10n;

/**
 * Provides Fred plugin localization.
 */
public class L10n implements FredPluginBaseL10n, FredPluginL10n {
	private PluginL10n l10n;

	public L10n() {
		l10n = new PluginL10n(this);
	}

	@Override
	public String getString(String s) {
		return l10n.getBase().getString(s);
	}

	/**
	 * Shorter version of getString() for template use.
	 */
	public String get(String s) {
		return getString(s);
	}

	@Override
	public void setLanguage(BaseL10n.LANGUAGE language) {
		l10n = new PluginL10n(this, language);
	}

	@Override
	public String getL10nFilesBasePath() {
		return "l10n/";
	}

	@Override
	public String getL10nFilesMask() {
		return "Infocalypse.${lang}.properties";
	}

	@Override
	public String getL10nOverrideFilesMask() {
		return "Infocalypse.${lang}.override.properties";
	}

	@Override
	public ClassLoader getPluginClassLoader() {
		return Plugin.class.getClassLoader();
	}
}
