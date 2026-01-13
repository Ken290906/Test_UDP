package org.example.service;

import org.example.config.BoardConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A mock database class to simulate the storage of warehouse layout information.
 * In a real-world application, this class would be replaced with a proper
 * database connection (e.g., using JPA, JDBC, etc.).
 */
public class WarehouseDB {

    // A map to store the relationship between a product's QR code and its physical location.
    private final Map<String, Location> productLocations = new HashMap<>();

    // A map to store the configurations of all controller boards in the warehouse.
    // The key is the board's serial number.
    private final Map<Integer, BoardConfig> boards = new HashMap<>();

    /**
     * Initializes the mock database with some sample data.
     */
    public WarehouseDB() {
        // --- Initialize Controller Boards ---
        // Using the user's provided IP and Serial Number for their single board.
        // Controller SN: 175111864 (Decimal)
        // IP: 192.168.0.0
        BoardConfig board1 = new BoardConfig("192.168.0.0", 6000, 175111864);
        boards.put(board1.serialNumber, board1);

        // --- Map Products (by QR code) to Locations ---
        // The PDF describes mapping a product to a location (which is a board + a relay).
        // Let's create some sample mappings for the single board.

        // Product with QR code "PROD123" is in the location controlled by board1, relay/door 1.
        productLocations.put("PROD123", new Location(boards.get(175111864), 1));

        // Product with QR code "PROD456" is in the location controlled by board1, relay/door 2.
        productLocations.put("PROD456", new Location(boards.get(175111864), 2));

        // Product with QR code "PROD789" is in the location controlled by board1, relay/door 3.
        productLocations.put("PROD789", new Location(boards.get(175111864), 3));
    }

    /**
     * Finds the physical location (board and relay) for a given product QR code.
     * This simulates a database query.
     *
     * @param qrCode The QR code of the product.
     * @return An Optional containing the Location if found, otherwise an empty Optional.
     */
    public Optional<Location> findLocationByQrCode(String qrCode) {
        return Optional.ofNullable(productLocations.get(qrCode));
    }
}
