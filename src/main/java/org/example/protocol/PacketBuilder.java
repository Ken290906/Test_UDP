package org.example.protocol;

import org.example.config.BoardConfig;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Builds command packets for the Access Controller based on the C# SDK.
 */
public class PacketBuilder {

    // sequenceId is a rolling number for each command sent.
    private static long sequenceId = 0;

    /**
     * Creates a 64-byte packet for the "Remote Open Door" command (Function ID 0x40).
     *
     * @param cfg   The configuration of the target controller board.
     * @param doorNumber The door/relay to open (1-4).
     * @return The 64-byte command packet.
     */
    public static byte[] remoteOpenDoor(BoardConfig cfg, int doorNumber) {
        // The total packet size is 64 bytes, as defined in the SDK.
        byte[] packet = new byte[64];

        // Byte 0: Packet Type (Always 0x17 for this controller model)
        packet[0] = 0x17;

        // Byte 1: Function ID for "Remote Open Door" is 0x40
        packet[1] = 0x40;

        // Bytes 2-3: Unused, remain 0.

        // Bytes 4-7: Device Serial Number (in Little Endian format)
        int sn = cfg.serialNumber;
        packet[4] = (byte) (sn & 0xFF);
        packet[5] = (byte) ((sn >> 8) & 0xFF);
        packet[6] = (byte) ((sn >> 16) & 0xFF);
        packet[7] = (byte) ((sn >> 24) & 0xFF);

        // --- Data Block (starts at byte 8) ---
        // For function 0x40, the only data is the door number.
        // Byte 8: The door number (1-4) to operate.
        packet[8] = (byte) doorNumber;

        // Bytes 9-39: Unused for this function, remain 0.

        // Bytes 40-43: Sequence ID (a unique, incrementing number for each packet)
        // This is crucial for the controller to accept the command.
        sequenceId++;
        packet[40] = (byte) (sequenceId & 0xFF);
        packet[41] = (byte) ((sequenceId >> 8) & 0xFF);
        packet[42] = (byte) ((sequenceId >> 16) & 0xFF);
        packet[43] = (byte) ((sequenceId >> 24) & 0xFF);

        // Bytes 44-63: Unused, remain 0.

        return packet;
    }
}
