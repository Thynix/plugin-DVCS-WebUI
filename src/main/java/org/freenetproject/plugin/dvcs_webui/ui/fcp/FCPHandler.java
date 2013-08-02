package org.freenetproject.plugin.dvcs_webui.ui.fcp;

import freenet.pluginmanager.FredPluginFCP;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginReplySender;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

import java.util.ArrayDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Tracks session state and dispatches messages to specific handlers.
 *
 * The UI event code currently handles a single event at a time, and treats more than one simultaneous ClearToSend
 * as an error. This is simpler to get things started, but is likely to be slow. Asynchronous code is a good place to
 * look for initial performance improvements.
 */
public class FCPHandler implements FredPluginFCP {

    /*
     * TODO: Where to keep state, like which sessions are still considered connected? Hm, that should be any message.
     * Still, where to keep state when it is needed? Per message type doesn't seem to make sense. It depends on the
     * type of state and where it's needed, so perhaps that remains to be seen when actual functionality comes into
     * the fold.
     *
     * Is connectedness only a UI thing? It would make sense if any message causes reconnection,
     * and there isn't yet an apparent reason why a session would carry its own state.
     *
     * So Infocalypse sends Ping, which merits no response, and ClearToSend, which replies with whatever user request
     * from the UI?
     *
     * TODO: Does it make sense to only allow one session at once? Is there a less ugly way to implement a timeout?
     */

    /**
     * Seconds before an FCP connection to Infocalypse is considered timed out.
     */
    public static final long fcpTimeout = 5;

    private String connectedIdentifier;
    private final ScheduledThreadPoolExecutor executor;
    private ScheduledFuture future;

    /**
     * SequenceID from the active ClearToSend. Matched with response.
     */
    private String sequenceID;
    private final ArrayDeque<InfocalypseQuery> queries;

    public FCPHandler() {
        executor = new ScheduledThreadPoolExecutor(1);
        queries = new ArrayDeque<InfocalypseQuery>();
        sequenceID = null;
    }

    @Override
    public void handle(PluginReplySender replySender, SimpleFieldSet params, Bucket data, int accessType) {
        // TODO: What to do with accessType?
        synchronized (executor) {
            if (connectedIdentifier == null) {
                connectedIdentifier = replySender.getIdentifier();
            } else if (!connectedIdentifier.equals(replySender.getIdentifier())) {
                // A different identifier is already connected.
                SimpleFieldSet sfs = new SimpleFieldSet(true);
                sfs.putOverwrite("Message", "Error");
                sfs.putOverwrite("Description", "An Infocalypse session is already connected: " + connectedIdentifier);
                try {
                    replySender.send(sfs);
                } catch (PluginNotFoundException e) {
                    // TODO: Lazy error handling. Look into real logging.
                    System.err.println("Cannot find plugin / connection closed: " + e);
                }
                return;
            } else {
                // This identifier was already connected.
                assert connectedIdentifier.equals(replySender.getIdentifier());
                // In order to be connected the timeout should already be scheduled.
                assert future != null;
                future.cancel(false);
            }

            future = executor.schedule(new Runnable() {
                @Override
                public void run() {
                    synchronized (executor) {
                        connectedIdentifier = null;
                    }
                }
            }, fcpTimeout, TimeUnit.SECONDS);
        }

        // TODO: The message type handler needs information on whether the message was successfully sent.
        // TODO: Perhaps run-time class lookup instead of if-else blocks? It might also be faster / more flexible to
        // put already-constructed instances into a HashMap. That might be too much like Python.
        // TODO: The flow of this is really messy.
        final String messageName = params.get("Message");
        final MessageHandler handler;
        if ("Ping".equals(messageName)) {
            handler = new Ping();
        } else if ("ClearToSend".equals(messageName)) {
            synchronized (queries) {
                // TODO: Shuffling off something that deals so much with state in here seems odd.
                // TODO: Move sequenceID handling somewhere else, even if it's just registration methods called from
                // ClearToSend() handlers.
                if (sequenceID == null) {
                    sequenceID = params.get("SequenceID");
                    handler = new ClearToSend(this);
                } else {
                    handler = new Overload();
                }
            }
        } else {
            // TODO: Where is a better place to handle query responses?
            // Might be a query response.
            synchronized (queries) {
                if (sequenceID.equals(params.get("SequenceID")) && !queries.isEmpty()) {
                    handler = queries.removeFirst().handler;
                    // Can now accept another ClearToSend.
                    sequenceID = null;
                } else {
                    handler = new Unknown();
                }
            }
        }

        try {
            replySender.send(handler.reply(params));
        } catch (PluginNotFoundException e) {
            // TODO: Copy and paste from above. Wrapper, rearrange control flow, or just put up with it?
            System.err.println("Cannot find plugin / connection closed: " + e);
        }
    }

    /**
     * @return the identifier of the connected session, or null if no session is connected.
     */
    public String getConnectedIdentifier() {
        synchronized (executor) {
            return connectedIdentifier;
        }
    }

    public void pushQuery(InfocalypseQuery query) {
        synchronized (queries) {
            queries.addLast(query);
            // ClearToSend might be waiting.
            notifyAll();
        }
    }

    /**
     * @return the next query in the queue, or null if the queue is empty.
     */
    InfocalypseQuery peekQuery() {
        synchronized (queries) {
            return queries.peekFirst();
        }
    }
}
