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
    
    public Server(int port) {
        this.port = port;
        this.messages = new Vector<String>();

        if (port == 5000) {
            this.componentType = "Broker";
        } else {
            this.componentType = "Market";
        }
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
        SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
        this.id = "" + this.generateNewId();
        
        System.out.println("Accepted " + this.componentType + " connection... ID [" + this.id + "]");
    }
        
    private void createMessageHandler(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel)key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        sc.read(buffer);
        String incomingMessage = new String(buffer.array()).trim();

        int incomingPort = sc.socket().getLocalPort();

        if (incomingMessage.length() <= 0) {
            System.out.println(this.componentType + " connection closed on port: " + incomingPort);
            sc.close();
        } else {
            Runnable runnable = new ServerRunnables().handlerRunnable(sc, incomingMessage, id, this.messages);
            threadPoolExecutor.execute(runnable);
        }
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
                        // create new message handler
                        // Start new message handler
                        createMessageHandler(key);
                        // this.handleReadOp(key);
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            this.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}