package mt.exp.amps.connect;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ClientHandler {
    private int connectionId;

    private long lastCheckTs;
    private long totalReceivedMsg = 0;
    private AtomicInteger ratePerSec = new AtomicInteger();

    public ClientHandler(int connectionId) {
        this.connectionId = connectionId;
    }

    public void handle(Socket socket) {
        try {
            log.info("[{}] Handling client's connection from: {}", connectionId, socket.getRemoteSocketAddress());

            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()), 64)) {

                long sentMessages = 0;
                String line;
                lastCheckTs = System.currentTimeMillis();
                while ((line = in.readLine()) != null) {
                    totalReceivedMsg++;
                    long newCheckTs = System.currentTimeMillis();
                    ratePerSec.incrementAndGet();
                    if (newCheckTs - lastCheckTs >= 1000) {
                        log.info("Received: {} msg. Current rate is: {} msg/sec", totalReceivedMsg, ratePerSec.get());
                        lastCheckTs = newCheckTs;
                        ratePerSec.set(0);
                    }

//                log.info("[IN] {}", line);

                    if (line.equals("STOP")) {
                        log.info("[{}] Received stop. Closing the connection with the client.", connectionId);
                        break;
                    }

                    out.println("Received msg: " + line + ", size: " + line.length() * 2 + " bytes.");
                }

                log.info("[{}] Sending STOP to the client. receivedMessages: {}, sentMessages: {}", connectionId, totalReceivedMsg, sentMessages);
                out.println("End ot session. Bye!");

            } catch (Exception e) {
                log.error("[{}] Error during handling client's request", connectionId, e);
            }
        } finally {
            try {
                log.info("[{}] Closing client's socket", connectionId);
                socket.close();
                log.info("[{}] Client's socket is closed. Done with client.", connectionId);
            } catch (IOException e) {
                log.error("[{}] Error during client's socket close.", connectionId, e);
            }
        }
    }
}
