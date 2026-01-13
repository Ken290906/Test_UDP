package org.example.config;

public class BoardConfig {
    public String ip;
    public int port;
    public int serialNumber;

    public BoardConfig(String ip, int port, int serialNumber) {
        this.ip = ip;
        this.port = port;
        this.serialNumber = serialNumber;
    }
}
