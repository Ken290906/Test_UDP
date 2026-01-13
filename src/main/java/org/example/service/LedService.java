package org.example.service;

import org.example.config.BoardConfig;
import org.example.network.UDPClient;
import org.example.protocol.PacketBuilder;

public class LedService {
    private BoardConfig config =
            new BoardConfig("192.168.1.1", 60000, 223000123);

    public void locateProduct(String qrCode) throws Exception{
        int relay = 3;

        byte[] packet =
                PacketBuilder.ledOn(config, relay);

        UDPClient.send(config, packet);
    }
}
