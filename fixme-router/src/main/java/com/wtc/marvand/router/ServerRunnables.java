package com.wtc.marvand.router;

import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.sound.midi.SysexMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import com.wtc.marvand.router.Server;
public class ServerRunnables {
    private static void closeSocketConnection(SocketChannel sc) {
        try {
            sc.close();
            System.out.println("Closing connection on port: " + sc.socket().getLocalPort());
            // System.out.println("Disconnected from " + " ID [" + id + "]");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to close connection");
        }
    }
    
    public static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    public static void sendMessage(String message, SocketChannel sc) {
        try {
            if (sc.isOpen() && sc.isConnected()) {
                ByteBuffer msgBuffer = ByteBuffer.allocate(message.length());
                msgBuffer.wrap(message.getBytes());
                sc.write(msgBuffer.wrap(message.getBytes()));
            } else {
                System.out.println("Closed connection");
            }
        } catch (IOException e) {
            closeSocketConnection(sc);
            // System.out.println(" No market avalaible, please connect a market");
        }
    }

    private boolean validMessage(String message) {
        try {
            // if (Pattern.matches("market_id=\\d+", message)) {
            //     return true;
            // }

            String checksum  = message.split("10=")[1];
            int startIndex = ((message.split("35=")[0]).length());
            int endIndex = message.length() - (checksum.length() + 3);
            String messageBody = message.substring(startIndex, endIndex);
            String newCs = "" + (getCRC32Checksum(messageBody.getBytes()));

            if (checksum.equals(newCs)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public Runnable handlerRunnable(SocketChannel sc, String incomingMessage, String id, Vector<String> messages) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (sc.isConnected() && sc.isOpen()) {
                        if (incomingMessage.equals("newConnection")) {
                                sendMessage("new_connection_id=" + id, sc);
                                } else if (validMessage(incomingMessage) == true) {
                                    messages.add(incomingMessage);
                        } else {
                            System.out.println("Invalid message received on port: " + sc.socket().getLocalPort());
                        }
                    }
            }
        };
        return runnable;
    }
}
