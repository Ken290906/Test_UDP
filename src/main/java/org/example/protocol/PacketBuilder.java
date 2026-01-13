package org.example.protocol;


import org.example.config.BoardConfig;

public class PacketBuilder {

    public static byte[] ledOn(BoardConfig cfg, int relay) {
        byte[] p = new byte[64];

        p[0] = 0x17; // type
        p[1] = (byte) 0xA0; //function
        p[2] = 0x00;
        p[3] = 0x00;

        int sn = cfg.serialNumber;
        p[4] = (byte) (sn & 0xFF);
        p[5] = (byte) (sn >> 8);
        p[6] = (byte) (sn >> 16);
        p[7] = (byte) (sn >> 24);

        // header giong NetAssist C#
        p[8] = 0x55;
        p[9] = (byte) 0xAA;
        p[10] = (byte) 0xAA;
        p[11] = 0x55;

        p[12] = (byte) relay;
        p[13] = 0x01;
        return p;
    }
}
