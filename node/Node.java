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
        ;
        String username = args[1];
        List<Neighbour> nodes = new ArrayList<Neighbour>();

        try {
            ip = InetAddress.getByName("localhost");
            sock = new DatagramSocket(port);

            echo(username + " created at port " + port + ". Try to register at Bootstrap server....");

            String req = " REG " + ip.getHostAddress() + " " + port + " " + username;
            req = String.format("%04d", req.length() + 5) + " " + req;

            DatagramPacket request = new DatagramPacket(req.getBytes(), req.getBytes().length, ip, 55555);
            sock.send(request);

            byte[] buffer = new byte[65536];
            DatagramPacket ackForReg = new DatagramPacket(buffer, buffer.length);
            sock.receive(ackForReg);

            byte[] data = ackForReg.getData();
            String s = new String(data, 0, ackForReg.getLength());
            // echo the details of incoming data - client ip : client port - client message
            echo(ackForReg.getAddress().getHostAddress() + " : " + ackForReg.getPort() + " - " + s);

            StringTokenizer st = new StringTokenizer(s, " ");
            String length = st.nextToken();
            String command = st.nextToken();
            String errorCode = st.nextToken();

            if (command.equals("REGOK")) {
                if (errorCode.equals("0")) {
                    echo("REGISTER SUCCESSFULL");
                } else if (errorCode.equals("9999")) {
                    echo("REGISTER FAIL :: due to node unreachable");
                } else if (errorCode.equals("9998")) {
                    echo("REGISTER FAIL :: some other error");
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
