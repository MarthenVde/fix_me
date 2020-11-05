package com.wtc.marvand.broker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Broker {
    public static void main( String[] args )
    {
        int portNumber = 5001;
        String hostname = "127.0.0.1";
        Socket clientSocket;

        PrintWriter out;
        BufferedReader in;
        InputStreamReader ir;

        try {
            clientSocket = new Socket(hostname, portNumber);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            ir = new InputStreamReader(clientSocket.getInputStream());
            in = new BufferedReader(ir);
            out.println("Broker says hi!!!");
            System.out.println("Server says: " + in.readLine());
        } catch (UnknownHostException e) {
            System.exit(1);
        } catch (IOException e) {
            System.exit(1);
        }
    }
}
