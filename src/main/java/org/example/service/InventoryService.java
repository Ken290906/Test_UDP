package org.example.service;

import org.example.config.BoardConfig;
import org.example.network.UDPClient;
import org.example.protocol.PacketBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages inventory logic, RFID/QR code associations, and LED control.
 * This version handles raw scanner input without command prefixes.
 */
public class InventoryService {

    // --- Data Storage ---
    private final Map<String, String> rfidToProductMap = new HashMap<>();
    private final Map<String, Integer> productToLedMap = new HashMap<>();
    // private final Set<String> availableRfidTags = new HashSet<>(); // No longer needed
    private String lastScannedProduct = null;
    private final BoardConfig boardConfig;

    // --- State Management ---
    private String rfidWaitingForExportConfirmation = null;
    private Thread flashingThread;
    private int flashingLedNumber = -1; // -1 indicates no LED is flashing
    private volatile boolean stopFlashing = false;

    /**
     * Constructor to initialize the service.
     */
    public InventoryService() {
        this.boardConfig = new BoardConfig("255.255.255.255", 60000, 175111864); // SN: 0x0A6FFEB8
        initializeSampleData();
    }

    private void initializeSampleData() {
        System.out.println("Initializing system with sample data...");
        // RFID tags are no longer pre-defined. Any tag can be used.

        // 5 products (from QR codes) mapped to 5 LEDs (floors 1-5)
        productToLedMap.put("PRD1", 1);
        productToLedMap.put("PRD2", 2);
        productToLedMap.put("PRD3", 3);
        productToLedMap.put("PRD4", 4);
        productToLedMap.put("PRD5", 5);

        System.out.println("Ready to scan. Waiting for input from scanner or 'confirm' command...");
    }

    /**
     * Parses and handles raw input from a scanner or user command.
     * @param inputLine The line of text from the scanner or console.
     */
    public void handleInput(String inputLine) {
        String command = inputLine.trim();
        if (command.isEmpty()) {
            return;
        }

        // Priority 1: Handle confirmation for a pending export
        if ("confirm".equalsIgnoreCase(command)) {
            handleExportConfirmation();
            return; // Done
        }

        // If an export is pending confirmation, block other operations.
        if (this.rfidWaitingForExportConfirmation != null) {
            System.out.println("[ACTION REQUIRED] An export is pending for RFID '" + this.rfidWaitingForExportConfirmation + "'. Please find the item and type 'confirm'.");
            return;
        }

        // Priority 2: Handle product QR scan (start of import)
        if (command.startsWith("PRD") && productToLedMap.containsKey(command)) {
            handleProductScan(command);
        }
        // Priority 3: Handle RFID scan (import step 2 or start of export)
        else {
            handleRfidScan(command);
        }
    }

    private void handleProductScan(String productId) {
        // This is "Import: Step 1" - scanning the product's QR code
        this.lastScannedProduct = productId;
        System.out.println("OK: Product '" + productId + "' scanned. Now scan the RFID tag to associate.");
    }

    private void handleRfidScan(String rfidTag) {
        // This could be "Import: Step 2" or "Export"

        // Check if this is the second step of an import process
        if (this.lastScannedProduct != null) {
            // --- IMPORT LOGIC ---
            System.out.println("IMPORT: Associating RFID tag '" + rfidTag + "' with product '" + this.lastScannedProduct + "'.");

            if (rfidToProductMap.containsKey(rfidTag)) {
                 System.out.println("[WARN] This RFID tag is already associated with product '" + rfidToProductMap.get(rfidTag) + "'. Please handle the export first.");
                 this.lastScannedProduct = null; // Clear state
                 return;
            }

            rfidToProductMap.put(rfidTag, this.lastScannedProduct);
            Integer ledNumber = productToLedMap.get(this.lastScannedProduct);
            if (ledNumber != null) {
                System.out.println("-> Turning ON LED " + ledNumber + " for product " + this.lastScannedProduct);
                controlLed(ledNumber, true);
            }

            this.lastScannedProduct = null; // Reset for the next operation

        } else {
            // --- EXPORT or FREE SCAN LOGIC ---
            if (rfidToProductMap.containsKey(rfidTag)) {
                // --- START EXPORT PROCESS ---
                String productId = rfidToProductMap.get(rfidTag);
                System.out.println("EXPORT: Found product '" + productId + "' associated with RFID '" + rfidTag + "'.");

                Integer ledNumber = productToLedMap.get(productId);
                if (ledNumber != null) {
                    System.out.println("-> Starting continuous flashing for LED " + ledNumber + " to locate the item.");
                    startFlashingLed(ledNumber); // Start flashing
                }

                // Set state to wait for confirmation instead of completing the export
                this.rfidWaitingForExportConfirmation = rfidTag;
                System.out.println("ACTION: Please find the item. Type 'confirm' and press Enter after you have picked it up.");

            } else {
                System.out.println("INFO: Scanned free RFID tag '" + rfidTag + "'. Not associated with any product.");
            }
        }
    }

