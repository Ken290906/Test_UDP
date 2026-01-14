package org.example.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A service that runs in a background thread to read lines from System.in.
 * Each line read is considered a scan event, triggering a predefined action.
 */
public class ConsoleReaderService implements Runnable {

    private final Runnable onScanAction;

    public ConsoleReaderService(Runnable onScanAction) {
        this.onScanAction = onScanAction;
    }

    @Override
    public void run() {
        System.out.println("Console Reader Service started. Waiting for input from console (System.in)...");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                if (!line.trim().isEmpty()) {
                    System.out.println("Console Input Detected: " + line.trim());
                    if (onScanAction != null) {
                        onScanAction.run(); // Trigger the defined action
                    }
                }
            }
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted()) { // Only log if not intentionally interrupted
                System.err.println("[ERROR] Console Reader Service error: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            System.out.println("Console Reader Service stopped.");
        }
    }
}
