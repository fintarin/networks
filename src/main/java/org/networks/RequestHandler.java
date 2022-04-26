package org.networks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RequestHandler implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(RequestHandler.class);
    private static final String CURRENT_PATH = new File("").getAbsolutePath();

    private Socket socket;
    private String directory;

    private BufferedReader in;
    private PrintWriter out;
    private OutputStream outData;

    RequestHandler(Socket socket, String directory) throws IOException {
        this.directory = directory;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            outData = socket.getOutputStream();

            var inStr = new String();
            inStr = in.readLine();
            if (inStr == null) {
                LOGGER.warn("Request is empty");
                return;
            }
            LOGGER.info("Request is: " + inStr);

            var reqTokens = new StringTokenizer(inStr);
            var reqType = RequestType.of(reqTokens.nextToken());
            if (reqType.isPresent()) {
                switch (reqType.get()) {
                    case GET: {
                        executeGet(reqTokens.nextToken());
                        break;
                    }
                    case POST: {
                        executePost();
                        break;
                    }
                    case OPTIONS: {
                        executeOptions();
                        break;
                    }
                    default: {
                        executeUnknown();
                    }
                }
            } else {
                executeUnknown();
            }
        } catch (IOException e) {
            LOGGER.error(e);
        } finally {
            try {
                in.close();
                out.close();
                outData.close();
                socket.close();
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
    }

    private void executeGet(String filename) throws IOException {
        LOGGER.info("GET request accepted");

        var path = Path.of(directory + filename);
        if (!Files.exists(path)) {
            sendHeader(HttpCode.NOT_FOUND, RequestType.GET);
            sendFile(Path.of(CURRENT_PATH + "/src/main/resources/404.html"), "404.html");
            LOGGER.info("GET Request Failed");
            return;
        }

        sendHeader(HttpCode.OK, RequestType.GET);
        if (!Files.isDirectory(path)) {
            sendFile(path, filename);
        } else {
            sendDirectory(path, filename);
        }

        LOGGER.info("GET request executed");
    }

    private void executePost() {
        LOGGER.info("POST request accepted");
        sendHeader(HttpCode.OK, RequestType.POST);
        LOGGER.info("POST request executed");
    }

    private void executeOptions() {
        LOGGER.info("OPTIONS request accepted");
        sendHeader(HttpCode.OK, RequestType.OPTIONS);
        out.println("Access-Control-Methods: " + "GET, POST, OPTIONS");
        LOGGER.info("OPTIONS request executed");
    }

    private void executeUnknown() throws IOException {
        LOGGER.warn("Request is unknown");
        sendHeader(HttpCode.NOT_IMPLEMENTED);
        sendFile(Path.of(CURRENT_PATH + "/src/main/resources/501.html"), "501.html");
    }

    private void sendFile(Path path, String filename) throws IOException {
        var content = ContentType.of(filename);
        var data = Files.readAllBytes(path);
        out.println("Content-type: " + content.getType());
        out.println("Content-length: " + data.length);
        out.println();
        outData.write(data);
    }

    private void sendDirectory(Path path, String dirname) throws IOException {
        var res = new StringBuilder();
        res.append("<!DOCTYPE html>\n");
        res.append("<html lang=\"en\">\n");
        res.append("<head>\n");
        res.append("<meta charset=\"UTF-8\">\n");
        res.append("<title>Files List</title>\n");
        res.append("</head>\n");
        res.append("<body>\n");
        appendFilesList(path, dirname, res);
        res.append("</body>\n");
        res.append("</html>\n");

        var data = res.toString().getBytes();
        out.println("Content-type: " + ContentType.HTML.getType());
        out.println("Content-length: " + data.length);
        out.println();
        outData.write(data);
    }

    private void appendFilesList(Path path, String dirname, StringBuilder res) {
        var folder = new File(path.toString());
        var filesList = folder.listFiles();
        for (var file : filesList) {
            var slash = file.isDirectory() ? "/" : "";
            res.append("<a href = \"./" + file.getName() + slash + "\">" + file.getName() + "</a><br>\n");
        }
    }

    private void sendHeader(HttpCode code) {
        out.println("HTTP/1.1 " + code.getCode() + " " + code.getDescription());
        out.println("Server: Networks Lab Server");
        out.println("Date: " + LocalDate.now());
        out.println("Access-Control-Allow-Origin: " + socket.getLocalAddress().getHostAddress());
    }

    private void sendHeader(HttpCode code, RequestType type) {
        sendHeader(code);
        out.println("Access-Control-Request-Method: " + type.getName());
    }
}
