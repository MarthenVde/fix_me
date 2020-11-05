package com.wtc.marvand.router;

import java.net.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
public class Router {
    public static void main(String[] args) {
        // Catch creating socket on same port exception
        // Thread brokerServer = new Thread(new Server(5000));
        // Thread marketServer = new Thread(new Server(5001));
        Server server = new Server(5000, 5001);
        server.startServer();

        // brokerServer.start();
        // marketServer.start();
    }
    //  Create and open and selector 
    //  Create server socket channel and set to non-blocking
    //  Bind ssc to InetAdress
    //  Register selector to ssc  
}

