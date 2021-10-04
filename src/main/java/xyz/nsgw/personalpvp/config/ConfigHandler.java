/*
 * Copyright (c) 2021.
 */

package xyz.nsgw.personalpvp.config;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;

import java.io.File;

public class ConfigHandler {
    private final SettingsManager settings;
    public ConfigHandler(final File folder) {

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
