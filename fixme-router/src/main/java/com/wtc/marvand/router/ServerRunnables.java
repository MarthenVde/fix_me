package com.wtc.marvand.router;

import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.SysexMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ServerRunnables {
    private void closeSocketConnection(SocketChannel sc) {
        try {
            sc.close();
            System.out.println("Closing connection ...");
            // System.out.println("Disconnected from " + " ID [" + id + "]");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to close connection");
        }
    }

    private void socketWrite(SocketChannel socket, String message) throws IOException {
        if (socket.isOpen() && socket.isConnected()) {
            ByteBuffer buffer = ByteBuffer.allocate(message.length());
            buffer.wrap(message.getBytes());
            socket.write(buffer);
            // buffer.flip();
            // buffer.clear();
        }
    }

    private boolean validMessage(String message) {
        return true;
    }

    public Runnable handlerRunnable(SocketChannel sc, String incomingMessage, String id, Vector<String> messages) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (sc.isConnected() && sc.isOpen()) {
                    try {
                        if (incomingMessage.equals("newConnection")) {
                            socketWrite(sc, "new_connection_id=" + id);
                            // ByteBuffer buffer = ByteBuffer.allocate(1024);
                            // buffer.wrap("hello new user".getBytes());
                            // sc.write(buffer);
                            // buffer.clear();
                        } else if (validMessage(incomingMessage) == true) {
                            System.out.println("Added message");
                            messages.add(incomingMessage);
                            // for (int i = 0; i < messages.size(); i++) {
                            //     System.out.println(i + " | " + messages.get(i));
                            // }
                        } else {
                            System.out.println("Invalid message received on port: " + sc.socket().getLocalPort());
                        }
                        // Check if new connection
                        // Validate message
                        // Add message
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        closeSocketConnection(sc);
                    }
                }
            }
        };
        return runnable;
    }
}
