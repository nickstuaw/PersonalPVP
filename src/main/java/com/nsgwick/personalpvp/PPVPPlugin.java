/*
 * Copyright (c) 2022.
 */

package com.nsgwick.personalpvp;

import com.nsgwick.personalpvp.commands.CommandHandler;
import com.nsgwick.personalpvp.managers.TaskManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.nsgwick.personalpvp.config.ConfigHandler;
import com.nsgwick.personalpvp.config.GeneralConfig;
import com.nsgwick.personalpvp.managers.PVPManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class PPVPPlugin extends JavaPlugin {

    /**
    The main Logger object to log to the console with.
     */
    private final Logger log = this.getLogger();

    /**
    A boolean that represents the existence of a data file.
     */
    private boolean data_existed;

    /**
    The command handler. This manages the execution of the commands.
     */
    private CommandHandler commandHandler;

    /**
    The config handler manages config.yml (and any other config files) using ConfigMe.
     */
    private ConfigHandler configHandler;

    /**
    An instance of this class that is accessible from other objects.
     */
    private static PPVPPlugin instance;

    /**
    The PVP manager stores and handles the PVP-related attributes of each player.
     */
    private  PVPManager pvpManager;

    /**
     * Get the instance of this class.
     * @return The plugin instance.
     */
    public static PPVPPlugin inst() {
        return instance;
    }

    /**
     * Pass this instance into the variable used by inst()
     * @param pl The instance to pass.
     */
    private static void setInstance(final PPVPPlugin pl) {
        instance = pl;
        /*
        Save the instance in a variable in Utils.
         */
        Utils.setPlugin(pl);
    }

    /*
    This method runs upon server startup.
     */
    @Override
    public void onEnable() {

        /*
        Initialise a new PVP manager.
         */
        pvpManager = new PVPManager();

        /*
        Initialise the config handler and pass the folder that this plugin is using to store files.
         */
        configHandler = new ConfigHandler(this.getDataFolder());

        /*
        Pass the instance to Utils for future use elsewhere.
         */
        setInstance(this);

        /*
        Initialise the command handler.
         */
        commandHandler = new CommandHandler(this);

        /*
        If any pvp attribute is reset when a player leaves,
         */
        if(no_reset_for_any()) {
            /*
            Try to create the new data file so that the data kept can be stored.
             */
            try {
                /*
                Fetch the name of the data file from config.yml.
                 */
                String filename = configHandler.get().getProperty(GeneralConfig.FILE);
                /*
                If the data file doesn't exist,
                 */
                if(!new File(this.getDataFolder(),filename).exists()) {
                    /*
                    Create a new data file. data_existed becomes true if the file was just created and didn't exist.
                     */
                    this.data_existed = new File(this.getDataFolder(),filename).createNewFile();
                }
                /*
                If the data file exists, load the existing data from it.
                 */
                else Utils.loadObjects();
            } catch (IOException | ClassNotFoundException e) {
                /*
                If any error occurs, show it.
                 */
                e.printStackTrace();
            }
        }
        /*
        If the file was created this time round, load the PVP manager.
         */
        if (this.data_existed) pvpManager.load();

        /*
        Register the PVP event listeners.
         */
        new Listeners(this);

        /*
        Enable the actionbar according to the config settings.
         */
        checkActionbar();

        /*
        Send startup information to the console.
         */
        this.log.info("Default PvP setting: "
                +(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS)?"TRUE":"FALSE"));
        this.log.info("If you are using spigot (not paper) or get actionbar errors, please disable the actionbar"
                + " in config.yml by changing toggleable-actionbar.enable to false.");
        this.log.info("Personal PvP ENABLED.");
    }

    /**
     * Get the config handler.
     * @return The global ConfigHandler.
     */
    public ConfigHandler conf() {
        return this.configHandler;
    }

    /**
     * Get the PVPManager.
     * @return The global PVPManager.
     */
    public PVPManager pvp() {return this.pvpManager;}

    /**
     * Checks the config and enables the actionbar accordingly.
     */
    public void checkActionbar() {
        /*
         * If actionbars are enabled,
         */
        if(configHandler.get().getProperty(GeneralConfig.ABAR_ENABLE)) {
            /*
            If actionbars don't reset on quit,
             */
            if(! configHandler.get().getProperty(GeneralConfig.ABAR_RESET_ON_Q)) {
                /*
                Load existing actionbar visibility data.
                 */
                TaskManager.load();
            }
            /*
            Start the actionbar loop to show the permanent actionbar.
             */
            TaskManager.start();
        }
    }

    /**
     * Reload the configuration files.
     */
    public void reloadConfigs() {
        configHandler.get().reload();
        setInstance(this);
        checkActionbar();
    }

    /**
     * Toggle the actionbar for an online player.
     * @param p The online player.
     * @return True if now default status, false if status has been altered.
     */
    public boolean toggleHiddenActionbar(final Player p) {
        return TaskManager.toggleHiddenActionbar(p.getUniqueId());
    }

    @Override
    public void onDisable() {
        /*
        Stop the actionbar loop.
         */
        TaskManager.stop();
        /*
        Disable the command handler.
         */
        commandHandler.onDisable();
        /*
        Create an empty list for saving.
         */
        List<UUID> emptyList = new ArrayList<>();
        /*
        If any PVP attributes don't reset on quit,
         */
        if(configHandler.get().getProperty(GeneralConfig.RESET_PVP_ON_QUIT)
                != configHandler.get().getProperty(GeneralConfig.ABAR_RESET_ON_Q) || no_reset_for_any()) {
            /*
            Save the lists.
             */
            Utils.saveObjects(configHandler.get().getProperty(GeneralConfig.FILE),
                    /*
                    If PVP statuses reset on quit, save an empty list.
                    If PVP statuses don't reset on quit, save the list.
                     */
                    configHandler.get().getProperty(GeneralConfig.RESET_PVP_ON_QUIT) ?
                            emptyList:
                            PPVPPlugin.inst().pvp().alteredPlayers(),
                    /*
                    If actionbar statuses reset on quit, save an empty list.
                    If actionbar statuses don't reset on quit, save the list.
                     */
                    configHandler.get().getProperty(GeneralConfig.ABAR_RESET_ON_Q) ? emptyList :
                            TaskManager.playersWithAlteredActionbar(),
                    /*
                    Save the list of locked players.
                     */
                    PPVPPlugin.inst().pvp().lockedPlayers());
        }
        /*
        Save the configuration files.
         */
        this.saveConfig();
        /*
        Console feedback.
         */
        this.log.info("Personal PvP DISABLED.");
    }

    /**
     * Check if any pvp attribute resets on quit.
     * @return True if any pvp attribute is reset when a player quits.
     */
    private boolean no_reset_for_any() {
        return !configHandler.get().getProperty(GeneralConfig.DO_STATUS_LOCKS_RESET_ON_QUIT) ||
                !configHandler.get().getProperty(GeneralConfig.RESET_PVP_ON_QUIT) ||
                !configHandler.get().getProperty(GeneralConfig.ABAR_RESET_ON_Q);
    }
}
