package com.wtc.marvand.market;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
public class Market {
   private static BufferedReader input = null;

    //Create selector
    //Open Socket channel
    // Set blocking to false
    // Connect sc to address
    //  
   public static void main(String[] args) throws Exception {
      InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("localhost"), 5001);
      Selector selector = Selector.open();
      SocketChannel sc = SocketChannel.open();
      sc.configureBlocking(false);
      sc.connect(addr);
      sc.register(selector, SelectionKey.OP_CONNECT |
         SelectionKey.OP_READ | SelectionKey.
            OP_WRITE);
      input = new BufferedReader(new
         InputStreamReader(System.in));
      while (true) {
         if (selector.select() > 0) {
            Boolean doneStatus = processReadySet
               (selector.selectedKeys());
            if (doneStatus) {
               break;
            }
         }
      }
      sc.close();
   }
   public static Boolean processReadySet(Set readySet)
         throws Exception {
      SelectionKey key = null;
      Iterator iterator = null;
      iterator = readySet.iterator();
      while (iterator.hasNext()) {
         key = (SelectionKey) iterator.next();
         iterator.remove();
      }
      if (key.isConnectable()) {
         Boolean connected = processConnect(key);
         if (!connected) {
            return true;
         }
      }
      if (key.isReadable()) {
         SocketChannel sc = (SocketChannel) key.channel();
         ByteBuffer bb = ByteBuffer.allocate(1024);
         sc.read(bb);
         String result = new String(bb.array()).trim();
         System.out.println("Message received from Server: " + result + " Message length= "
            + result.length());
      }
      if (key.isWritable()) {
         System.out.print("Type a message (type quit to stop): ");
         String msg = input.readLine();
         if (msg.equalsIgnoreCase("quit")) {
            return true;
         }
         SocketChannel sc = (SocketChannel) key.channel();
         ByteBuffer bb = ByteBuffer.wrap(msg.getBytes());
         sc.write(bb);
      }
      return false;
   }
   public static Boolean processConnect(SelectionKey key) {
      SocketChannel sc = (SocketChannel) key.channel();
      try {
         while (sc.isConnectionPending()) {
            sc.finishConnect();
         }
      } catch (IOException e) {
         key.cancel();
         e.printStackTrace();
         return false;
      }
      return true;
   }
}




// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.concurrent.Future;
 
// public class Core {
 
//     private static ExecutorService executor = null;
//     private static volatile Future taskOneResults = null;
//     private static volatile Future taskTwoResults = null;
 
//     public static void main(String[] args) {
//         executor = Executors.newFixedThreadPool(2);
//         while (true)
//         {
//             try
//             {
//                 checkTasks();
//                 Thread.sleep(1000);
//             } catch (Exception e) {
//                 System.err.println("Caught exception: " + e.getMessage());
//             }
//         }
//     }
 
//     private static void checkTasks() throws Exception {
//         if (taskOneResults == null
//                 || taskOneResults.isDone()
//                 || taskOneResults.isCancelled())
//         {
//             taskOneResults = executor.submit(new TestOne());
//         }
 
//         if (taskTwoResults == null
//                 || taskTwoResults.isDone()
//                 || taskTwoResults.isCancelled())
//         {
//             taskTwoResults = executor.submit(new TestTwo());
//         }
//     }
// }
 
// class TestOne implements Runnable {
//     public void run() {
//         while (true)
//         {
//             System.out.println("Executing task one");
//             try
//             {
//                 Thread.sleep(1000);
//             } catch (Throwable e) {
//                 e.printStackTrace();
//             }
//         }
 
//     }
// }
 
// class TestTwo implements Runnable {
//     public void run() {
//         while (true)
//         {
//             System.out.println("Executing task two");
//             try
//             {
//                 Thread.sleep(1000);
//             } catch (Throwable e) {
//                 e.printStackTrace();
//             }
//         }
//     }
// }
