package xyz.cosmicity.personalpvp;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;
import xyz.cosmicity.personalpvp.storage.Message;

import java.util.List;

public class Config {

    public static void setCommands(final YamlConfiguration conf) {
        commands = conf;
    }
    public static void setLang(final YamlConfiguration conf) {
        lang = conf;
    }

    public static final double CONFIG_VERSION = 1.2, LANG_VERSION = 1.0, COMMANDS_VERSION = 1.2;
    private static double version;
    private static boolean toggleable_actionbar_enabled, pvp_toggle_log_to_console;
    private static String[] ab_pvpstatus_placeholder_values, pvp_status_prefixes, console_pvpstatus_placeholder_values;
    private static String data_filename, worldtime_in_world,  pvpcontrol_personal_lines, pvpcontrol_lines, ab_message, console_format;
    private static Message locked_message, pvp_cooldown_prompt, pvp_on_reminder, pvp_enabled_message, pvp_disabled_message;
    private static boolean default_pvp_status, use_locking, keep_xp_pvp, keep_inv_pvp, ab_status_default, prevent_fishing_rods, prevent_player_damage, prevent_projectiles, prevent_potions, prevent_combustion,
            rs_combination, rs_actionbar_on_quit, rs_pvp_on_quit, rs_locks_on_quit;
    private static double ab_duration_attack, ab_duration_login;
    private static int pvp_cooldown;

    private static FileConfiguration config;
    private static YamlConfiguration lang, commands;

    public static void load(final FileConfiguration conf) {
        config = conf;

        version = getDbl("version");
        data_filename = getStr("storage-file");
        rs_actionbar_on_quit = getBool("toggleable-actionbar.reset-actionbar-status-on-quit");
        rs_pvp_on_quit = getBool("command-settings.pvp-toggle.reset-on-quit");
        rs_locks_on_quit = getBool("command-settings.pvp-toggle.reset-locks-on-quit");
        rs_combination = !rs_locks_on_quit || !rs_pvp_on_quit || !rs_actionbar_on_quit;

        prevent_fishing_rods = getBool("prevent.fishing-rods");
        prevent_player_damage = getBool("prevent.damage-from-players");
        prevent_projectiles = getBool("prevent.throwable-projectiles");
        prevent_potions = getBool("prevent.potions");
        prevent_combustion = getBool("prevent.combustion-from-players");

        toggleable_actionbar_enabled = getBool("toggleable-actionbar.enable");
        ab_status_default = getBool("toggleable-actionbar.default-actionbar-status");
        ab_duration_attack = getDbl("toggleable-actionbar.show-on-login-duration-seconds");
        ab_duration_login = getDbl("toggleable-actionbar.show-on-attack-duration-seconds");
        ab_message = getStr("toggleable-actionbar.message");
        ab_pvpstatus_placeholder_values = getStrLs("toggleable-actionbar.pvp-status").toArray(String[]::new);
        pvp_status_prefixes = getStrLs("toggleable-actionbar.pvp-status-dependent-prefixes").toArray(String[]::new);
        worldtime_in_world = getStr("toggleable-actionbar.worldtime-in-world");

        pvp_cooldown = config.getInt("command-settings.pvp-toggle.cooldown");
        keep_inv_pvp = getBool("command-settings.pvp-toggle.keep-inventory-on-pvpdeath");
        keep_xp_pvp = getBool("command-settings.pvp-toggle.keep-xp-on-pvpdeath");
        pvp_toggle_log_to_console = getBool("command-settings.pvp-toggle.log-to-console");
        console_format = getStr("command-settings.pvp-toggle.console-format");
        console_pvpstatus_placeholder_values = getStrLs("command-settings.pvp-toggle.pvpstatus").toArray(String[]::new);
        default_pvp_status = getBool("command-settings.pvp-toggle.default-pvp-status");
        pvpcontrol_lines = String.join("\n",getStrLs(("command-settings.pvp-control.lines")));
        pvpcontrol_personal_lines = String.join("\n",getStrLs(("command-settings.pvp-control.personal-lines")));

        pvp_enabled_message = getMessage("enabled-pvp");
        pvp_disabled_message = getMessage("disabled-pvp");
        pvp_cooldown_prompt = getMessage("cooldown-prompt");
        locked_message = getMessage("locked-message");
        pvp_on_reminder = getMessage("reminder-to-enable-pvp");

        use_locking = getBool("command-settings.pvp-toggle.locking-enabled");
    }

    public static boolean getBool(final String path) {return config.getBoolean(path);}
    private static String getStr(final String path) {
        String tmp = config.getString(path);
        return tmp != null ? tmp : "";
    }
    private static double getDbl(final String path) {
        return config.getDouble(path);
    }
    private static List<String> getStrLs(final String path) {return config.getStringList(path);}
    private static ConfigurationSection getConfSect(final String path) {return config.getConfigurationSection(path);}
    private static ConfigurationSection getLangConfSect(final String path) {return lang.getConfigurationSection(path);}
    private static Message getMessage(final String label) {
        return new Message(getLangConfSect("messages."+label));
    }

    public static double version() {return version;}

    public static boolean prevent_fishing_rods() {return prevent_fishing_rods;}
    public static boolean prevent_player_damage() {return prevent_player_damage;}
    public static boolean prevent_projectiles() {return prevent_projectiles;}
    public static boolean prevent_potions() {return prevent_potions;}
    public static boolean prevent_combustion() {return prevent_combustion;}

    public static String data_filename() {return data_filename;}

    public static boolean enable_toggleable_actionbar() {return toggleable_actionbar_enabled;}

    public static String actionbar_message() {return ab_message;}
    public static String[] actionbar_pvp_statuses() {return ab_pvpstatus_placeholder_values;}
    public static String[] actionbar_pvp_prefixes() {return pvp_status_prefixes;}

    public static boolean rs_pvp_on_quit() {return rs_pvp_on_quit;}
    public static boolean rs_locks_on_quit() {return rs_locks_on_quit;}

    public static boolean rs_actionbar_on_quit() {return rs_actionbar_on_quit;}

    public static boolean no_reset_for_any() {return rs_combination;}

    public static boolean pvp_toggle_log_to_console() {return pvp_toggle_log_to_console;}
    public static String console_format() {return console_format;}
    public static String[] console_pvpstatuses() {return console_pvpstatus_placeholder_values;}
    public static int pvp_cooldown() {return pvp_cooldown;}

    public static Message pvp_on_reminder() {return pvp_on_reminder;}
    public static Message pvp_enabled_message() {return pvp_enabled_message;}
    public static Message pvp_disabled_message() {return pvp_disabled_message;}
    public static Message pvp_cooldown_prompt() {return pvp_cooldown_prompt;}
    public static Message locked_message() {return locked_message;}
    public static boolean use_locking() {return use_locking;}
    public static boolean default_pvp_status() {return default_pvp_status;}
    public static boolean keep_inv_pvp() {return keep_inv_pvp;}
    public static boolean keep_xp_pvp() {return keep_xp_pvp;}
    public static boolean default_actionbar_status() {return ab_status_default;}
    public static double actionbar_login_duration() {return ab_duration_attack;}
    public static double actionbar_attack_duration() {return ab_duration_login;}
    public static String worldtime_in_world() {return worldtime_in_world;}

    public static String pvpcontrol_personal_lines() {return pvpcontrol_personal_lines;}
    public static String pvpcontrol_lines() {return pvpcontrol_lines;}

    public static YamlConfiguration commands(){return commands;}
    public static YamlConfiguration lang(){return lang;}
}
