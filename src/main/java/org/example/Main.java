package org.example;

import org.example.config.BoardConfig;
import org.example.network.UDPClient;
import org.example.protocol.PacketBuilder;
import org.example.service.ConsoleReaderService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main application entry point.
 * This class initializes and runs the console-based scanner LED controller application.
 */
public class Main {
    public static void main(String[] args) {
        // 1. Define the action to be taken when a scan event is detected from console
        Runnable onScanAction = () -> {
            try {
                System.out.println("... Triggering UDP command.");
                // Use the broadcast IP address as you suggested.
                BoardConfig myBoard = new BoardConfig("255.255.255.255", 60000, 175111864); // Corrected serial number from screenshot: 0x0A6FFEB8

                int doorNumber = 1; // This must be 1 according to the document.
                int floorNumber = 5;  // Control floor 5 (LD05) as shown in the screenshot.

                System.out.println("... Sending command to control Floor/Device " + floorNumber);

                // Build and send the packet.0006649614

                byte[] packet = PacketBuilder.sendControlCommand(myBoard, doorNumber, floorNumber);
                UDPClient.send(myBoard, packet);

                System.out.println("... Command for Floor/Device " + floorNumber + " sent successfully!");

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to send UDP command: " + e.getMessage());
                e.printStackTrace();
            }
        };

        // 2. Create and start the ConsoleReaderService in a background thread
        ConsoleReaderService consoleService = new ConsoleReaderService(onScanAction);
        Thread consoleThread = new Thread(consoleService);
        consoleThread.start();
        System.out.println("Application started. Listening for scan events from console...");
        System.out.println("Press Enter to manually trigger a scan, or type 'exit' and press Enter to quit.");

        // Keep the main thread alive for console input if needed for manual trigger/exit
        // This is a simple way to keep the application running.
        try (BufferedReader systemInReader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = systemInReader.readLine()) != null) {
                if ("exit".equalsIgnoreCase(line.trim())) {
                    System.out.println("Exiting application.");
                    consoleThread.interrupt(); // Signal the console service to stop
                    break;
                }
                // If user just presses Enter or types something, it will trigger the action
                if (!line.trim().isEmpty()) {
                    System.out.println("Manual trigger from console: " + line.trim());
                    onScanAction.run();
                } else {
                    // If user just presses Enter on an empty line, also trigger
                    System.out.println("Manual trigger (empty line).");
                    onScanAction.run();
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error reading from System.in in main thread: " + e.getMessage());
            e.printStackTrace();
        }
    }
}