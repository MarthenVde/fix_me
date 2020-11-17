package com.wtc.marvand.router;

import java.net.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.rmi.server.ServerNotActiveException;
import java.util.*;
public class Router {
    public static void main(String[] args) {
        // Catch creating socket on same port exception
        Server brokerServer = new Server(5000);
        Thread brokerThread = new Thread(brokerServer);
        // Thread marketServer = new Thread(new Server(5001));
        // Server server = new Server(5000, 5001);
        // server.startServer();

        brokerThread.start();
        // marketServer.start();

        while (true) {
            // System.out.println("looking for mesasge");
            String message = brokerServer.popMessage();

            if (message != null) {
                System.out.println(message);
            }
            // Get broker requests
            // send broker requests to market
            // get market messages
            // send messages to broker
        }
    }
}

