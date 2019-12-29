package com.storyboard.hitsoundplus.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import com.storyboard.hitsoundplus.storage.DiskStorage;
import com.storyboard.hitsoundplus.util.AsyncTask;

import org.apache.logging.log4j.Logger;

public class ConfigManager {

    private static final String CONFIG_NAME = "config.json";

    private Logger logger;
    
    private DiskStorage modConfigStorage;

    public ConfigManager(File configDirectory, Logger logger) {
        this.logger = logger;

        modConfigStorage = new DiskStorage(configDirectory);

        modConfigStorage.createStorageDirectory();
    }

    public DiskStorage getModConfigStorage() {
        return modConfigStorage;
    }

    public File getModConfigDirectory() {
        return modConfigStorage.getStorageFolder();
    }

    public AsyncTask<Void> loadConfig(IConfigFile config) {
        return loadConfig(config, CONFIG_NAME);
    }

    public AsyncTask<Void> saveConfig(IConfigFile config) {
        return saveConfig(config, CONFIG_NAME);
    }

    public AsyncTask<Void> loadConfig(IConfigFile config, String name) {
        return loadConfig(modConfigStorage, config, name);
    }

    public AsyncTask<Void> saveConfig(IConfigFile config, String name) {
        return saveConfig(modConfigStorage, config, name);
    }

    protected AsyncTask<Void> loadConfig(DiskStorage storage, IConfigFile config, String name) {
        return new AsyncTask<>(() -> {
            try {
                config.load(new ByteArrayInputStream(storage.getSync(name)));
            } catch (Exception e) {
                logger.error("Error while loading config " + name + " : " + e.getLocalizedMessage());
            }

            return null;
        });
    }

    protected AsyncTask<Void> saveConfig(DiskStorage storage, IConfigFile config, String name) {
        return new AsyncTask<>(() -> {
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                config.save(output);
        
                storage.saveSync(output.toByteArray(), name);
            } catch (IOException e) {
                logger.error("Error while loading config " + name + " : " + e.getLocalizedMessage());
            }

            return null;
        });
    }

}