package com.wtc.marvand.market;

import java.net.Socket;
import java.io.*;


public class MarketRunnable implements Runnable {
    protected Socket clientSocket = null;

    public MarketRunnable (Socket clientSocket) {
        this.clientSocket = clientSocket; 
    }

    public void run () {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String arg1 = in.readline();
            System.out.println("Client says: " + arg1);
            out.println("Thanks for your message!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}