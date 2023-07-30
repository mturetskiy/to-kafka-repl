package mt.exp.amps.connect;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Connector {
    private String appName;
    private Integer acceptPort;

    private volatile boolean isRunning = false;
    private Thread serverThread;
    private volatile ServerSocket serverSocket;

    public Connector(String appName, Integer acceptPort) {
        this.appName = appName;
        this.acceptPort = acceptPort;

        log.info("Created Connector for appName: {}", appName);
    }

    public void startAccepting() {
        isRunning = true;
        serverThread = new Thread(() -> {
            log.info("Accepting server thread is starting.");
            try (ServerSocket serverSocket = new ServerSocket(acceptPort)) {
                final AtomicInteger connections = new AtomicInteger();
                Integer rcvbuff = serverSocket.getOption(StandardSocketOptions.SO_RCVBUF);
                Integer sndbuff = 0;
//                Integer sndbuff = serverSocket.getOption(StandardSocketOptions.SO_SNDBUF);
                log.info("rcvbuff: {}, sndbuff: {}", rcvbuff, sndbuff);
                serverSocket.setReceiveBufferSize(65536*2);
                serverSocket.setPerformancePreferences(0,1,2);
                int receiveBufferSize = serverSocket.getReceiveBufferSize();
                this.serverSocket = serverSocket; // figure out better way:
                log.info("Server started accepting connections on port: {}, receiveBufferSize: {}", acceptPort, receiveBufferSize);
                while (isRunning) {
                    log.info("Waiting for a connection ...");
                    Socket socket = serverSocket.accept();
                    connections.incrementAndGet();
                    new Thread(() -> new ClientHandler(connections.get()).handle(socket), "client-handler-" + connections.get()).start();
                }
            } catch (Exception e) {
                log.error("Error inside accepting socket server.", e);
                isRunning = false;
            }

            log.info("Accepting server thread is stopped.");
        }, "connector-accept");
        serverThread.start();
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping the connector ..");
        isRunning = false;

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }

            serverThread.join();
            log.info("Connector has been stopped.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("Unable to close server socket.", e);
        }
    }
}
