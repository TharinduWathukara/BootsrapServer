/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;

// import javafx.scene.effect.Light.Spot;

/**
 *
 * @author Tharindu Wathukara
 */
public class Node {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        DatagramSocket sock = null;
        InetAddress ip;
        int port = Integer.parseInt(args[0]);
        int masterPort = 55555;
        String username = args[1];
        List<Neighbour> nodes = new ArrayList<Neighbour>();
        String s;
        List<String> fileNames = new ArrayList<String>();

        echo("-----RANDOM FILES GENERATING-----");
        generateRandomFiles(fileNames);
        echo(fileNames.toString());

        try {
            ip = InetAddress.getByName("localhost");
            sock = new DatagramSocket(port);

            echo("-----Try to register at Bootstrap server-----");

            String req = " REG " + ip.getHostAddress() + " " + port + " " + username;
            req = String.format("%04d", req.length() + 5) + " " + req;

            DatagramPacket request = new DatagramPacket(req.getBytes(), req.getBytes().length, ip, masterPort);
            sock.send(request);

            while (true) {
                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                sock.receive(incoming);

                byte[] data = incoming.getData();
                s = new String(data, 0, incoming.getLength());

                // echo the details of incoming data - client ip : client port - client message
                echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);

                StringTokenizer st = new StringTokenizer(s, " ");

                String length = st.nextToken();
                String command = st.nextToken();

                if (command.equals("REGOK")) {
                    String errorCode = st.nextToken();
                    if (errorCode.equals("9999")) {
                        echo("REGISTER FAIL :: there is some error in the command");
                        break;
                    } else if (errorCode.equals("9998")) {
                        echo("REGISTER FAIL :: already registered to you, unregister first");
                        break;
                    } else if (errorCode.equals("9997")) {
                        echo("REGISTER FAIL :: registered to another user, try a different IP and port");
                        break;
                    } else if (errorCode.equals("9996")) {
                        echo("REGISTER FAIL :: can’t register. BS full");
                        break;
                    } else {
                        echo("-----REGISTER SUCCESSFULL-----");
                        for (int i = 0; i < Integer.parseInt(errorCode); i++) {
                            String neighborIp = st.nextToken();
                            int neighborPort = Integer.parseInt(st.nextToken());

                            // send join request to neighbor node
                            String joinReq = " JOIN " + ip.getHostAddress() + " " + port;
                            joinReq = String.format("%04d", joinReq.length() + 5) + " " + joinReq;

                            DatagramPacket joinRequest = new DatagramPacket(joinReq.getBytes(),
                                    joinReq.getBytes().length, InetAddress.getByName(neighborIp), neighborPort);
                            sock.send(joinRequest);
                        }

                        echo(username + " created at port " + port + ". Waiting for incoming data...");

                    }

                } else if (command.equals("JOIN")) {
                    String neighborIp = st.nextToken();
                    int neighborPort = Integer.parseInt(st.nextToken());
                    nodes.add(new Neighbour(neighborIp, neighborPort));
                    echo("Number of neighbours : " + nodes.size());

                    String reply = "0012 JOINOK 0";
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                            incoming.getAddress(), incoming.getPort());
                    sock.send(dpReply);

                } else if (command.equals("JOINOK")) {
                    nodes.add(new Neighbour(incoming.getAddress().getHostAddress(), incoming.getPort()));
                    echo("" + nodes.size());

                } else if (command.equals("UNREGNODE")) {
                    String reqest = " UNREG " + ip.getHostAddress() + " " + port + " " + username;
                    reqest = String.format("%04d", reqest.length() + 5) + " " + reqest;

                    DatagramPacket dpRequest = new DatagramPacket(reqest.getBytes(), reqest.getBytes().length, ip,
                            masterPort);
                    sock.send(dpRequest);

                } else if (command.equals("UNROK")) {
                    for (int i = 0; i < nodes.size(); i++) {
                        String leaveReq = " LEAVE " + ip.getHostAddress() + " " + port;
                        leaveReq = String.format("%04d", leaveReq.length() + 5) + " " + leaveReq;
                        DatagramPacket leaveRequest = new DatagramPacket(leaveReq.getBytes(),
                                leaveReq.getBytes().length, InetAddress.getByName(nodes.get(i).getIp()),
                                nodes.get(i).getPort());
                        sock.send(leaveRequest);
                    }
                    nodes.clear();
                    echo("-----LEAVE-----");
                    if (nodes.size() == 0) {
                        break;
                    }

                } else if (command.equals("LEAVE")) {
                    String ipleave = st.nextToken();
                    int portleave = Integer.parseInt(st.nextToken());
                    for (int i = 0; i < nodes.size(); i++) {
                        if (nodes.get(i).getPort() == portleave) {
                            nodes.remove(i);
                            String reply = "0014 LEAVEOK 0";
                            DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                                    incoming.getAddress(), incoming.getPort());
                            sock.send(dpReply);
                        }
                    }
                    echo("Number of neighbours : " + nodes.size());

                } else if (command.equals("LEAVEOK")) {
                    echo("-----LEAVE-----");
                    if (nodes.size() == 0) {
                        break;
                    }

                } else if (command.equals("SEARCH")) {
                    StringTokenizer stq = new StringTokenizer(s, "\"");
                    String query = stq.nextToken();
                    query = stq.nextToken();
                    String last = stq.nextToken();
                    int hops = Integer.parseInt(last.substring(1, last.length()-1));
                    if (hops > 0) {
                        String files = "";
                        int fileCount = 0;

                        for (int i = 0; i < fileNames.size(); i++) {
                            String fileName = fileNames.get(i);
                            if (queryMatch(query, fileName)) {
                                files += "\"" + fileNames.get(i) + ".txt\" ";
                                fileCount += 1;
                            }
                        }

                        if (fileCount > 0) {
                            echo(fileCount + " files found for query - " + query);
                            echo("Files are - " + files);

                            // String reply = " SEROK " + fileCount + " " + ip + " " + port + " " + files;
                            // reply = String.format("%04d", reply.length() + 5) + " " + reply;
                            // DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, 
                            //         incoming.getAddress(), incoming.getPort());
                            // sock.send(dpReply);

                        } else if ((hops - 1) > 0) {
                            echo("No files found for query - " + query);
                            echo("Send search requests for neighbor nodes");

                            String serReq = " SER " + ip.getHostAddress() + " " + port
                                    + " \"" + query + "\" " + (hops - 1);
                            serReq = String.format("%04d", serReq.length() + 5) + " " + serReq;

                            for (int i = 0; i < nodes.size(); i++) {
                                DatagramPacket serRequest = new DatagramPacket(serReq.getBytes(),
                                        serReq.getBytes().length, InetAddress.getByName(nodes.get(i).getIp()),
                                        nodes.get(i).getPort());
                                sock.send(serRequest);
                            }
                        } else {
                            echo("No files found for query - " + query);

                        }
                    }

                } else if (command.equals("SER")) {
                    String nodeIp = st.nextToken();
                    int nodePort = Integer.parseInt(st.nextToken());

                    StringTokenizer stq = new StringTokenizer(s, "\"");
                    String query = stq.nextToken();
                    query = stq.nextToken();
                    String last = stq.nextToken();
                    int hops = Integer.parseInt(last.substring(1));

                    if (hops > 0) {
                        String files = "";
                        int fileCount = 0;

                        for (int i = 0; i < fileNames.size(); i++) {
                            String fileName = fileNames.get(i);
                            if (queryMatch(query, fileName)) {
                                files += "\"" + fileNames.get(i) + ".txt\" ";
                                fileCount += 1;
                            }
                        }

                        if (fileCount > 0) {
                            echo(fileCount + " files found for query - " + query);
                            echo("Files are - " + files);

                            String reply = " SEROK " + fileCount + " " + ip.getHostAddress() + " " + port + " " + files;
                            reply = String.format("%04d", reply.length() + 5) + " " + reply;
                            DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                                    InetAddress.getByName(nodeIp), nodePort);
                            sock.send(dpReply);

                        } else if ((hops - 1) > 0) {
                            echo("No files found for query - " + query);
                            echo("Send search requests for neighbor nodes");

                            String serReq = " SER " + nodeIp + " " + nodePort
                                    + " \"" + query + "\" " + (hops - 1);
                            serReq = String.format("%04d", serReq.length() + 5) + " " + serReq;

                            for (int i = 0; i < nodes.size(); i++) {
                                if (nodes.get(i).getPort() != incoming.getPort()) {
                                    DatagramPacket serRequest = new DatagramPacket(serReq.getBytes(),
                                            serReq.getBytes().length, InetAddress.getByName(nodes.get(i).getIp()),
                                            nodes.get(i).getPort());
                                    sock.send(serRequest);
                                }
                            }
                        } else {
                            echo("No files found for query - " + query);

                        }
                    }

                } else if (command.equals("SEROK")) {
                    int fileCount = Integer.parseInt(st.nextToken());
                    String nodeIp = st.nextToken();
                    int nodePort = Integer.parseInt(st.nextToken());

                    echo("Matching files found in IP " + nodeIp + " port " + nodePort);
                    StringTokenizer stq = new StringTokenizer(s, "\"");
                    stq.nextToken();
                    String files = "";
                    for (int i = 0; i < fileCount; i++) {
                        files += stq.nextToken();
                        files += stq.nextToken();
                    }
                    echo("Files : " + files);

                } else if (command.equals("DOWNLOAD")) {
                    String nodeIp = st.nextToken();
                    int nodePort = Integer.parseInt(st.nextToken());

                    StringTokenizer stq = new StringTokenizer(s, "\"");
                    String file = stq.nextToken();
                    file = stq.nextToken();

                    if(ip.getHostAddress().equals(nodeIp) && nodePort==port && fileNames.contains(file.split("\\.")[0])){
                        
                        // random file generate here
                        Random r = new Random();
                        int x = r.nextInt(9) + 2;
                        byte[] bytes = new byte[x * 1024 * 1024];
                        r.nextBytes(bytes);

                        try {
                            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                            byte[] hash = messageDigest.digest(bytes);

                            StringBuilder sb = new StringBuilder(2 * hash.length);
                            for (byte b : hash) {
                                sb.append(String.format("%02x", b & 0xff));
                            }
                            String digest = sb.toString();

                            echo("File : " + file + "   File Size : " + x + "MB   Hash : " + digest);

                        } catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        // save file in local storage
                        FileOutputStream fs = new FileOutputStream(
                                "./Files/download_" + file.split("\\.")[0] + " " + new Date().getTime() + ".txt");
                        fs.write(bytes, 0, bytes.length);
                        echo("FILE DOWNLOADED");

                    } else {
                        String dwnReq = " DWN " + ip.getHostAddress() + " " + port + " \"" + file + "\"";
                        dwnReq = String.format("%04d", dwnReq.length() + 5) + " " + dwnReq;
                        DatagramPacket dwnRequest = new DatagramPacket(dwnReq.getBytes(), dwnReq.getBytes().length,
                                InetAddress.getByName(nodeIp), nodePort);
                        sock.send(dwnRequest);
                    }

                } else if (command.equals("DWN")) {
                    String nodeIp = st.nextToken();
                    int nodePort = Integer.parseInt(st.nextToken());

                    StringTokenizer stq = new StringTokenizer(s, "\"");
                    String file = stq.nextToken();
                    file = stq.nextToken();

                    if (fileNames.contains(file.split("\\.")[0])) {

                        // random file generate here
                        Random r = new Random();
                        int x = r.nextInt(9) + 2;
                        byte[] bytes = new byte[x * 1024 * 1024];
                        r.nextBytes(bytes);

                        try {
                            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                            byte[] hash = messageDigest.digest(bytes);

                            StringBuilder sb = new StringBuilder(2 * hash.length);
                            for (byte b : hash) {
                                sb.append(String.format("%02x", b & 0xff));
                            }
                            String digest = sb.toString();

                            echo("File : " + file + "   File Size : " + x + "MB   Hash : " + digest);

                        } catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        // save file in local storage
                        FileOutputStream fs = new FileOutputStream(
                                "./Files/send_" + file.split("\\.")[0] + " " + new Date().getTime() + ".txt");
                        fs.write(bytes, 0, bytes.length);

                        // send file using TCP
                        Thread t = new Thread() {
                            public void run() {
                                try {
                                    ServerSocket server = new ServerSocket(port);
                                    Socket sr = server.accept();
                                    OutputStream os = sr.getOutputStream();
                                    os.write(bytes, 0, bytes.length);
                                    echo("FILE SENT");

                                    if (!sr.isClosed()) {
                                        sr.close();
                                    }
                                } catch (IOException e) {
                                    System.err.println("IOException " + e);
                                }
                            }
                        };

                        t.start();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Send acknowledgement
                        String reply = " DWNOK " + ip.getHostAddress() + " " + port + " \"" + file + "\"";
                        reply = String.format("%04d", reply.length() + 5) + " " + reply;
                        DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                                InetAddress.getByName(nodeIp), nodePort);
                        sock.send(dpReply);

                    }

                } else if (command.equals("DWNOK")) {
                    // Using TCP download file
                    String nodeIp = st.nextToken();
                    int nodePort = Integer.parseInt(st.nextToken());

                    StringTokenizer stq = new StringTokenizer(s, "\"");
                    String file = stq.nextToken();
                    file = stq.nextToken();

                    Socket sr = new Socket(nodeIp, nodePort);
                    InputStream is = sr.getInputStream();

                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    int b;
                    while ((b = is.read()) != -1) {
                        bs.write(b);
                    }
                    byte[] bytes = bs.toByteArray();
                    FileOutputStream fr = new FileOutputStream(
                            "./Files/received_" + file.split("\\.")[0] + " " + new Date().getTime() + ".txt");
                    is.read(bytes, 0, bytes.length);
                    fr.write(bytes, 0, bytes.length);
                    echo("FILE RECEIVED");
                    sr.close();

                } else if (command.equals("ECHO")) {
                    echo("NEIGHBOR NODES");
                    for (int i = 0; i < nodes.size(); i++) {
                        echo(nodes.get(i).getIp() + " " + nodes.get(i).getPort());
                    }

                    echo("CONTAINING FILES");
                    for (int i = 0; i < fileNames.size(); i++) {
                        echo(fileNames.get(i));
                    }

                    String reply = "0012 ECHOK 0\n";
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                            incoming.getAddress(), incoming.getPort());
                    sock.send(dpReply);
                }

            }
        } catch (IOException e) {
            System.err.println("IOException " + e);
        }

    }

    // simple function to echo data to terminal
    public static void echo(String msg) {
        System.out.println(msg);
    }

    public static void generateRandomFiles(List<String> fileNames) {
        Random r = new Random();
        int low = 3;
        int high = 6;
        int noOfFiles = r.nextInt(high - low) + low;
        List<String> allFiles = new ArrayList<String>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(
                    new FileReader(System.getProperty("user.dir").replace("\\", "/") + "/node/" + "FileNames.txt"));
            String line = reader.readLine();
            while (line != null) {
                allFiles.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("IOException " + e);
        }

        low = 0;
        high = allFiles.size();
        for (int i = 0; i < noOfFiles; i++) {
            int random = r.nextInt(high - low) + low;
            while (!fileNames.contains(allFiles.get(random))) {
                fileNames.add(allFiles.get(random));
            }
        }
    }

    public static boolean queryMatch(String query, String fileName) {
        String[] words = query.split(" ");
        boolean found = false;
        for (String word : words) {
            String regex = "\\b" + word + "\\b";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                found = true;
            }
        }
        return found;
    }

}
