package org.freenetproject.plugin.dvcs_webui.ui.web;

import freenet.client.HighLevelSimpleClient;
import freenet.support.SimpleFieldSet;
import org.apache.velocity.VelocityContext;
import org.freenetproject.plugin.dvcs_webui.main.L10n;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.FCPHandler;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.Query;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.ResultListener;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Plugin homepage / dashboard.
 */
public class Homepage extends VelocityToadlet {

	private final FCPHandler fcpHandler;
	// TODO: Assuming this needs to be private (and not local) to stay around and not get GC'd?
	private final ScheduledThreadPoolExecutor executor;

	// TODO: Some way to reset stored state. (For when a client disconnects or times out.)

	public Homepage(HighLevelSimpleClient client, L10n l10n, FCPHandler handler) {
		// TODO: Remove "Infocalypse" from l10n; path.
		super(client, l10n, "homepage.vm", "/infocalypse/", "Infocalypse.Menu");
		fcpHandler = handler;

		executor = new ScheduledThreadPoolExecutor(1);
		// Query local repo information immediately and every 5 seconds thereafter.
		executor.scheduleWithFixedDelay(new VoidQuery(), 0, 5, TimeUnit.SECONDS);
	}

	@Override
	void updateContext(VelocityContext context) {
		context.put("greetings", new String[]{"Hello", "Hi", "Howdy", "How'da do", "What's up", "How's it hanging"});
		context.put("connected", fcpHandler.isConnected());
	}

	private class VoidQuery implements Runnable, ResultListener {
		// Construct the SFS once instead of each run.
		private final SimpleFieldSet sfs;

		public VoidQuery() {
			sfs = new SimpleFieldSet(false);
			sfs.putOverwrite("Message", "VoidQuery");
		}

		@Override
		public void run() {
			fcpHandler.pushQuery(new Query(sfs, this));
		}

		@Override
		public void handle(SimpleFieldSet params) {
			assert params.get("Message").equals("VoidResult");
			System.out.println("Got VoidResult");
		}

		@Override
		public String handles() {
			return "VoidResult";
		}
	}
}
