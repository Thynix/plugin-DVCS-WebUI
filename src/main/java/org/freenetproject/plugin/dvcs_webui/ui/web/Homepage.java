package org.freenetproject.plugin.dvcs_webui.ui.web;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContext;
import freenet.node.FSParseException;
import freenet.support.SimpleFieldSet;
import freenet.support.api.HTTPRequest;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.view.CookieTool;
import org.freenetproject.plugin.dvcs_webui.main.L10n;
import org.freenetproject.plugin.dvcs_webui.main.WoTConnector;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.FCPHandler;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.Query;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.ResultListener;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Plugin homepage / dashboard.
 */
public class Homepage extends VelocityToadlet {

	private final FCPHandler fcpHandler;
	private final WoTConnector woTConnector;
	/**
	 * Local paths to repositories reported by DVCS.
	 */
	private final ArrayList<String> localPaths;

	// TODO: Some way to reset stored state. (For when a client disconnects or times out.)

	public Homepage(HighLevelSimpleClient client, L10n l10n, FCPHandler handler, WoTConnector connector, SessionManager sessionManager) {
		super(client, l10n, sessionManager, "homepage.vm", "/dvcs/", "Menu");
		fcpHandler = handler;
		woTConnector = connector;

		localPaths = new ArrayList<String>();

		final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		// Query local repo information immediately and every two minutes thereafter. TODO: event-based
		executor.scheduleWithFixedDelay(new LocalRepoListQuery(), 0, 2, TimeUnit.MINUTES);
	}

	/**
	 * The homepage is always enabled so that the menu entry is displayed.
	 * @return true
	 */
	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return true;
	}

	@Override
	void onGet(VelocityContext context) {
		context.put("connected", fcpHandler.isConnected());
		synchronized (localPaths) {
			// TODO: Is toArray() required? Trying to avoid partial lists.
			context.put("paths", localPaths.toArray());
		}
		context.put("identifiers", woTConnector.getLocalIdentifiers());
	}

	private class LocalRepoListQuery implements Runnable, ResultListener {
		// Construct the SFS once instead of each run.
		private final SimpleFieldSet sfs;

		public LocalRepoListQuery() {
			sfs = new SimpleFieldSet(false);
			sfs.putOverwrite("Message", "LocalRepoQuery");
		}

		@Override
		public void run() {
			fcpHandler.pushQuery(new Query(sfs, this));
		}

		@Override
		public void handle(SimpleFieldSet params) {
			final SimpleFieldSet paths;
			try {
				paths = params.getSubset("Path");
			} catch (FSParseException e) {
				// TODO: l4j logging.
				System.err.println("Error parsing");
				return;
			}

			Iterator<String> pathIterator = paths.keyIterator();
			synchronized (localPaths) {
				localPaths.clear();
				while (pathIterator.hasNext()) {
					localPaths.add(paths.get(pathIterator.next()));
				}
			}
		}

		// TODO: Move handles() functionality to Query constructor?
		@Override
		public String handles() {
			return "LocalRepoResult";
		}
	}
}
