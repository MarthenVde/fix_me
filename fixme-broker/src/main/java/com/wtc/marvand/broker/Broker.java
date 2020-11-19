package com.wtc.marvand.broker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Broker {
    private static Scanner in = new Scanner(System.in);
    private static BufferedReader input = null;
    private static final String[] instrumentTypes = {"Violin", "Trumpet", "Piano", "Cello", "Drums", "Bass"};
    private static final int[] instrumentQty = {10, 10, 10, 10, 10, 10};
    private static String id = "";
    private static Vector<String> orders = new Vector<String>();
    private static int orderId = 0;
    // private static String marketId = "";

    public static void main(String[] args) throws Exception {
        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("localhost"), 5000);
        Selector selector = Selector.open();
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.connect(addr);
        sc.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        input = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            if (selector.select() > 0) {
                Boolean doneStatus = processReadySet(selector.selectedKeys());
                if (doneStatus) {
                    break;
                }
            }
        }
        sc.close();
    }

    public static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    private static int nextOrderId() {
        return orderId++;
    }

    private static String generateFixMessage(int orderType, String instrument, int qty, float price) {
        String msgType = "D";
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC);

        String body = "35=" + msgType + "|";
        body += "56=100001|";
        body += "49=" + id.trim() + "|";
        body += "54=" + orderType + "|";
        body += "38=" + qty + "|";
        body += "40=1|";
        body += "44=" + price + "|";
        body += "52=" + currentTime + "|";
        body += "INSTRUMENT=" + instrument + "|";
        body += "11=" + nextOrderId() + "|";

        int bodyLength = body.getBytes().length;
        String header = "8=FIX.4.4|9=" + bodyLength;
        String message = header + "|" + body + "10=" + getCRC32Checksum(body.getBytes());
        return message;
    }

    public static Boolean processReadySet(Set readySet) throws Exception {
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
            try {
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer bb = ByteBuffer.allocate(1024);
                sc.read(bb);
                String response = new String(bb.array()).trim();

                if (Pattern.matches("new_connection_id=\\d+", response)) {
                    id = response.split("=")[1];
                    System.out.println("Connected to router with id: " + id);
                } else {
                    System.out.println("Market sent message: " + response);
                }
                sendBuyOrder(sc);
            } catch (IOException e) {
                System.out.println("Connection to router closed!");
                return true;
            }
        }
        return false;
    }

    private static void listInstruments() {
        System.out.println("Available Instruments:");
        for (int i = 0; i < instrumentTypes.length; i++) {
            System.out.println("[" + i + "] - " + instrumentTypes[i]);
        }
    }

    public static void sendBuyOrder(SocketChannel sc) {
            boolean buy = false;
            int selIndex = 0;
            int qty = 0;
            float price = 0;
            int buySell = 2;

            while (true) {
                System.out.println("Buy or sell? (b/s): ");
                String resp = in.nextLine();

                if (resp.equalsIgnoreCase("buy") || resp.equalsIgnoreCase("b")) {
                    buySell = 1;
                    break;
                }
                if (resp.equalsIgnoreCase("sell") || resp.equalsIgnoreCase("s"))
                    break;
            }

            listInstruments();

            while (true) {
                System.out.println("Select and instrument type [0 - " + (instrumentTypes.length - 1) + "] :");
                try {
                    int selResp = Integer.parseInt(in.nextLine());
                    if ((selResp >= 0) && (selResp < instrumentTypes.length)) {
                        selIndex = selResp;
                        break;
                    } else {
                        System.out.println("Out of selection range");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input");
                }
            }

            while (true) {
                int maxQty = 100;

                if (buy == false) {
                    maxQty = instrumentQty[selIndex];
                }

                System.out.println("Choose the quantity [1 - " + maxQty + "]");
                try {
                    int qtyResp = Integer.parseInt(in.nextLine());
                    if ((qtyResp >= 1) && (qtyResp <= maxQty)) {
                        qty = qtyResp;
                        break;
                    } else {
                        System.out.println("Quantity out of range");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input");
                }
            }

            while (true) {
                System.out.println("Choose a price type [1 - 2000]");
                try {
                    float priceResp = Float.parseFloat(in.nextLine());
                    if ((priceResp >= 1) && (priceResp <= 2000)) {
                        price = priceResp;
                        break;
                    } else {
                        System.out.println("Price out of range");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input");
                }
            }
            String fixMsg = generateFixMessage(buySell, instrumentTypes[selIndex], qty, price);
            sendMessage(fixMsg, sc);
            orders.add(fixMsg);
            System.out.println("Order sent...");
    }

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

    public static Boolean processConnect(SelectionKey key) {
       SocketChannel sc = (SocketChannel) key.channel();
       try {
          while (sc.isConnectionPending()) {
             sc.finishConnect();
          }
          sendMessage("newConnection", sc);
       } catch (IOException e) {
          key.cancel();
          e.printStackTrace();
          return false;
       }
       return true;
    }
 }
 