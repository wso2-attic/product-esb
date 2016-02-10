package org.wso2.carbon.esb.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleSocketServer extends Thread {
    private int port;
    private String expectedOutput;
    private ServerSocket serverSocket;
    private StringBuffer receivedRequest;
    private static int requestCount;

    public SimpleSocketServer(int port, String expectedOutput) {
        this.port = port;
        this.expectedOutput = expectedOutput;
    }

    public void run() {

        try {
            serverSocket = new ServerSocket(port);
            System.err.println("Server starting on port : " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.err.println("Client connected");
                ++requestCount;
                BufferedReader in =
                                    new BufferedReader(
                                                       new InputStreamReader(
                                                                             clientSocket.getInputStream()));
                BufferedWriter out =
                                     new BufferedWriter(
                                                        new OutputStreamWriter(
                                                                               clientSocket.getOutputStream()));

                while (true) {
                    String s;
                    if ((s = in.readLine()) != null) {
                        System.out.println(s);
                        if (!s.isEmpty()) {
                            if (receivedRequest == null) {
                                receivedRequest = new StringBuffer();
                            }
                            receivedRequest.append(s);
                            continue;
                        }
                    }

                    out.write(expectedOutput);
                    System.err.println("connection terminated");
                    out.close();
                    in.close();
                    clientSocket.close();
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getReceivedRequest() {
        if (receivedRequest == null) {
            return null;
        }
        return receivedRequest.toString();
    }

    public void resetServerDetails() {
        receivedRequest = null;
    }

    public void shutdown() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                System.err.println("Simple socket server shutting down");
                serverSocket.close();
            } catch (IOException e) {
                // NO need to handle
            }
        }
    }

    public static int getRequestCount() {
        return requestCount;
    }

}
