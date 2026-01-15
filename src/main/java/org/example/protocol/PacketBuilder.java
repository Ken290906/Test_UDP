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
     * Creates a 64-byte packet for the "One-to-many remote control" command (Function ID 0x40).
     * This command can be used to control relays and LEDs.
     *
     * @param cfg         The configuration of the target controller board.
     * @param doorNumber  The door number (must be 1).
     * @param floorNumber The floor/device number to control (1-40 for relays/LEDs).
     * @return The 64-byte command packet.
     */
    public static byte[] sendControlCommand(BoardConfig cfg, int doorNumber, int floorNumber) {
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
        // Byte 8: Door number (must be 1 for this command).
        packet[8] = (byte) doorNumber;
        // Byte 9: Floor number (1-40 for NO, 41-80 for NC). This controls the specific relay/LED.
        packet[9] = (byte) floorNumber;
        
        // Bytes 10-11: Action duration (0 for default).
        packet[10] = 0;
        packet[11] = 0;

        // Bytes 12-39: Unused for this function, remain 0.

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
