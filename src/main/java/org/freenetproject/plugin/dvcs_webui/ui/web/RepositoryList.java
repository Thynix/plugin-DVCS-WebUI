package org.freenetproject.plugin.dvcs_webui.ui.web;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.SessionManager;
import freenet.support.SimpleFieldSet;
import freenet.support.api.HTTPRequest;
import org.apache.velocity.VelocityContext;
import org.freenetproject.plugin.dvcs_webui.main.L10n;
import org.freenetproject.plugin.dvcs_webui.main.Plugin;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.FCPHandler;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.Query;
import org.freenetproject.plugin.dvcs_webui.ui.fcp.ResultListener;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * List repositories published by remote identities.
 */
public class RepositoryList extends VelocityToadlet implements ResultListener {

	// TODO: How to handle sending many requests? Is displaying only the latest one reasonable?

	private final FCPHandler fcpHandler;

	private final ArrayList<Repository> repositories;

	// This must be public so that Velocity can access it.
	public class Repository {
		// Velocity calls methods, but does not check for public member variables.
		// https://velocity.apache.org/engine/devel/user-guide.html#propertylookuprules
		private final String name;
		private final String key;

		public Repository(String name_, String key_) {
			name = name_;
			key = key_;
		}

		public String getName() {
			return name;
		}

		public String getKey() {
			return key;
		}
	}

	public RepositoryList(HighLevelSimpleClient client, L10n l10n, FCPHandler handler, SessionManager sessionManager) {
		super(client, l10n, sessionManager, "RepositoryList.vm", "/dvcs/list", "ListRepositories");

		fcpHandler = handler;
		repositories = new ArrayList<Repository>();
	}

	@Override
	void onGet(VelocityContext context) {
		context.put("repositories", repositories);
	}

	@Override
	void onPost(HTTPRequest request) {
		SimpleFieldSet sfs = new SimpleFieldSet(true);
		sfs.putOverwrite("Message", "RepoListQuery");
		sfs.putOverwrite("Truster", request.getPartAsStringFailsafe("local-id", Plugin.IDENTITY_ID_LENGTH));
		sfs.putOverwrite("RemoteIdentifier",
		                 request.getPartAsStringFailsafe("remote_identifier", Plugin.MAX_IDENTIFIER_LENGTH));

		fcpHandler.pushQuery(new Query(sfs, this));
	}

	@Override
	public void handle(SimpleFieldSet params) {
		SimpleFieldSet subset = params.subset("Repo");
		Iterator<String> names = subset.keyIterator();

		// TODO: Locking to prevent an incomplete list from being displayed.
		// TODO: Something with Iterable might allow a cleaner loop.
		repositories.clear();
		while (names.hasNext()) {
			final String name = names.next();
			repositories.add(new Repository(name, subset.get(name)));
		}
	}

	@Override
	public String handles() {
		return "RepoListResult";
	}
}
