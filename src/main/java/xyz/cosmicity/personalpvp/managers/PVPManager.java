package xyz.cosmicity.personalpvp.managers;

import org.bukkit.entity.Player;
import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;

import java.util.*;

public class PVPManager {

    private static List<UUID> players = new ArrayList<>();

    private static final HashMap<UUID,Long> cooldowns = new HashMap<>();

    public static List<UUID> players() {return players;}

    public static boolean pvpPositive(final UUID uuid) {
        return PPVPPlugin.inst().default_pvp_status() != players.contains(uuid);
    }
    public static boolean pvpNegative(final UUID uuid) {
        return PPVPPlugin.inst().default_pvp_status() == players.contains(uuid);
    }

    public static void remove(final UUID uuid) {
        players.remove(uuid);
    }

    public static void reset(final UUID uuid) {
        if(pvpPositive(uuid) != PPVPPlugin.inst().default_pvp_status()) toggle(uuid);
    }

    public static boolean toggle(final UUID uuid) {
        boolean c = players.contains(uuid);
        if(c) players.remove(uuid);
        else players.add(uuid);
        return PPVPPlugin.inst().default_pvp_status() == c;
    }

    public static boolean isPvpEnabled(final UUID uuid) {
        return PPVPPlugin.inst().default_pvp_status() != players.contains(uuid);
    }
    public static boolean isPvpDisabled(final UUID uuid) {
        return PPVPPlugin.inst().default_pvp_status() == players.contains(uuid);
    }
    public static boolean isEitherNegative(final UUID u1, final UUID u2) {
        return pvpNegative(u1)||pvpNegative(u2);
    }

    public static void load() {
        if(Utils.loaded().get(0) != null) players = Utils.loaded().get(0);
    }

    public static void coolDown(final Player p){
        if(p.hasPermission(PPVPPlugin.inst().commands().getString("pvp.permission")+".bypass")) return;
        cooldowns.put(p.getUniqueId(), new Date().getTime());
    }

    public static boolean coolingDown(final Player p) {
        if(p.hasPermission(PPVPPlugin.inst().commands().getString("pvp.permission")+".bypass")) return false;
        UUID uuid = p.getUniqueId();
        if(!cooldowns.containsKey(uuid)) return false;
        if(getRemainingSeconds(uuid)<=0) {
            cooldowns.remove(uuid);
            return false;
        }
        return (new Date().getTime()-cooldowns.get(uuid))> PPVPPlugin.inst().pvp_cooldown();
    }
    public static int getRemainingSeconds(final UUID uuid) {
        return (int) Math.ceil(Double.parseDouble(Long.toString(((PPVPPlugin.inst().pvp_cooldown()* 1000L)-(new Date().getTime()-cooldowns.get(uuid)))/1000)));
    }

}
