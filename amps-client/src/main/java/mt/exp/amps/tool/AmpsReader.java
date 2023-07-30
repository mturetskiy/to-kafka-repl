package mt.exp.amps.tool;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class AmpsReader {
    public static final long GRACEFUL_TIMEOUT_MS = 5_000;
    private volatile boolean isRunning;
    private Thread readingThread;
    private long lastCheckTs;
    private long totalReceivedMsg = 0;
    private AtomicInteger ratePerSec = new AtomicInteger();

    public void startReading(BufferedReader in) {
        isRunning = true;
        readingThread = new Thread(() -> {
            long stopTs = 0;
            long messagesAfterStop = 0;
            String line = null;
            try {
                lastCheckTs = System.currentTimeMillis();
                while ((line = in.readLine()) != null) {
                    if (!isRunning) {
                        stopTs = System.currentTimeMillis();
                        messagesAfterStop++;
                    }

                    totalReceivedMsg++;
                    long newCheckTs = System.currentTimeMillis();
//                    log.info("Response: {}", line);
//

                    ratePerSec.incrementAndGet();
                    if (newCheckTs - lastCheckTs >= 1000) {
                        log.info("Received: {} msg. Current rate is: {} msg/sec", totalReceivedMsg, ratePerSec.get());
                        lastCheckTs = newCheckTs;
                        ratePerSec.set(0);
                    }

                    process(line);
                }

                long doneReadTs = System.currentTimeMillis();
                log.info("Delta : {} ms. messagesAfterStop: {}", doneReadTs - stopTs, messagesAfterStop);
            } catch (Exception e) {
                log.error("Error during reading responses.", e);
            }

            log.info("Done with response reading.");
        }, "reader");
        readingThread.start();
    }

    private void process(String msg) {
//        try {
////            Thread.sleep(0, 100);
//        } catch (InterruptedException e){}
        for (int i = 0; i < 10000; i++) {
            long k = Math.round(Math.random() * i * 100);
        }
    }

    public void waitForDone() {
        if (readingThread != null) {
            try {
                readingThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        log.info("Stopping reader.");
        isRunning = false;
    }
}
