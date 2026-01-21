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
    private final Set<String> availableRfidTags = new HashSet<>();
    private String lastScannedProduct = null;
    private final BoardConfig boardConfig;

    /**
     * Constructor to initialize the service.
     */
    public InventoryService() {
        this.boardConfig = new BoardConfig("255.255.255.255", 60000, 175111864); // SN: 0x0A6FFEB8
        initializeSampleData();
    }

    private void initializeSampleData() {
        System.out.println("Initializing system with sample data...");
        // 5 sample RFID tags
        availableRfidTags.add("0006649621");
        availableRfidTags.add("0006649618");
        availableRfidTags.add("0006652658");
        availableRfidTags.add("0006649614");
        availableRfidTags.add("0006649617");

        // 5 products (from QR codes) mapped to 5 LEDs (floors 1-5)
        productToLedMap.put("PRD1", 1);
        productToLedMap.put("PRD2", 2);
        productToLedMap.put("PRD3", 3);
        productToLedMap.put("PRD4", 4);
        productToLedMap.put("PRD5", 5);

        System.out.println("Ready to scan. Waiting for input from scanner...");
    }

    /**
     * Parses and handles raw input from a scanner (acting as a keyboard wedge).
     * @param inputLine The line of text from the scanner.
     */
    public void handleInput(String inputLine) {
        String scannedData = inputLine.trim();
        if (scannedData.isEmpty()) {
            return;
        }

        // 1. Check if the scanned data is a known Product ID (from a QR code)
        if (productToLedMap.containsKey(scannedData)) {
            handleProductScan(scannedData);
        }
        // 2. Check if the scanned data is a known RFID tag
        else if (availableRfidTags.contains(scannedData)) {
            handleRfidScan(scannedData);
        }
        // 3. Otherwise, the data is unknown
        else {
            System.out.println("[ERROR] Unknown data scanned: '" + scannedData + "'. Please scan a valid product QR code or RFID tag.");
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
                 // Clear the state so the user doesn't accidentally associate the next RFID scan
                 this.lastScannedProduct = null;
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
            // --- EXPORT LOGIC ---
            if (rfidToProductMap.containsKey(rfidTag)) {
                String productId = rfidToProductMap.get(rfidTag);
                System.out.println("EXPORT: Product '" + productId + "' is being shipped.");
                
                Integer ledNumber = productToLedMap.get(productId);
                if (ledNumber != null) {
                    System.out.println("-> Turning ON LED " + ledNumber);
                    controlLed(ledNumber, true);
                }

                rfidToProductMap.remove(rfidTag); // Un-assign the RFID tag
                System.out.println("-> RFID tag '" + rfidTag + "' is now free.");

            } else {
                System.out.println("INFO: Scanned free RFID tag '" + rfidTag + "'. Not associated with any product.");
            }
        }
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