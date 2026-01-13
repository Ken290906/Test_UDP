package org.example;

import org.example.config.BoardConfig;
import org.example.network.UDPClient;
import org.example.protocol.PacketBuilder;

/**
 * Main application entry point.
 * This class is simplified to directly send a command to the LED controller
 * for basic connectivity testing, as requested by the user.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("--- Direct LED Controller Connectivity Test ---");

        // 1. Configure your board directly (IP, Port, Serial Number)
        //    (These values were previously in WarehouseDB, but are now here for direct testing)
        //    Controller SN: 175111864 (Decimal)
        //    IP: 192.168.0.0
        BoardConfig myBoard = new BoardConfig("192.168.0.0", 60000, 175111864);
        int doorNumberToTest = 1; // Test with Relay/Door 1

        System.out.println("Attempting to send command to board: IP=" + myBoard.ip +
                           ", SN=" + myBoard.serialNumber + ", Door=" + doorNumberToTest);

        try {
            // 2. Build the UDP packet for "Remote Open Door"
            byte[] packet = PacketBuilder.remoteOpenDoor(myBoard, doorNumberToTest);

            // 3. Send the packet via UDP
            UDPClient.send(myBoard, packet);

            System.out.println("Command to activate Door/Relay " + doorNumberToTest +
                               " sent successfully to " + myBoard.ip);
            System.out.println("Please check if the LED connected to Door/Relay " + doorNumberToTest +
                               " on your board has turned ON.");

        } catch (Exception e) {
            System.err.println("An error occurred while sending the command:");
            e.printStackTrace();
            System.err.println("Please ensure your board's IP (" + myBoard.ip +
                               ") is correct and reachable, and check firewall settings.");
        }

        System.out.println("\n--- Test Finished ---");
    }
}