package com.wtc.marvand.router;

import java.net.Socket;
import java.io.*;
import java.io.BufferedReader;

public class ServerRunnable implements Runnable {
    protected Socket clientSocket = null;

    public ServerRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket; 
    }

    @Override
    public void run () {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String arg1 = in.readLine();
            System.out.println("Client says: " + arg1);
            out.println("Thanks for your message!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}