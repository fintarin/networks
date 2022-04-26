package org.networks;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    private Integer port;
    private String directory;

    public Server(Integer port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOGGER.info("Server started with port " + port + " in directory " + directory);
            while (true) {
                var socket = serverSocket.accept();
                var handler = new RequestHandler(socket, directory);
                var handlerThread = new Thread(handler);
                LOGGER.info("Client connected");
                handlerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
