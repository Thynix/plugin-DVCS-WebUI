package org.freenetproject.plugin.infocalypse_webui.ui.web;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.api.HTTPRequest;

import java.io.IOException;
import java.net.URI;

/**
 * Plugin homepage / dashboard.
 */
public class Homepage extends Toadlet implements LinkEnabledCallback {
    public static final String PATH = "/infocalypse/";

    public Homepage(HighLevelSimpleClient client) {
        super(client);
    }

    @Override
    public String path() {
        return PATH;
    }

    public void handleMethodGET(URI uri, HTTPRequest request, ToadletContext ctx) throws
            ToadletContextClosedException, IOException {
        writeReply(ctx, 200, "text/plain", "OK", "Whazza!");
    }

    @Override
    public boolean isEnabled(ToadletContext ctx) {
        return true;
    }
}
