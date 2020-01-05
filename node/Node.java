/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import javafx.scene.effect.Light.Spot;

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
        String username = args[1];
        List<Neighbour> nodes = new ArrayList<Neighbour>();
        String s;

        try {
            ip = InetAddress.getByName("localhost");
            sock = new DatagramSocket(port);

            echo("Try to register at Bootstrap server....");

            String req = " REG " + ip.getHostAddress() + " " + port + " " + username;
            req = String.format("%04d", req.length() + 5) + " " + req;

            DatagramPacket request = new DatagramPacket(req.getBytes(), req.getBytes().length, ip, 55555);
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
                        echo("REGISTER FAIL :: due to node unreachable");
                    } else if (errorCode.equals("9998")) {
                        echo("REGISTER FAIL :: some other error");
                    } else {
                        echo("REGISTER SUCCESSFULL");
                        for (int i = 0; i < Integer.parseInt(errorCode); i++) {
                            String neighborIp = st.nextToken();
                            int neighborPort = Integer.parseInt(st.nextToken());

                            // send join request to neighbor node
                            String joinReq = " JOIN " + neighborIp + " " + neighborPort;
                            joinReq = String.format("%04d", joinReq.length() + 5) + " " + joinReq;

                            DatagramPacket joinRequest = new DatagramPacket(joinReq.getBytes(),
                                    joinReq.getBytes().length, InetAddress.getByName(neighborIp), neighborPort);
                            sock.send(joinRequest);
                        }

                        echo(username + " created at port " + port + ". Waiting for incoming data...");
                    }
                } else if (command.equals("ECHO")) {
                    for (int i = 0; i < nodes.size(); i++) {
                        echo(nodes.get(i).getIp() + " " + nodes.get(i).getPort());
                    }
                    String reply = "0012 ECHOK 0";
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                            incoming.getAddress(), incoming.getPort());
                    sock.send(dpReply);

                } else if (command.equals("JOIN")) {
                    String neighborIp = st.nextToken();
                    int neighborPort = Integer.parseInt(st.nextToken());
                    nodes.add(new Neighbour(neighborIp, neighborPort));
                    echo("" + nodes.size());

                    String reply = "0012 JOINOK 0";
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                            incoming.getAddress(), incoming.getPort());
                    sock.send(dpReply);
                }

                else if (command.equals("JOINOK")) {
                    nodes.add(new Neighbour(incoming.getAddress().getHostAddress(), incoming.getPort()));
                    echo("" + nodes.size());
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
}
