/*
 * Copyright (c) 2022.
 */

package com.nsgwick.personalpvp.managers;

import com.nsgwick.personalpvp.PPVPPlugin;
import com.nsgwick.personalpvp.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.nsgwick.personalpvp.config.GeneralConfig;

import java.util.*;

public class PVPManager {

    /*
    alteredPlayers stores players whose statuses are the opposite of the default specified in config.yml.
    lockedPlayers stores players whose statuses are locked (cannot be changed by themselves).
     */
    private List<UUID> alteredPlayers = new ArrayList<>(),
            lockedPlayers = new ArrayList<>();

    /*
    cooldowns stores the timestamp of each player's last attempt to change their pvp status.
     */
    private final HashMap<UUID,Long> cooldowns = new HashMap<>();

    /*
    Constructor
     */
    public PVPManager() {}

    /**
     * Get the list of altered players.
     * @return The list of players who have the opposite pvp status.
     */
    public List<UUID> alteredPlayers() {return alteredPlayers;}
    /**
     * Get the list of locked players.
     * @return The list of players who have a locked pvp status.
     */
    public List<UUID> lockedPlayers() {return lockedPlayers;}

    /**
     * Check whether a player has PVP enabled.
     * @param uuid The uuid of the player to check.
     * @return true if PVP is positive for the uuid.
     */
    public boolean pvpPositive(final UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        /*
        If the player is online, continue.
         */
        if(p != null) {
            /*
            If the player isn't an operator, look for permissions.
             */
            if(!p.isOp()) {
                if (p.hasPermission("personalpvp.always.on")) {
                        /*
                        PVP alteration is permanently on so their status is the default PVP status.
                        This is checking if pvp is positive so return the altered status because
                        if the default status is false, and they have this permission pvp is on.
                        This method returns true if pvp is on.
                         */
                    return !PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
                } else if (p.hasPermission("personalpvp.always.off")) {
                        /*
                        PVP alteration is permanently off so their status is the opposite of the default PVP status.
                        This is checking if pvp is positive so return the default status because
                        if the default status is true, and they have this permission pvp is on.
                        This method returns true if pvp is on.
                         */
                    return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
                }
            }
        }
        /*
        Returns true if the player has pvp enabled.
        If default status is true and player isn't altered, player has pvp enabled.
        If default status is true and player is altered, player has pvp disabled.
        If default status is false and player isn't altered, player has PVP disabled.
        If default status is false and player is altered, player has PVP enabled.
         */
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) != alteredPlayers.contains(uuid);
    }

    /**
     * Check whether a player has PVP disabled.
     * @param uuid The uuid of the player to check.
     * @return true if PVP is negative for the uuid.
     */
    public boolean pvpNegative(final UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        /*
        If the player is online, continue.
         */
        if(p != null) {
            /*
            If the player isn't an operator, look for permissions.
             */
            if(!p.isOp()) {
                {
                    if (p.hasPermission("personalpvp.always.on")) {
                        /*
                        PVP alteration is permanently on so always return the default PVP status.
                        If default pvp status is true and player has this permission, pvp is disabled.
                        If default pvp status is false and player has this permission, pvp is enabled.
                         */
                        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
                    } else if (p.hasPermission("personalpvp.always.off")) {
                        /*
                        PVP alteration is permanently off so always return the opposite of the default PVP status.
                        If default pvp status is true and player has this permission, pvp is enabled.
                        If default pvp status is false and player has this permission, pvp is disabled.
                         */
                        return !PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
                    }
                }
            }
        }
        /*
        Returns true if the player has pvp disabled.
        If default status is true and player is altered, player has pvp disabled.
        If default status is true and player isn't altered, player has pvp enabled.
        If default status is false and player is altered, player has PVP enabled.
        If default status is false and player isn't altered, player has PVP disabled.
         */
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) == alteredPlayers.contains(uuid);
    }

    /**
     * Check whether a player has a locked PVP status.
     * @param uuid The uuid of the player to check.
     * @return true if the player's status is locked.
     */
    public boolean isLocked(final UUID uuid) {
        /*
        lockedPlayers contains players with locked statuses.
         */
        return lockedPlayers.contains(uuid);
    }

    /**
     * Toggle the status lock for a player.
     * @param uuid The uuid of the player to be toggled.
     * @return True if the status has been locked - false if unlocked.
     */
    public boolean toggleLocked(final UUID uuid) {
        /*
        If the player's status is locked, unlock it by removing them from the locked list.
         */
        if(lockedPlayers.contains(uuid)) lockedPlayers.remove(uuid);
        /*
        If the player's status is unlocked, lock it by adding them to the locked list.
         */
        else lockedPlayers.add(uuid);
        /*
        Return true if the player is now locked; false otherwise.
         */
        return lockedPlayers.contains(uuid);
    }

    /**
     * Remove a player from the list of players with the opposite status of the default status.
     * @param uuid The uuid of the player to remove (reset the status of).
     */
    public void remove(final UUID uuid) {
        /*
        The player is no longer recognised as having an altered PVP status.
         */
        alteredPlayers.remove(uuid);
    }

    /**
     * Reset the PVP status of a player to the default PVP status displayed in the config.
     * @param uuid The uuid of the player to reset.
     */
    public void reset(final UUID uuid) {
        /*
        If they have an altered PVP status, toggle it.
         */
        if(pvpPositive(uuid) != PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS)) toggle(uuid);
    }

    /**
     * Toggle the PVP status of a player.
     * @param uuid The uuid of the player to toggle.
     * @return True if the player is now altered, false if they are now the default status.
     */
    public boolean toggle(final UUID uuid) {
        /*
        Store the current alteration status of the player. (True if changing from altered to unaltered).
         */
        boolean wasAltered = alteredPlayers.contains(uuid);
        /*
        If the player was originally altered, remove them from the list of altered players to toggle their status.
         */
        if(wasAltered) alteredPlayers.remove(uuid);
        /*
        If the player wasn't originally altered, add them to the list of altered players to toggle their status.
         */
        else alteredPlayers.add(uuid);
        /*
        Return true if the player is now in the altered list; false if they now have the default pvp status.
         */
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) == wasAltered;
    }

    /**
     * Check if either player has PVP disabled.
     * @param u1 First player.
     * @param u2 Second player.
     * @return True if either has PVP disabled, false if both have PVP enabled.
     */
    public boolean isEitherNegative(final UUID u1, final UUID u2) {
        /*
        Return true if u1 or u2 has pvp disabled.
         */
        return pvpNegative(u1)||pvpNegative(u2);
    }

    /*
    Load the alteredPlayers and lockedPlayers lists from the data in Utils.
     */
    public void load() {
        /*
        If players don't have their status reset on quit and the data exists, load the data into alteredPlayers.
         */
        if(!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.RESET_PVP_ON_QUIT)
                && Utils.loaded().get(0) != null) alteredPlayers = Utils.loaded().get(0);
        /*
        If players don't have their status locks reset on quit and locking is enabled, load the data into lockedPlayers.
         */
        if(!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DO_STATUS_LOCKS_RESET_ON_QUIT)
                && PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.IS_STATUS_LOCKING))
            lockedPlayers = Utils.loaded().get(2);
    }

    /**
     * Update the cooldown timer for the player (permission check included).
     * @param p The player to update the cooldown timer of.
     */
    public void coolDown(final Player p){
        /*
        If the player has the cooldown bypass permission, forcefully bypass the cooldown.
         */
        if(p.hasPermission("pvp.cooldown.bypass")) return;
        cooldowns.put(p.getUniqueId(), new Date().getTime());
    }

    /**
     * Check if a player is still cooling down.
     * @param p The player to check the cooldwon of.
     * @return true if the cooldown is in progress; false if it isn't.
     */
    public boolean coolingDown(final Player p) {
        /*
        If the player has the cooldown bypass permission, forcefully bypass the cooldown.
         */
        if(p.hasPermission("pvp.cooldown.bypass")) return false;
        /*
        Get the player's uuid.
         */
        UUID uuid = p.getUniqueId();
        /*
        If the player is not cooling down, return false for no cooldown.
         */
        if(!cooldowns.containsKey(uuid)) return false;
        /*
        If the player isn't cooling down anymore, return false and remove them from the cooldown list.
         */
        if(getRemainingSeconds(uuid)<=0) {
            cooldowns.remove(uuid);
            return false;
        }
        /*
        If the time between now and their last pvp status interaction is larger than the cooldown, return true.
         */
        return (new Date().getTime() - cooldowns.get(uuid)) >
                PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_COOLDOWN);
    }

    /**
     * Get the remaining seconds of a player's cooldown.
     * @param uuid The uuid of the player to find the remaining seconds of.
     * @return The remaining seconds of the player's cooldown.
     */
    public int getRemainingSeconds(final UUID uuid) {
        /*
        Round up the difference in seconds to simplify it.
        Calculation:
        Multiply the cooldown seconds by 1000 to convert them to millis.
        Cooldown limit - cooldown millis = remaining millis (convert this to seconds by dividing by 1000)
         */
        return (int) Math.ceil(
                Double.parseDouble(
                        Long.toString(
                                ((PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_COOLDOWN)
                                        * 1000L)
                                        - (new Date().getTime() - cooldowns.get(uuid))) / 1000)));
    }

}
