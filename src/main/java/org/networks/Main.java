package org.networks;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        var port = Integer.parseInt(args[0]);
        var directory = new File("").getAbsolutePath() + "/src/main/resources/shared";
        var server = new Server(port, directory);
        var serverThread = new Thread(server);
        serverThread.start();
    }
}
