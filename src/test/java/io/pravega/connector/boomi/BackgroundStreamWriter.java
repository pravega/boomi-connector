package io.pravega.connector.boomi;

import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

class BackgroundStreamWriter implements Runnable, AutoCloseable {
    private static Logger log = LoggerFactory.getLogger(BackgroundStreamWriter.class);

    private EventStreamWriter<String> eventWriter;
    private AtomicBoolean running = new AtomicBoolean(true);
    private int eventCounter = 0;
    private int maxEvents;
    private long startTime;
    private List<Future> futures = new ArrayList<>(500000); // don't let resizing slow us down
    private String jsonMessage = TestUtils.generateJsonMessage();
    private AtomicBoolean paused = new AtomicBoolean(false);
    private final Object pauseLock = new Object();

    BackgroundStreamWriter(EventStreamClientFactory clientFactory, String stream) {
        this(clientFactory, stream, Integer.MAX_VALUE);
    }

    BackgroundStreamWriter(EventStreamClientFactory clientFactory, String stream, int maxEvents) {
        this.maxEvents = maxEvents;
        eventWriter = clientFactory.createEventWriter(stream, new UTF8StringSerializer(), EventWriterConfig.builder().build());
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public synchronized void run() {
        startTime = System.currentTimeMillis();
        while (running.get() && eventCounter < maxEvents) {
            futures.add(eventWriter.writeEvent(jsonMessage));
            eventCounter++;
            checkForPause();
        }
        running.set(false);
    }

    private void checkForPause() {
        if (paused.get()) {
            synchronized (pauseLock) {
                log.debug("writer has been paused (wrote {} events)", eventCounter);
                try {
                    pauseLock.wait();
                    log.debug("writer has been resumed");
                } catch (InterruptedException e) {
                    log.warn("interrupted while paused", e);
                }
            }
        }
    }

    void pause() {
        paused.set(true);
    }

    void resume() {
        paused.set(false);
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    void stop() {
        running.set(false);
    }

    @Override
    public void close() {
        stop();
        synchronized (this) { // since run() is synchronized, this will wait until execution is complete
            if (eventWriter != null) {
                eventWriter.close();
                eventWriter = null;
                log.info("wrote {} events in {} ms", eventCounter, System.currentTimeMillis() - startTime);
            }
        }
    }

    // since run() is synchronized, this will wait until execution is complete
    synchronized void waitForAck() throws Exception {
        for (Future future : futures) future.get();
    }

    void waitForMaxEvents() throws Exception {
        assert maxEvents < Integer.MAX_VALUE; // dummy detector

        while (running.get() && startTime == 0) { // wait until execution actually starts
            Thread.sleep(1000);
        }

        waitForAck();
    }
}
