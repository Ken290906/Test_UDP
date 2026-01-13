package org.example.service;

import org.example.config.BoardConfig;

/**
 * Represents a physical location in the warehouse, which is defined by a specific
 * controller board and a relay number on that board.
 */
public class Location {
    private final BoardConfig board;
    private final int relayNumber;

    public Location(BoardConfig board, int relayNumber) {
        this.board = board;
        this.relayNumber = relayNumber;
    }

    public BoardConfig getBoard() {
        return board;
    }

    public int getRelayNumber() {
        return relayNumber;
    }
}