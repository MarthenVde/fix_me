package com.wtc.marvand.router;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.*;
import java.io.*;

/**
 * Hello world!
 *
 */
public class Main {
    // private ServerSocket serverSocket;
   
    // public Main(int port) throws IOException {
    //    marketSocket = new ServerSocket(port);
    //    marketSocket.setSoTimeout(10000);

    //    brokerSocket = new ServerSocket(port);
    //    brokerSocket.setSoTimeout(10000);
    // }
 
    // public void run() {
    //    while(true) {
    //       try {
    //         System.out.println("Waiting for client on port " + 
    //         serverSocket.getLocalPort() + "...");
    //         Socket server = serverSocket.accept();
             
    //         System.out.println("Just connected to " + server.getRemoteSocketAddress());
    //         DataInputStream in = new DataInputStream(server.getInputStream());
             
    //         System.out.println(in.readUTF());
    //         DataOutputStream out = new DataOutputStream(server.getOutputStream());
    //         out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress()
    //             + "\nGoodbye!");
    //         server.close(); 
    //       } catch (SocketTimeoutException s) {
    //          System.out.println("Socket timed out!");
    //          break;
    //       } catch (IOException e) {
    //          e.printStackTrace();
    //          break;
    //       }
    //    }
    // }
    
    public static void main(String [] args) {
        Router router = new Router();
        router.runServer();
    }

    // public static void main( String[] args )
    // {
    //     // String serverName = "localhost";
    //     // int port = 5001;
    //     // try {
    //     //    System.out.println("Connecting to " + serverName + " on port " + port);
    //     //    Socket client = new Socket(serverName, port);
           
    //     //    System.out.println("Just connected to " + client.getRemoteSocketAddress());
    //     //    OutputStream outToServer = client.getOutputStream();
    //     //    DataOutputStream out = new DataOutputStream(outToServer);
           
    //     //    out.writeUTF("Hello from " + client.getLocalSocketAddress());
    //     //    InputStream inFromServer = client.getInputStream();
    //     //    DataInputStream in = new DataInputStream(inFromServer);
           
    //     //    System.out.println("Server says " + in.readUTF());
    //     //    client.close();
    //     // } catch (IOException e) {
    //     //    e.printStackTrace();
    //     // }
    // }
}
