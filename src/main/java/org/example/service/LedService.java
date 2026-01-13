package org.example.service;

import org.example.config.BoardConfig;
import org.example.network.UDPClient;
import org.example.protocol.PacketBuilder;

import java.util.Optional;

/**
 * This service contains the core business logic for the warehouse management system.
 */
public class LedService {

    // The service uses an instance of our mock database.
    // In a Spring Boot application, this would be an injected dependency (@Autowired).
    private final WarehouseDB database;

    public LedService() {
        this.database = new WarehouseDB();
    }

    /**
     * Locates a product by its QR code, finds its physical location in the database,
     * and sends a command to the corresponding controller to activate an LED.
     *
     * @param qrCode The QR code of the product to locate.
     * @throws Exception If there is a network error during packet sending.
     */
    public void locateProduct(String qrCode) throws Exception {
        System.out.println("Received request to locate product with QR Code: " + qrCode);

        // 1. Look up the location in the database using the QR code.
        Optional<Location> locationOptional = database.findLocationByQrCode(qrCode);

        // 2. Check if the location was found.
        if (locationOptional.isPresent()) {
            Location location = locationOptional.get();
            BoardConfig board = location.getBoard();
            int doorNumber = location.getRelayNumber();

            System.out.println("Product found at location: Board SN " + board.serialNumber + ", Relay/Door " + doorNumber);

            // 3. Build the UDP packet using the retrieved information.
            byte[] packet = PacketBuilder.remoteOpenDoor(board, doorNumber);

            // 4. Send the command.
            System.out.println("Sending 'Remote Open Door' command to IP " + board.ip + "...");
            UDPClient.send(board, packet);
            System.out.println("Command sent successfully.");

        } else {
            // Handle the case where the QR code does not map to any known location.
            System.err.println("Error: QR Code '" + qrCode + "' not found in the database. No command sent.");
        }
    }
}
