package mt.exp.amps.tool;

import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class AmpsWriter {
    private volatile boolean isRunning;
    private Thread writingThread;
    private long lastCheckTs;

    private long totalSentMsg = 0;

    private AtomicInteger ratePerSec = new AtomicInteger();

    public void startWriting(PrintWriter out, Iterator<?> dataIterator) {
        isRunning = true;

        writingThread = new Thread(() -> {
            log.info("Sending messages to the server ..");
            lastCheckTs = System.currentTimeMillis();
            while (isRunning && dataIterator.hasNext()) {
                out.println(dataIterator.next());
                totalSentMsg++;

                long newCheckTs = System.currentTimeMillis();
                ratePerSec.incrementAndGet();
                if (newCheckTs - lastCheckTs >= 1000) {
                    log.info("Sent: {} msg. Current rate is: {} msg/sec", totalSentMsg, ratePerSec.get());
                    lastCheckTs = newCheckTs;
                    ratePerSec.set(0);
                }
            }

            log.info("Done with sending data. Send: {} msg. Sending STOP ...", totalSentMsg);
            out.println("STOP");
            log.info("Done with sending.");
        }, "writer");
        writingThread.start();
    }

    public void waitForDone() {
        if (writingThread != null) {
            try {
                writingThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        log.info("Stopping writer.");
        isRunning = false;
    }
}
