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
                BoardConfig myBoard = new BoardConfig("192.168.0.0", 60000, 175111864);
                int doorNumberToTest = 1; // Control Relay/Door 1

                // Build and send the packet
                byte[] packet = PacketBuilder.remoteOpenDoor(myBoard, doorNumberToTest);
                UDPClient.send(myBoard, packet);

                System.out.println("... Command for LED " + doorNumberToTest + " sent successfully!");

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