/*
 * Copyright (c) 2022.
 */

package com.nsgwick.personalpvp.config;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;

import java.io.File;

public class ConfigHandler {
    /**
     * Manages the config settings.
     */
    private final SettingsManager settings;
    public ConfigHandler(final File folder) {

        /*
        Create a settings manager to manage the config settings.
         */
        settings = SettingsManagerBuilder
                .withYamlFile(new File(folder, "config.yml"))
                .configurationData(GeneralConfig.class)
                .useDefaultMigrationService()
                .create();
    }
    public SettingsManager get() {
        return settings;
    }
}
