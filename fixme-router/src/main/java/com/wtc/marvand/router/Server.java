package com.wtc.marvand.router;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.wtc.marvand.router.ServerRunnables;
import java.util.List;

public class Server implements Runnable {
    private static int idCounter = 99999;
    private Selector selector;
    public final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 2, 0, 
        TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>());
    private Vector<String> messages;
    String id = "";
    int port;
    String componentType = "";
    SocketChannel sc = null;
    
    public Server(int port) {
        this.port = port;
        this.messages = new Vector<String>();

        if (port == 5000) {
            this.componentType = "Broker";
        } else {
            this.componentType = "Market";
        }
    }

    public String getId() {
        return this.id;
    }

    public String popMessage() {
        if (messages.size() > 0) {
            String message = messages.get(0);
            messages.remove(0);
            return message;
        } else {
            return null;
        }
    }

    private int generateNewId() {
        idCounter++;
        if (idCounter < 1000000) {
            return idCounter;
        } else {
            System.out.println("Ran out of unique id's!");
            System.exit(1);
            return -1;
        }
    }
    
    private void acceptConnection(ServerSocketChannel ssc) throws IOException {
        this.sc = ssc.accept();
        this.sc.configureBlocking(false);
        this.sc.register(selector, SelectionKey.OP_READ);
        this.id = "" + this.generateNewId();
        
        System.out.println("Accepted " + this.componentType + " connection... ID [" + this.id + "]");
    }
        
    private void createMessageHandler(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            this.sc.read(buffer);
            String incomingMessage = new String(buffer.array()).trim();
            if (incomingMessage.length() <= 0) {
                throw new IOException();
            } else {
                Runnable runnable = new ServerRunnables().handlerRunnable(this.sc, incomingMessage, id, this.messages);
                threadPoolExecutor.execute(runnable);
            }
        } catch (IOException e) {
            System.out.println(this.componentType + " connection closed on port: " + this.port);
            this.sc.close();
        }
    }

    public SocketChannel getScChannel() {
        return this.sc;
    }

    public void startServer() throws Exception {
        this.selector = Selector.open();
        InetSocketAddress address = new InetSocketAddress("localhost", this.port);
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.configureBlocking(false);
        ssChannel.bind(address);
        ssChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        SelectionKey key = null;

        System.out.println("Server listening on port: " + ssChannel.socket().getLocalPort());
        while (true) {
            if (this.selector.select() > 0) {
                Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    key = (SelectionKey)iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        this.acceptConnection(ssChannel);
                    }
                    if (key.isReadable()) {
                        createMessageHandler(key);
                    }
                }
            }
        }
    }
            // System.out.println(" No market avalaible, please connect a market");
      

    // public static void sendMessage(String message, SocketChannel sc){
    //     try {
    //         if (sc.isOpen() && sc.isConnected()) {
    //             ByteBuffer msgBuffer = ByteBuffer.allocate(message.length());
    //             msgBuffer.wrap(message.getBytes());
    //             sc.write(msgBuffer.wrap(message.getBytes())); 
    //         } else {
    //             System.out.println("Closed connection");
    //         }
    //     } catch (IOException e){
    //         System.out.println(" No market avalaible, please connect a market");
    //     }
    // }

    @Override
    public void run() {
        try {
            this.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}