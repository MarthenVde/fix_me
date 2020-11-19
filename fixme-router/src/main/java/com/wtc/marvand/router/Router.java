package com.wtc.marvand.router;

import java.net.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.rmi.server.ServerNotActiveException;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class Router {
    public static void main(String[] args) throws InterruptedException {
        Server brokerServer = new Server(5000);
        Thread brokerThread = new Thread(brokerServer);

        Server marketServer = new Server(5001);
        Thread marketThread = new Thread(marketServer);

        brokerThread.start();
        marketThread.start();

        while (true) {
            // String marketId = marketServer.getId();
            // System.out.println("looking for mesasge");
            String brokerRequest = brokerServer.popMessage();
            
            if (brokerRequest != null) {
                if (marketServer.getScChannel() == null) {
                    while (marketServer.getScChannel() == null) {
                        System.out.println("Waiting for market connection ...");
                        TimeUnit.SECONDS.sleep(1);
                        // System.out.println((marketServer.getScChannel() == null));
                    }
                }
                String[] fixMsg = brokerRequest.split("\\|");
                fixMsg[3] = "56" + marketServer.getId();
                brokerRequest = String.join("|", fixMsg);
                System.out.println("Broker message received ID[" + brokerServer.id + "] : " + brokerRequest);
                ServerRunnables.sendMessage(brokerRequest, marketServer.getScChannel());
                // Replace market id
                // ServerRunnables.sendMessage("market_id=" + marketId, brokerServer.getScChannel());
            }
            
            
            String marketResponse = marketServer.popMessage();
            
            if (marketResponse != null) {
                ServerRunnables.sendMessage(marketResponse, brokerServer.getScChannel());
                System.out.println("Market message received ID[" + marketServer.id + "] : " + marketResponse);
            }
        }
    }
}

