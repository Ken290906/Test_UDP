package org.example.network;

import org.example.config.BoardConfig;

import java.io.IOException;
import java.net.*;

public class UDPClient {

    public static void send(BoardConfig cfg, byte[] data) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress addr = InetAddress.getByName(cfg.ip);

        DatagramPacket packet = new DatagramPacket(data, data.length, addr, cfg.port);

        socket.send(packet);
        socket.close();
    }

}
