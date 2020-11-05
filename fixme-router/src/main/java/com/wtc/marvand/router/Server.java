package com.wtc.marvand.router;

// import com.wtc.marvand.router.ServerRunnable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private static int idCounter = 99999;
    private Selector selector;
    int brokerPort = 5000;
    int marketPort = 5001;

    public Server(int mPort, int bPort) {
    }

    private void initSSChannel(int port) {
        try {
            InetSocketAddress address = new InetSocketAddress("localhost", port);
            ServerSocketChannel ssChannel = ServerSocketChannel.open();
            ssChannel.configureBlocking(false);
            ssChannel.bind(address);
            ssChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void startServer() {
        try {
            this.selector = Selector.open();
            initSSChannel(this.brokerPort);
            initSSChannel(this.marketPort);

            while (true) {
                if (this.selector.select() <= 0)
                    continue;
                else {
                    System.out.println("Selector has message");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // public void createServerSocketChannel(int port) throws Exception {
    //     ServerSocketChannel ssChannel = ServerSocketChannel.open();
    //     ssChannel.configureBlocking(false);
    //     ssChannel.bind(new InetSocketAddress(port));
    //     ssChannel.register(this.selector, SelectionKey.OP_ACCEPT);
    // }

    // public void runServer() {
    //     try {
    //         this.selector = Selector.open();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
        
    // }

    // private int generateNewId() {
    //     idCounter++;
    //     if (idCounter < 1000000) {
    //         return idCounter;
    //     } else {
    //         System.out.println("Ran out of unique id's!");
    //         System.exit(1);
    //     }
    // }

    // private void acceptConnection(ServerSocketChannel ssc) {

    // }

    // private void createServer() throws Exception {
    //     InetAddress host = InetAddress.getByName("localhost");
    //     this.selector = Selector.open();
    //     ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    //     serverSocketChannel.configureBlocking(false);
    //     serverSocketChannel.bind(new InetSocketAddress(host, this.port));
    //     serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    //     SelectionKey key = null;
    //     System.out.println("Waiting for connection on port: " + this.port);
    //     while (true) {
    //         if (selector.select() <= 0)
    //             continue;
    //         Set<SelectionKey> selectedKeys = selector.selectedKeys();
    //         Iterator<SelectionKey> iterator = selectedKeys.iterator();
    //         while (iterator.hasNext()) {
    //             key = (SelectionKey) iterator.next();
    //             iterator.remove();
    //             if (key.isAcceptable()) {
    //                 SocketChannel sc = serverSocketChannel.accept();
    //                 sc.configureBlocking(false);
    //                 sc.register(selector, SelectionKey.OP_READ);
    //                 System.out.println("Connection Accepted: " + sc.getLocalAddress() + "\n");
    //             }
    //             if (key.isReadable()) {
    //                 SocketChannel sc = (SocketChannel) key.channel();
    //                 ByteBuffer bb = ByteBuffer.allocate(1024);
    //                 sc.read(bb);
    //                 String result = new String(bb.array()).trim();
    //                 System.out.println("Message received: " + result + " Message length= " + result.length());
    //                 if (result.length() <= 0) {
    //                     sc.close();
    //                     System.out.println("Connection closed...");
    //                     System.out.println("Server will keep running. " + "Try running another client to "
    //                             + "re-establish connection");
    //                 }
    //             }
    //         }
    //     }

    //     // public static void main(String[] args) throws Exception {

    //     // }
    // }

    // @Override
    // public void run() {
    //     // TODO Auto-generated method stub
    //     try {
    //         this.createServer();
    //     } catch (Exception e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }
    // }
//     public void runServer() {
//         InetAddress hostIPAddress = InetAddress.getByName("localhost");
//         int port = 19000;
//         Selector selector = Selector.open();
//         ServerSocketChannel ssChannel = ServerSocketChannel.open();
//         ssChannel.configureBlocking(false);
//         ssChannel.socket().bind(new InetSocketAddress(hostIPAddress, port));
//         ssChannel.register(selector, SelectionKey.OP_ACCEPT);
//         while (true) {
//           if (selector.select() <= 0) {
//             continue;
//           }
//           processReadySet(selector.selectedKeys());
//         }
//         // try {
//             //     port = 5001;
//         //     // server.bind(new InetSocketAddress("127.0.0.1", portNumber));
//         //     // Future<AsynchronousServerSocketChannel> acceptFuture = server.accept();
//         //     // AsynchronousSocketChannel worker = acceptFuture.get(10, TimeUnit.SECONDS);
//         // } catch (IOException e) {
//         //     System.err.println(e.getMessage());

//         // }
        
//         // try {
//         //     this.serverSocket = new ServerSocket(this.portNumber);
//         //     this.serverSocket.setSoTimeout(50000);
//         //     System.out.println("Starting Server Socket on port: " +  this.serverSocket.getLocalPort());
//         // } catch (IOException e) {
//         //     System.err.println("Could not run server!");
//         //     System.err.println(e.getMessage());
//         // }

//         // while (true) {
//         //     try {
//         //         Socket clientSocket = serverSocket.accept();
//         //         // Start new thread
//         //         ServerRunnable mr = new ServerRunnable(clientSocket);
//         //         new Thread(mr).start();
//         //     } catch (IOException e) {
//         //         System.err.println(e.getMessage());
//         //     }
//         // }
//     }
}