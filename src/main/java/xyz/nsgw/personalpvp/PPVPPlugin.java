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

package xyz.nsgw.personalpvp;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.nsgw.personalpvp.commands.CommandHandler;
import xyz.nsgw.personalpvp.config.ConfigHandler;
import xyz.nsgw.personalpvp.config.GeneralConfig;
import xyz.nsgw.personalpvp.managers.PVPManager;
import xyz.nsgw.personalpvp.managers.TaskManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class PPVPPlugin extends JavaPlugin {

    public static final double VERSION = 1.3;

    private final Logger log = this.getLogger();

    private boolean data_existed;

    private CommandHandler commandHandler;

    private ConfigHandler configHandler;

    private static PPVPPlugin instance;

    public static PPVPPlugin inst() {
        return instance;
    }
    private static void setInstance(final PPVPPlugin pl) {
        instance = pl;
        Utils.setPlugin(pl);
    }

    @Override
    public void onEnable() {

        configHandler = new ConfigHandler(this.getDataFolder());

        setInstance(this);

        commandHandler = new CommandHandler(this);

        if(no_reset_for_any()) {
            try {
                String filename = configHandler.get().getProperty(GeneralConfig.FILE);
                if(!new File(this.getDataFolder(),filename).exists()) {
                    this.data_existed = new File(this.getDataFolder(),filename).createNewFile();
                }
                else Utils.loadObjects();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (this.data_existed) PVPManager.load();

        new Listeners(this);

        checkActionbar();

        this.log.info("Default PvP setting: "+(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS)?"TRUE":"FALSE"));
        this.log.info("Personal PvP ENABLED.");
    }

    public ConfigHandler conf() {
        return configHandler;
    }

    public void checkActionbar() {
        if(configHandler.get().getProperty(GeneralConfig.ABAR_ENABLE)) {
            if(! configHandler.get().getProperty(GeneralConfig.ABAR_RESET_ON_Q)) {
                TaskManager.load();
            }
            TaskManager.start();
        }
    }

    public void reloadConfigs() {
        configHandler.get().reload();
        setInstance(this);
        checkActionbar();
    }

    public boolean toggleHiddenActionbar(final Player p) {
        return TaskManager.toggleHidden(p.getUniqueId());
    }

    @Override
    public void onDisable() {
        TaskManager.stop();
        commandHandler.onDisable();
        List<UUID> emptyList = new ArrayList<>();
        if(configHandler.get().getProperty(GeneralConfig.RESET_PVP_ON_QUIT) != configHandler.get().getProperty(GeneralConfig.ABAR_RESET_ON_Q) || no_reset_for_any()) {
            Utils.saveObjects(configHandler.get().getProperty(GeneralConfig.FILE), configHandler.get().getProperty(GeneralConfig.RESET_PVP_ON_QUIT) ?emptyList:PVPManager.players(), configHandler.get().getProperty(GeneralConfig.ABAR_RESET_ON_Q) ?emptyList:TaskManager.ignoredValues(), PVPManager.lockedPlayers());
        }
        this.saveConfig();
        this.log.info("Personal PvP DISABLED.");
    }

    private boolean no_reset_for_any() {
        return !configHandler.get().getProperty(GeneralConfig.DO_STATUS_LOCKS_RESET_ON_QUIT) ||
                !configHandler.get().getProperty(GeneralConfig.RESET_PVP_ON_QUIT) ||
                !configHandler.get().getProperty(GeneralConfig.ABAR_RESET_ON_Q);
    }
}
