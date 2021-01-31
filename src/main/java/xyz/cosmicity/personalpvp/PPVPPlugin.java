/*
 *
 * Copyright © Nicholas Williams 2021
 *      Permission to store, use, copy, and distribute this source code and/or software may be
 *      taken as given on condition that the authorship of this source code is acknowledged as
 *      Nicholas Williams.
 * DISCLAIMER:
 *      The author assumes no responsibility or liability for any
 *      consequential damages from the use of this source code.
 *
 */

package xyz.cosmicity.personalpvp;

import dev.jorel.commandapi.CommandAPI;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.cosmicity.personalpvp.managers.PVPManager;
import xyz.cosmicity.personalpvp.managers.TaskManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class PPVPPlugin extends JavaPlugin {

    public static final double VERSION = 1.2;

    private final Logger log = this.getLogger();

    private boolean data_existed;

    private File langFile;

    private static PPVPPlugin instance;

    public static PPVPPlugin inst() {
        return instance;
    }
    private static void setInstance(final PPVPPlugin pl) {
        instance = pl;
        Utils.setPlugin(pl);
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(false);
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable(this);

        if (!(new File(this.getDataFolder(), "config.yml").exists())) {
            this.getConfig().options().copyDefaults(true);
        }
        this.langFile = new File(this.getDataFolder(),"lang.yml");
        this.saveDefaultConfig();
        this.loadCommandsConfig();
        this.loadLangConfig();

        this.checkConfigVersions();

        Config.load(this.getConfig());

        setInstance(this);

        new CommandHandler();

        if(Config.no_reset_for_any()) {
            try {
                if(!new File(this.getDataFolder(),Config.data_filename()).exists()) {
                    this.data_existed = new File(this.getDataFolder(),Config.data_filename()).createNewFile();
                }
                else Utils.loadObjects();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (this.data_existed) PVPManager.load();

        new Listeners(this);

        checkActionbar();

        this.log.info("Default PvP setting: "+(Config.default_pvp_status()?"TRUE":"FALSE"));
        this.log.info("Personal PvP ENABLED.");
    }

    public void checkActionbar() {
        if(Config.getBool("toggleable-actionbar.enable")) {
            if(!Config.rs_actionbar_on_quit()) {
                TaskManager.load();
            }
            TaskManager.start();
        }
    }

    public void reloadConfigs() {
        super.reloadConfig();
        this.loadCommandsConfig();
        this.loadLangConfig();
        this.checkConfigVersions();
        setInstance(this);
        Config.load(this.getConfig());
        checkActionbar();
    }

    public void checkConfigVersions() {
        if(this.getConfig().getDouble("version") < Config.CONFIG_VERSION) {
            this.log.info("A new version for config.yml is available.");
        }
        if(Config.commands().getDouble("version") < Config.COMMANDS_VERSION) {
            this.saveResource("commands.yml",true);
            Config.setCommands(YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "commands.yml")));
            this.log.info("NOTICE: commands.yml has been automatically updated.");
        }
        if(Config.lang().getDouble("version") < Config.LANG_VERSION) {
            File old = new File(this.getDataFolder(),"old_lang.yml");
            if(old.exists())
                if(!old.delete())
                    this.log.severe("Language YML update failed! Please delete old_lang.yml and restart.");
            File oldConfig = new File(this.getDataFolder(),"lang.yml");
            oldConfig.renameTo(old);
            this.saveResource("lang.yml",true);
            this.loadLangConfig();
            this.log.info("NOTICE: lang.yml has been automatically updated. Old lang is in old_lang.yml.");
        }
    }

    public void loadCommandsConfig() {
        if (!(new File(this.getDataFolder(), "commands.yml").exists())) {
            this.saveResource("commands.yml", false);
        }
        Config.setCommands(YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "commands.yml")));
    }
    public void loadLangConfig() {
        if (!(this.langFile.exists())) {
            this.saveResource("lang.yml", false);
        }
        Config.setLang(YamlConfiguration.loadConfiguration(this.langFile));
    }

    public boolean toggleHiddenActionbar(final Player p) {
        return TaskManager.toggleHidden(p.getUniqueId());
    }

    @Override
    public void onDisable() {
        TaskManager.stop();
        List<UUID> emptyList = new ArrayList<>();
        if(Config.rs_pvp_on_quit() != Config.rs_actionbar_on_quit() || Config.no_reset_for_any()) {
            Utils.saveObjects(Config.data_filename(), Config.rs_pvp_on_quit() ?emptyList:PVPManager.players(), Config.rs_actionbar_on_quit() ?emptyList:TaskManager.ignoredValues(), PVPManager.lockedPlayers());
        }
        this.saveConfig();
        this.log.info("Personal PvP DISABLED.");
    }
}
