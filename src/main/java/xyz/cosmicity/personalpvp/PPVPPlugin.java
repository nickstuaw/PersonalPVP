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
import xyz.cosmicity.personalpvp.commands.CommandHandler;
import xyz.cosmicity.personalpvp.managers.PVPManager;
import xyz.cosmicity.personalpvp.managers.TaskManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class PPVPPlugin extends JavaPlugin {

    public static final double VERSION = 1.1, CONFIG_VERSION = 1.1, COMMANDS_VERSION = 1.0;

    private YamlConfiguration commandsConfig;

    private final Logger log = this.getLogger();

    private double config_version, actionbar_login_duration, actionbar_attack_duration;
    private int pvp_cooldown;

    private boolean keep_xp_pvp, keep_inv_pvp, use_reminder, reset_combination, reset_actionbar_on_quit, reset_pvp_on_quit, default_pvp_status, default_actionbar_status, prevent_fishing_rods, prevent_player_damage, prevent_projectiles, prevent_potions, prevent_combustion;

    private String pvp_on_reminder, worldtime_in_world, pvp_cooldown_prompt, pvpcontrol_personal_lines, pvpcontrol_lines, actionbar_message, console_format, data_filename;
    private String[] actionbar_pvp_statuses, console_pvpstatuses, pvp_status_prefixes;

    private boolean pvp_toggle_log_to_console, data_existed;

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

        CommandAPI.onLoad(false);
        CommandAPI.onEnable(this);

        Utils.setPlugin(this);

        if (!(new File(this.getDataFolder(), "config.yml").exists())) {
            this.getConfig().options().copyDefaults(true);
        }
        saveDefaultConfig();
        if (!(new File(this.getDataFolder(), "commands.yml").exists())) {
            this.saveResource("commands.yml", false);
        }

        this.loadVariables();

        setInstance(this);

        this.loadCommandsConfig();

        this.checkConfigVersion();

        new CommandHandler();

        if(this.reset_pvp_on_quit != this.reset_actionbar_on_quit || !this.reset_combination) {
            try {
                if(!new File(this.getDataFolder(),this.data_filename()).exists()) {
                    data_existed = new File(this.getDataFolder(),this.data_filename()).createNewFile();
                }
                else Utils.loadObjects();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (!this.reset_pvp_on_quit && data_existed) PVPManager.load();

        if(!this.getBool("command-settings.pvp-toggle.enable")) new Listeners(this);

        checkActionbar();

        this.log.info("Default PvP setting: "+(this.default_pvp_status?"TRUE":"FALSE"));
        this.log.info("Personal PvP ENABLED.");
    }

    public void checkActionbar() {
        if(this.getBool("togglable-actionbar.enable")) {
            if(!this.reset_actionbar_on_quit) {
                TaskManager.load();
            }
            TaskManager.start();
        }
    }

    public double config_version() {
        return this.config_version;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.loadVariables();
        this.loadCommandsConfig();
        this.checkConfigVersion();
        setInstance(this);
        checkActionbar();
    }

    public void checkConfigVersion() {
        if(this.config_version < CONFIG_VERSION) {
            this.log.info("A new version for config.yml is available.");
        }
        if(this.commandsConfig.getDouble("version") < COMMANDS_VERSION && this.getBool("auto-update-commands-yml")) {
            this.saveResource("commands.yml",true);
            this.log.info("NOTICE: commands.yml has been automatically updated.");
        }
    }

    private void loadVariables() {
        this.config_version = this.getConfig().getDouble("version");
        this.prevent_fishing_rods = this.getBool("prevent.fishing-rods");
        this.prevent_player_damage = this.getBool("prevent.damage-from-players");
        this.prevent_projectiles = this.getBool("prevent.throwable-projectiles");
        this.prevent_potions = this.getBool("prevent.potions");
        this.prevent_combustion = this.getBool("prevent.combustion-from-players");
        this.default_actionbar_status = this.getBool("togglable-actionbar.show-actionbar-on-first-join");
        this.actionbar_login_duration = this.getConfig().getDouble("togglable-actionbar.show-on-login-duration-seconds");
        this.actionbar_attack_duration = this.getConfig().getDouble("togglable-actionbar.show-on-attack-duration-seconds");
        this.reset_actionbar_on_quit = this.getBool("togglable-actionbar.reset-actionbar-status-on-quit");
        this.actionbar_message = this.getStr("togglable-actionbar.message");
        this.actionbar_pvp_statuses = this.getStrLs("togglable-actionbar.pvpstatus").toArray(String[]::new);
        this.pvp_status_prefixes = this.getConfig().getStringList("togglable-actionbar.pvp-status-dependent-prefixes").toArray(String[]::new);
        this.pvp_cooldown = this.getConfig().getInt("command-settings.pvp-toggle.cooldown");
        this.pvp_cooldown_prompt = this.getStr("command-settings.pvp-toggle.cooldown-prompt");
        this.reset_pvp_on_quit = this.getBool("command-settings.pvp-toggle.reset-on-quit");
        this.keep_inv_pvp = this.getBool("command-settings.pvp-toggle.keep-inventory-on-pvpdeath");
        this.keep_xp_pvp = this.getBool("command-settings.pvp-toggle.keep-xp-on-pvpdeath");
        this.pvp_toggle_log_to_console = this.getBool("command-settings.pvp-toggle.log-to-console");
        this.console_format = this.getStr("command-settings.pvp-toggle.console-format");
        this.console_pvpstatuses = this.getStrLs("command-settings.pvp-toggle.pvpstatus").toArray(String[]::new);
        this.default_pvp_status = this.getBool("command-settings.pvp-toggle.default-pvp-status");
        this.data_filename = this.getStr("storage.filename");
        this.worldtime_in_world = this.getStr("togglable-actionbar.worldtime-in-world");
        this.reset_combination = this.reset_pvp_on_quit || this.reset_actionbar_on_quit;
        this.pvpcontrol_lines = String.join("\n",this.getStrLs(("command-settings.pvp-control.lines")));
        this.pvpcontrol_personal_lines = String.join("\n",this.getStrLs(("command-settings.pvp-control.personal-lines")));
        this.pvp_on_reminder = this.getStr("togglable-actionbar.reminder-to-enable-pvp");
        this.use_reminder = !this.pvp_on_reminder.isBlank();
    }

    private boolean getBool(final String path) {return this.getConfig().getBoolean(path);}
    private String getStr(final String path) {
        String tmp = this.getConfig().getString(path);
        return tmp!=null?tmp:"";
    }
    private List<String> getStrLs(final String path) {return this.getConfig().getStringList(path);}

    public void loadCommandsConfig() {
        this.commandsConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "commands.yml"));
    }

    public YamlConfiguration commands() { return this.commandsConfig;}

    public boolean prevent_fishing_rods() {return this.prevent_fishing_rods;}
    public boolean prevent_player_damage() {return this.prevent_player_damage;}
    public boolean prevent_projectiles() {return this.prevent_projectiles;}
    public boolean prevent_potions() {return this.prevent_potions;}
    public boolean prevent_combustion() {return this.prevent_combustion;}

    public String data_filename() {return this.data_filename;}

    public String actionbar_message() {return this.actionbar_message;}
    public String[] actionbar_pvp_statuses() {return this.actionbar_pvp_statuses;}
    public String[] actionbar_pvp_prefixes() {return this.pvp_status_prefixes;}

    public boolean pvp_toggle_log_to_console() {return this.pvp_toggle_log_to_console;}
    public String console_format() {return this.console_format;}
    public String[] console_pvpstatuses() {return this.console_pvpstatuses;}
    public int pvp_cooldown() {return this.pvp_cooldown;}
    public String pvp_on_reminder() {return this.pvp_on_reminder;}
    public boolean use_reminder() {return this.use_reminder;}
    public String pvp_cooldown_prompt() {return this.pvp_cooldown_prompt;}
    public boolean default_pvp_status() {return this.default_pvp_status;}
    public boolean keep_inv_pvp() {return this.keep_inv_pvp;}
    public boolean keep_xp_pvp() {return this.keep_xp_pvp;}
    public boolean default_actionbar_status() {return this.default_actionbar_status;}
    public double actionbar_login_duration() {return this.actionbar_login_duration;}
    public double actionbar_attack_duration() {return this.actionbar_attack_duration;}
    public String worldtime_in_world() {return this.worldtime_in_world;}

    public String pvpcontrol_personal_lines() {return this.pvpcontrol_personal_lines;}
    public String pvpcontrol_lines() {return this.pvpcontrol_lines;}

    public boolean toggleHiddenActionbar(final Player p) {
        return TaskManager.toggleHidden(p.getUniqueId());
    }

    @Override
    public void onDisable() {
        TaskManager.stop();
        List<UUID> emptyList = new ArrayList<>();
        if(this.reset_pvp_on_quit != this.reset_actionbar_on_quit || !this.reset_combination) Utils.saveObjects(this.data_filename, this.reset_pvp_on_quit ?emptyList:PVPManager.players(), this.reset_actionbar_on_quit ?emptyList:TaskManager.ignoredValues());
        this.saveConfig();
        this.log.info("Personal PvP DISABLED.");
    }
}
