/*
 * Copyright (c) 2021.
 */

package xyz.nsgw.personalpvp.managers;

import org.bukkit.entity.Player;
import xyz.nsgw.personalpvp.PPVPPlugin;
import xyz.nsgw.personalpvp.Utils;
import xyz.nsgw.personalpvp.config.GeneralConfig;

import java.util.*;

public class PVPManager {

    private static List<UUID> players = new ArrayList<>(),
            lockedPlayers = new ArrayList<>();

    private static final HashMap<UUID,Long> cooldowns = new HashMap<>();

    public static List<UUID> players() {return players;}
    public static List<UUID> lockedPlayers() {return lockedPlayers;}

    public static boolean pvpPositive(final UUID uuid) {
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) != players.contains(uuid);
    }
    public static boolean pvpNegative(final UUID uuid) {
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) == players.contains(uuid);
    }
    public static boolean isLocked(final UUID uuid) {
        return lockedPlayers.contains(uuid);
    }
    public static boolean toggleLocked(final UUID uuid) {
        if(lockedPlayers.contains(uuid)) lockedPlayers.remove(uuid);
        else lockedPlayers.add(uuid);
        return lockedPlayers.contains(uuid);
    }

    public static void remove(final UUID uuid) {
        players.remove(uuid);
    }

    public static void reset(final UUID uuid) {
        if(pvpPositive(uuid) != PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS)) toggle(uuid);
    }

    public static boolean toggle(final UUID uuid) {
        boolean c = players.contains(uuid);
        if(c) players.remove(uuid);
        else players.add(uuid);
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) == c;
    }

    public static boolean isPvpEnabled(final UUID uuid) {
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) != players.contains(uuid);
    }
    public static boolean isPvpDisabled(final UUID uuid) {
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) == players.contains(uuid);
    }
    public static boolean isEitherNegative(final UUID u1, final UUID u2) {
        return pvpNegative(u1)||pvpNegative(u2);
    }

    public static void load() {
        if(!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.RESET_PVP_ON_QUIT) && Utils.loaded().get(0) != null) players = Utils.loaded().get(0);
        if(!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DO_STATUS_LOCKS_RESET_ON_QUIT) && PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.IS_STATUS_LOCKING)) lockedPlayers = Utils.loaded().get(2);
    }

    public static void coolDown(final Player p){
        if(p.hasPermission("pvp.cooldown.bypass")) return;
        cooldowns.put(p.getUniqueId(), new Date().getTime());
    }

    public static boolean coolingDown(final Player p) {
        if(p.hasPermission("pvp.cooldown.bypass")) return false;
        UUID uuid = p.getUniqueId();
        if(!cooldowns.containsKey(uuid)) return false;
        if(getRemainingSeconds(uuid)<=0) {
            cooldowns.remove(uuid);
            return false;
        }
        return (new Date().getTime()-cooldowns.get(uuid))> PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_COOLDOWN);
    }
    public static int getRemainingSeconds(final UUID uuid) {
        return (int) Math.ceil(Double.parseDouble(Long.toString(((PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_COOLDOWN) * 1000L)-(new Date().getTime()-cooldowns.get(uuid)))/1000)));
    }

}
