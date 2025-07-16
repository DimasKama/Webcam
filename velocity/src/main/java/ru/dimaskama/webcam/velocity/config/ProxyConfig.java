package ru.dimaskama.webcam.velocity.config;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ProxyConfig {

    private final Path path;
    private int port = 25454;
    private String bindAddress = "";
    private String host = "";

    public ProxyConfig(Path path) {
        this.path = path;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void loadOrCreate() throws Exception {
        if (Files.exists(path)) {
            load();
        } else {
            save();
        }
    }

    private void load() throws Exception {
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        }
        loadFromProperties(properties);
    }

    private void loadFromProperties(Properties properties) {
        int port = Integer.parseInt(properties.getProperty("port", "25454"));
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("specified port " + port + " is not in range [0;65535]");
        }
        String bindAddress = properties.getProperty("bind_address", "");
        String host = properties.getProperty("host", "");
        if (!host.isEmpty()) {
            try {
                new URI("webcam://" + host);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Failed to parse host", e);
            }
        }
        this.port = port;
        this.bindAddress = bindAddress;
        this.host = host;
    }

    public void save() throws Exception {
        Properties properties = new Properties();
        properties.put("port", String.valueOf(port));
        properties.put("bind_address", bindAddress);
        properties.put("host", host);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (OutputStream out = Files.newOutputStream(path)) {
            properties.store(out, " Webcam proxy config");
        }
    }

}
