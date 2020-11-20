package com.wtc.marvand.market;

import java.io.BufferedReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.io.*;


public class Market {
    private static final String[] instrumentTypes = {"Violin", "Trumpet", "Piano", "Cello", "Drums", "Bass"};
    private static final int[] instrumentQty = {100, 200, 14, 12, 22, 13};
    private static final float[] minPrice = {10, 20, 35, 11, 5, 6};
    private static final float[] maxPrice = {20, 40, 60, 100, 20, 20};
    private static String id = "";
    public static void main(String[] args) throws Exception {
        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("localhost"), 5001);
        Selector selector = Selector.open();
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.connect(addr);
        sc.register(selector, SelectionKey.OP_CONNECT |
            SelectionKey.OP_READ | SelectionKey.
                OP_WRITE);
        displayInventory();
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

    public static void displayInventory() {
        System.out.println("Current inventory:");
        for (int i = 0; i < instrumentTypes.length; i++) {
            System.out.println("--> [Instrument] : " + instrumentTypes[i] + "  [Qty] : " + instrumentQty[i]);
        }
        System.out.println("--------------------------");
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
                SocketChannel sc = (SocketChannel)key.channel();
                ByteBuffer bb = ByteBuffer.allocate(1024);
                sc.read(bb);
                String response = new String(bb.array()).trim();
    
                if (Pattern.matches("new_connection_id=\\d+", response)) {
                    id = response.split("=")[1];    
                } else {
                    processBrokerRequest(response, sc);
                }
            } catch (IOException e) {
                System.out.println("Connection to router closed!");
                return true;
            }
       }
       return false;
    }

    private static boolean orderInstrument(String instrument, int qty, float price, boolean buy) {
        for (int i = 0; i < instrumentTypes.length; i++) {
            if (instrumentTypes[i].equals(instrument) == true) {
                if (buy == true) {
                    if (instrumentQty[i] - qty < 0) {
                        System.out.println("Not enough stock");
                        return false;
                    } else {
                        if (price >= minPrice[i]) {
                            instrumentQty[i] -= qty;
                            return true;
                        } else {
                            System.out.println("Price too low");
                            return false;
                        }
                    }
                } else {
                    if (price < maxPrice[i]) {
                        instrumentQty[i] += qty;
                        return true;
                    } else {
                        System.out.println("Price too high");
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    private static void sendExecutionResponse(boolean accepted, String brokerId, int orderId, SocketChannel sc) {
        String msgType = "8";
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC);

        int orderStatus = (accepted == true) ? 2 : 8; 

        String body = "35=" + msgType + "|";
        body += "56="+ brokerId + "|";
        body += "49=" + id.trim() + "|";
        body += "39=" + orderStatus + "|";
        body += "52=" + currentTime + "|";
        body += "11=" + orderId + "|";

        int bodyLength = body.getBytes().length;
        String header = "8=FIX.4.4|9=" + bodyLength;

        String message = header + "|" + body + "10=" + getCRC32Checksum(body.getBytes());
        sendMessage(message, sc);
    }

    // 8=FIX.4.4|9=101|35=D|56100000|49=100001|54=1|38=2|40=1|44=12.0|52=2020-11-20T08:29:54.006806Z|INSTRUMENT=Piano|11=1|10=3906825874
    private static void processBrokerRequest(String req, SocketChannel sc) {
        if (req.length() >= 1) {
            try {
                String[] fixReq = req.split("\\|");
                String brokerId = fixReq[4].split("=")[1];
                String instrument = fixReq[10].split("=")[1];
                float price = Float.parseFloat(fixReq[8].split("=")[1]);
                int qty = Integer.parseInt(fixReq[6].split("=")[1]);
                int orderType = Integer.parseInt(fixReq[5].split("=")[1]);
                int orderId = Integer.parseInt(fixReq[11].split("=")[1]);
                boolean buy = (orderType == 1) ? true : false;
                boolean orderAccepted = orderInstrument(instrument, qty, price, buy);
                
                if (orderAccepted) {
                    System.out.println("Order accepted!");
                } else {
                    System.out.println("Order rejected!");
                }

                displayInventory();
                
                sendExecutionResponse(orderAccepted, brokerId, orderId, sc);
            } catch (Exception e) {
                System.out.print("Unable to process order ...");
                e.printStackTrace();
            }
        } else {
            closeSocketConnection(sc);
            System.exit(1);
        }
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