    /**
     * Handles the confirmation command to finalize an export.
     */
    private void handleExportConfirmation() {
        if (this.rfidWaitingForExportConfirmation == null) {
            System.out.println("[INFO] No export operation is waiting for confirmation.");
            return;
        }

        String rfidTag = this.rfidWaitingForExportConfirmation;
        String productId = rfidToProductMap.get(rfidTag);

        System.out.println("CONFIRMED: Completing export for product '" + productId + "' with RFID '" + rfidTag + "'.");

        // Stop flashing the LED
        if (flashingLedNumber != -1) {
            stopFlashingLed();
            // Send the final OFF command (simulated)
            System.out.println("-> Sending final OFF command for LED " + flashingLedNumber);
            controlLed(flashingLedNumber, false);
        }

        // Complete the export by removing the association
        rfidToProductMap.remove(rfidTag);
        System.out.println("-> RFID tag '" + rfidTag + "' is now free.");

        // Reset the state
        this.rfidWaitingForExportConfirmation = null;
        System.out.println("System is ready for the next operation.");
    }

    private void startFlashingLed(int ledNumber) {
        stopFlashing = false;
        flashingLedNumber = ledNumber;
        System.out.println("-> LED " + flashingLedNumber + " is now flashing continuously. Waiting for 'confirm'...");
        flashingThread = new Thread(() -> {
            while (!stopFlashing) {
                try {
                    // Send ON command silently
                    byte[] packet = PacketBuilder.sendControlCommand(this.boardConfig, 1, flashingLedNumber);
                    UDPClient.send(this.boardConfig, packet);
                    Thread.sleep(500); // Flash every 500ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Flashing thread interrupted.");
                    break;
                } catch (Exception e) {
                    System.err.println("[ERROR] Flashing LED failed: " + e.getMessage());
                }
            }
            System.out.println("Flashing thread stopped for LED " + flashingLedNumber);
        });
        flashingThread.start();
    }

    private void stopFlashingLed() {
        stopFlashing = true;
        if (flashingThread != null) {
            flashingThread.interrupt(); // Interrupt the thread to stop it
            try {
                flashingThread.join(1000); // Wait for the thread to finish (max 1 second)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            flashingThread = null;
        }
        flashingLedNumber = -1;
    }


    /**
     * Sends a command to control an LED.
     * @param ledNumber The LED to control (1-5).
     * @param turnOn    True to turn on, false to turn off.
     */
    private void controlLed(int ledNumber, boolean turnOn) {
        try {
            if (turnOn) {
                System.out.println("... Sending UDP command to turn ON floor/device " + ledNumber);
                byte[] packet = PacketBuilder.sendControlCommand(this.boardConfig, 1, ledNumber);
                UDPClient.send(this.boardConfig, packet);
                System.out.println("... ON command for floor/device " + ledNumber + " sent successfully!");
            } else {
                // --- IMPORTANT ---
                // The provided SDK documentation does not specify how to turn a relay/LED OFF.
                // It might be sending the same command again, or sending with a different floor number (e.g., floor + 40), or a different function ID.
                // For now, this action is only simulated.
                System.out.println("[SIMULATED] Turning OFF floor/device " + ledNumber + ". You need to implement the actual UDP 'OFF' command here.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send UDP command for LED " + ledNumber + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}