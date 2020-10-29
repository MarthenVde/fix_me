package com.wtc.marvand.router;

import java.io.IOException;
import java.net.ServerSocket;
public class Router extends Thread {
    int portNumber = 5001;
    ServerSocket serverSocket = null;

    public void runServer() {
        try {
            this.serverSocket = new ServerSocket(this.portNumber);
            this.serverSocket.setSoTimeout(50000);
            System.out.println("Starting Server Socket on port: " +  this.serverSocket.getLocalPort());
        } catch (IOException e) {
            System.err.println("Could not run server!");
            System.err.println(e.getMessage());
        }

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                // Start new thread
                MarketRunnable mr = MarketRunnable(clientSocket); 
                new Thread(mr).start();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
