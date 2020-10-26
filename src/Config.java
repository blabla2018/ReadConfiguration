package com.ringcentral.sre.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.testng.TestException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Config {

    private static Config config;
    private final Map<String, Object> properties = new HashMap<>();
    private static final String DEFAULT_FILE_NAME = "config.yaml";
    private static final String SYSTEM_PROPERTY_FILE_NAME = "config.file";

    private Config() {
        String[] fileNames = System.getProperty(SYSTEM_PROPERTY_FILE_NAME, DEFAULT_FILE_NAME).split(",");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        for (String filename : fileNames) {
            try {
                File file = new File(filename);
                if (file.exists()) {
                    final Map<String, Object> stringObjectMap = mapper.readValue(file, new TypeReference<>() {
                    });
                    properties.putAll(flatten(stringObjectMap));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static synchronized Config get() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    private Map<String, Object> flatten(Map<String, Object> in) {
        return in.entrySet().stream()
                .flatMap(entry -> flatten(entry).entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    private Map<String, Object> flatten(Map.Entry<String, Object> in) {
        // for other then Map objects return them
        if (!(in.getValue() instanceof Map)) {
            return Collections.singletonMap(in.getKey(), in.getValue());
        }
        // extract the key prefix for nested objects
        String prefix = in.getKey();
        @SuppressWarnings("unchecked")
        Map<String, Object> values = (Map<String, Object>) in.getValue();
        // create a new Map, with prefix added to each key
        Map<String, Object> flattenMap = new HashMap<>();
        values.keySet().forEach(key ->  flattenMap.put(prefix + "." + key, values.get(key)));
        // use recursion to flatten the structure deeper
        return flatten(flattenMap);
    }


    private String getSystemProperty(String name) {
        return System.getProperty(name);
    }

    private String getProperty(String name, boolean throwException) {
        String systemProperty = getSystemProperty(name);
        Object result = systemProperty == null ? properties.get(name) : systemProperty;
        if (throwException && result == null) {
            throw new TestException("Variable '" + name + "' is missing!");
        }
        return String.valueOf(result);
    }

    public String getProperty(String name) {
        return getProperty(name, true);
    }

    public String getProperty(String name, String defaultValue) {
        Object value = getProperty(name, false);
        return value == null ? defaultValue : String.valueOf(value);
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String value = getProperty(name, false);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public Long getLongProperty(String name, Long defaultValue) {
        String value = getProperty(name, false);
        Long result = defaultValue;
        if (value != null) {
            try {
                result = Long.parseLong(value);
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        return result;
    }

    public int getIntProperty(String name, int defaultValue) {
        return getLongProperty(name, (long) defaultValue).intValue();
    }
}
