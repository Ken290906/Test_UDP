package org.example;

import org.example.service.InventoryService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main application entry point.
 * This class initializes and runs the console-based inventory management application,
 * listening for input from hardware scanners (acting as keyboard wedges).
 */
public class Main {
    public static void main(String[] args) {
        // 1. Initialize the inventory service which holds all the logic
        InventoryService inventoryService = new InventoryService();

        // 2. Print instructions for the user
        System.out.println("========================================================");
        System.out.println("Inventory Management System Started");
        System.out.println("========================================================");
        System.out.println("The system is now ready.");
        System.out.println("Please use your QR code and RFID scanners.");
        System.out.println("\n--- WORKFLOW ---");
        System.out.println("1. IMPORT: Scan a product QR code, then scan an RFID tag to link them.");
        System.out.println("2. EXPORT: Scan an already-linked RFID tag to unlink it.");
        System.out.println("\nType 'exit' and press Enter to quit the application.");
        System.out.println("--------------------------------------------------------");


        // 3. Keep the main thread alive to read scanner data from the console
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if ("exit".equalsIgnoreCase(line.trim())) {
                    System.out.println("Exiting application.");
                    break;
                }

                // Pass the scanned data to the service to handle
                inventoryService.handleInput(line);

            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to read from console: " + e.getMessage());
            e.printStackTrace();
        }
    }
}