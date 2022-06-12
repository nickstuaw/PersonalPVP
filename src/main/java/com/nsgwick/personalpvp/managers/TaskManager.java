/*
 * Copyright (c) 2022.
 */

package com.nsgwick.personalpvp.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import com.nsgwick.personalpvp.PPVPPlugin;
import com.nsgwick.personalpvp.Utils;
import com.nsgwick.personalpvp.config.GeneralConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TaskManager {

    /**
    Stores players with an altered actionbar status.
     */
    private static List<UUID> playersWithAlteredActionbar = new ArrayList<>();
    /**
     * Stores the online players.
     */
    private static final List<UUID> onlineUuids = new ArrayList<>();

    private static int actionbarTask= -1;

    public static void stop() {
        if(actionbarTask==-1) return;
        PPVPPlugin.inst().getServer().getScheduler().cancelTask(actionbarTask);
    }

    private static int hours = 0, minutes = 0;
    private static String suffix = "";
    private static void setHours(final int hrs) {
        hours = hrs;
    }
    private static void setMins(final int mins) {
        minutes = mins;
    }
    private static void setSuffix(final String str) {
        suffix = str;
    }

    public static void start() {
        /*
        Start the repeating actionbar task.
         */
        actionbarTask = PPVPPlugin.inst().getServer().getScheduler().scheduleSyncRepeatingTask(PPVPPlugin.inst(), () -> {
            String actionbarMessage = PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.ABAR_MESSAGE);
            /*
            If worldtime will be shown and the world specified in the config exists, save it.
             */
            if (actionbarMessage.contains("<worldtime>")) {
                /*
                Fetch the world object.
                 */
                World world = Bukkit.getWorld(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.ABAR_TIME_WORLD));
                /*
                If the world exists,
                 */
                if(world != null) {
                    /*
                    Save the world time
                     */
                    int time = (int) (world.getTime()) + 6000, tmpHours, tmpMinutes;
                    /*
                    Determine the suffix.
                     */
                    final String sfx = (24000 <= time && time <= 30000) || time <= 12000 ? "am" : "pm";
                    /*
                    Set the hours and minutes.
                     */
                    tmpHours = (int) (time / 1000f) - 12;
                    tmpHours += tmpHours < 1 ? 12 : (tmpHours > 12 ? -12 : 0);
                    tmpMinutes = (int) Math.floor((time % 1000) / 16.7);
                    setHours(tmpHours);
                    setMins(tmpMinutes);
                    setSuffix(sfx);
                }
            }
            /*
            If anyone is online,
             */
            if(onlineUuids.size()>0) {
                /*
                send the actionbar.
                 */
                onlineUuids.stream().filter(TaskManager::isPlayerActionbarShown).forEach(TaskManager::sendUpdate);
            }
            /*
            Repeat every 17 ticks.
            1 second = 20L.
             */
            }, 20L, 17L);//17L
    }

    /**
     * Check if a player has the actionbar hidden.
     * @param u The uuid of the player to check.
     * @return True if it's hidden; false if shown.
     */
    public static boolean isPlayerActionbarHidden(final UUID u) {
        /*
        If default visibility is true and player is ignored, ignore.
        If default visibility is false and player is ignored, pardon.
         */
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.ABAR_DEFAULT_VISIBILITY)
                == playersWithAlteredActionbar.contains(u);
    }
    /**
     * Check if a player has the actionbar shown.
     * @param u The uuid of the player to check.
     * @return True if it's shown; false if hidden.
     */
    public static boolean isPlayerActionbarShown(final UUID u) {
        /*
        If default visibility is true and player is ignored, return false (ignoring).
        If default visibility is false and player is ignored, return true (not ignoring).
         */
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.ABAR_DEFAULT_VISIBILITY)
                != playersWithAlteredActionbar.contains(u);
    }

    /**
     * Try to send an actionbar update.
     * @param u The uuid of the player.
     */
    public static void sendUpdate(final UUID u) {
        if(isPlayerActionbarHidden(u)) return;
        if(Bukkit.getPlayer(u)==null || isPlayerActionbarHidden(u)) return;
        sendActionbarUpdate(u);
    }

    /**
     * Send an actionbar.
     * @param u The uuid of the player to send the actionbar to. They MUST be online.
     */
    public static void sendActionbarUpdate(final UUID u) {
        PPVPPlugin pl = PPVPPlugin.inst();
        /*
        Send the actionbar.
         */
        Utils.send(Bukkit.getPlayer(u),
                Utils.parse(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.ABAR_MESSAGE),
                "pvpprefix",
                        pl.pvp().pvpPositive(u) ?
                                pl.conf().get().getProperty(GeneralConfig.ABAR_PVP_ENABLED_PFX) :
                                pl.conf().get().getProperty(GeneralConfig.ABAR_PVP_DISABLED_PFX),
                "worldtime", (hours<10?"0":"")+hours+":"+(minutes<10?"0":"")+minutes+suffix),
                false, true);
    }

    /**
     * Toggle the actionbar of a player.
     * @param uuid The UUID of the player to toggle the actionbar of.
     * @return True if now default status, false if status has been altered.
     */
    public static boolean toggleHiddenActionbar(final UUID uuid) {
        boolean c = playersWithAlteredActionbar.contains(uuid);
        if(c) playersWithAlteredActionbar.remove(uuid);
        else playersWithAlteredActionbar.add(uuid);
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.ABAR_DEFAULT_VISIBILITY) == c;
    }

    public static void sendJoinDuration(final UUID u, final PPVPPlugin pl) {
        for(int i=0;i<PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.ABAR_LOGIN_VISIBILITY_DURATION)+1;i++) {
            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, () -> TaskManager.sendActionbarUpdate(u),
                    i * 20L);
        }
    }

    /**
     * Run a pvp alert.
     * @param us uuids of those involved.
     */
    public static void blockedAttack(final UUID... us) {
        /*
        If the pvp alert is disabled in the configuration, skip the alert.
         */
        if(!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.ENABLE_PVP_ALERT)) return;
        /*
        Get the plugin instance for pvp status access.
         */
        PPVPPlugin pl = PPVPPlugin.inst();
        /*
        For each online player,
         */
        for(UUID u : us) {
            /*
            If the player has the actionbar hidden and pvp disabled,
             */
            if (isPlayerActionbarHidden(u) && pl.pvp().pvpNegative(u)) {
                /*
                Notify them in chat.
                 */
                Utils.sendText(Objects.requireNonNull(Bukkit.getPlayer(u)),
                        Utils.parse("<#ed4213>Use <red><bold>/pvp<#ed4213> to enable pvp."));
            }
        }
    }

    /**
     * Get the list of player uuids with an altered actionbar.
     * @return The list of player uuids with an altered actionbar.
     */
    public static List<UUID> playersWithAlteredActionbar() {return playersWithAlteredActionbar;}

    /**
     * Load the list of players with an altered actionbar from Utils.
     */
    public static void load() {
        if(Utils.loaded().get(1)!=null) playersWithAlteredActionbar = Utils.loaded().get(1);
    }

    /**
     * Add a player's uuid to the list of recognised online uuids.
     * @param u The uuid of the player.
     */
    public static void addOnlineUuid(final UUID u) {
        onlineUuids.add(u);
    }
    /**
     * Remove a player's uuid from the list of recognised online uuids.
     * @param u The uuid of the player.
     */
    public static void removeOnlineUuid(final UUID u) {
        onlineUuids.remove(u);
    }

}
