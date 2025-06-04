package com.mycompany.datagenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;

public class ConfigLoader {

    private static final String CONFIG_FILE = "/application.yml"; // Leading '/' for classpath root

    public static AppConfig loadConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules(); // Good practice for additional modules like JavaTime

        try (InputStream inputStream = ConfigLoader.class.getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException("Cannot find configuration file: " + CONFIG_FILE +
                        ". Make sure it's in src/main/resources.");
            }
            return mapper.readValue(inputStream, AppConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load or parse configuration from " + CONFIG_FILE, e);
        }
    }
}