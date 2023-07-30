package mt.exp.amps.tool;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
@Component
public class AmpsClient {
    @Value("${amps.server.host}")
    private String host;
    @Value("${amps.server.port}")
    private Integer port;

    private AmpsReader reader;
    private AmpsWriter writer;
    private DataGenerator dataGenerator;

    public AmpsClient() {
        this.reader = new AmpsReader();
        this.writer = new AmpsWriter();
        this.dataGenerator = new DataGenerator();

        log.info("Created amps client.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown the client.");
            stop();
        }));
    }

    @PostConstruct
    public void init() {
        log.info("Connecting to the server at {}:{} .. ", host, port);
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            log.info("Connected.");

            reader.startReading(in);
            writer.startWriting(out, dataGenerator);

            writer.waitForDone();
            reader.waitForDone();

            waitForServerDisconnected(socket, in);

            // wait for socket close on server's side:

            log.info("Done.");
        } catch (Exception e) {
            log.error("Error during interacting with amps server.", e);
        }
    }

    private void waitForServerDisconnected(Socket socket, BufferedReader in) {
        long start = System.currentTimeMillis();
        String line = null;
        while (true) {
            log.info("isClosed: {}", socket.isClosed());
            log.info("isConnected: {}", socket.isConnected());
            log.info("isBound: {}", socket.isBound());
            log.info("isInputShutdown: {}", socket.isInputShutdown());
            log.info("isOutputShutdown: {}", socket.isOutputShutdown());


            try {
                log.info("in.ready: {}", in.ready());
                line = in.readLine();
                log.info("in.readLine: {}", in.readLine());


                Thread.sleep(1);
            } catch (Exception e) {
                log.error("Error during on ready", e);
            }

            log.info("------------------------------------------------------");

            if (line == null) {
                break;
            }


        }

    }

    public void stop() {
        log.info("Stopping the client ..");
        writer.stop();
        reader.stop();
    }
}
