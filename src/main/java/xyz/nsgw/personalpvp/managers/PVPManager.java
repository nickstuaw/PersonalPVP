/*
 * Copyright (c) 2021.
 */

package xyz.nsgw.personalpvp.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.nsgw.personalpvp.PPVPPlugin;
import xyz.nsgw.personalpvp.Utils;
import xyz.nsgw.personalpvp.config.GeneralConfig;

import java.util.*;

public class PVPManager {

    private List<UUID> differentPlayers = new ArrayList<>(),
            lockedPlayers = new ArrayList<>();

    private final HashMap<UUID,Long> cooldowns = new HashMap<>();

    public PVPManager() {}

    public List<UUID> players() {return differentPlayers;}
    public List<UUID> lockedPlayers() {return lockedPlayers;}

    public boolean pvpPositive(final UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if(p != null) {
            if(!p.isOp()) {
                if (p.hasPermission("personalpvp.always.on")) {
                    return !PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
                } else if (p.hasPermission("personalpvp.always.off")) {
                    return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
                }
            }
        }
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) != differentPlayers.contains(uuid);
    }
    public boolean pvpNegative(final UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if(p != null) {
            if(!p.isOp()) {
                {
                    if (p.hasPermission("personalpvp.always.on")) {
                        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
                    } else if (p.hasPermission("personalpvp.always.off")) {
                        return !PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
                    }
                }
            }
        }
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) == differentPlayers.contains(uuid);
    }
    public boolean isLocked(final UUID uuid) {
        return lockedPlayers.contains(uuid);
    }
    public boolean toggleLocked(final UUID uuid) {
        if(lockedPlayers.contains(uuid)) lockedPlayers.remove(uuid);
        else lockedPlayers.add(uuid);
        return lockedPlayers.contains(uuid);
    }

    public void remove(final UUID uuid) {
        differentPlayers.remove(uuid);
    }

    public void reset(final UUID uuid) {
        if(pvpPositive(uuid) != PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS)) toggle(uuid);
    }

    public boolean toggle(final UUID uuid) {
        boolean c = differentPlayers.contains(uuid);
        if(c) differentPlayers.remove(uuid);
        else differentPlayers.add(uuid);
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) == c;
    }

    public boolean isPvpEnabled(final UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if(p != null) {
            if(!p.isOp()) {
                if (p.hasPermission("personalpvp.always.on")) {
                    return true;
                } else if (p.hasPermission("personalpvp.always.off")) {
                    return false;
                }
            }
        }
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) != differentPlayers.contains(uuid);
    }
    public boolean isPvpDisabled(final UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if(p != null) {
            if(!p.isOp()) {
                if (p.hasPermission("personalpvp.always.on")) {
                    return true;
                } else if (p.hasPermission("personalpvp.always.off")) {
                    return false;
                }
            }
        }
        return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) == differentPlayers.contains(uuid);
    }
    public boolean isEitherNegative(final UUID u1, final UUID u2) {
        return pvpNegative(u1)||pvpNegative(u2);
    }

    public void load() {
        if(!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.RESET_PVP_ON_QUIT) && Utils.loaded().get(0) != null) differentPlayers = Utils.loaded().get(0);
        if(!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DO_STATUS_LOCKS_RESET_ON_QUIT) && PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.IS_STATUS_LOCKING)) lockedPlayers = Utils.loaded().get(2);
    }

    public void coolDown(final Player p){
        if(p.hasPermission("pvp.cooldown.bypass")) return;
        cooldowns.put(p.getUniqueId(), new Date().getTime());
    }

    public boolean coolingDown(final Player p) {
        if(p.hasPermission("pvp.cooldown.bypass")) return false;
        UUID uuid = p.getUniqueId();
        if(!cooldowns.containsKey(uuid)) return false;
        if(getRemainingSeconds(uuid)<=0) {
            cooldowns.remove(uuid);
            return false;
        }
        return (new Date().getTime()-cooldowns.get(uuid))> PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_COOLDOWN);
    }
    public int getRemainingSeconds(final UUID uuid) {
        return (int) Math.ceil(Double.parseDouble(Long.toString(((PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_COOLDOWN) * 1000L)-(new Date().getTime()-cooldowns.get(uuid)))/1000)));
    }

}
