package xyz.cosmicity.personalpvp.managers;

import org.bukkit.Bukkit;
import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskManager {

    private static List<UUID> ignoredValues = new ArrayList<>();
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
    }private static void setMins(final int mins) {
        minutes = mins;
    }private static void setSuffix(final String str) {
        suffix = str;
    }

    public static void start() {
        actionbarTask = PPVPPlugin.inst().getServer().getScheduler().scheduleSyncRepeatingTask(PPVPPlugin.inst(), () -> {
            PPVPPlugin pl = PPVPPlugin.inst();
            String actionbarMessage = pl.actionbar_message();
            if (actionbarMessage.contains("<worldtime>")) {
                int time = (int) (Bukkit.getWorld(pl.worldtime_in_world()).getTime()) + 6000, tmpHours, tmpMinutes;
                final String sfx = (24000 <= time && time <= 30000) || time <= 12000 ? "am" : "pm";
                tmpHours = (int) (time / 1000f) - 12;
                tmpHours += tmpHours < 1 ? 12 : (tmpHours > 12 ? -12 : 0);
                tmpMinutes = (int) Math.floor((time % 1000) / 16.7);
                setHours(tmpHours);
                setMins(tmpMinutes);
                setSuffix(sfx);
            }
            if(onlineUuids.size()>0 && pl.default_actionbar_status()) {
                onlineUuids.forEach(u -> sendUpdate(u, pl));
            }
            }, 20L, 20L);//17L
    }

    public static boolean ignoredPositive(final UUID u) {
        return PPVPPlugin.inst().default_actionbar_status() == ignoredValues.contains(u);
    }
    public static boolean ignoredNegative(final UUID u) {
        return PPVPPlugin.inst().default_actionbar_status() != ignoredValues.contains(u);
    }

    public static void sendUpdate(final UUID u, final PPVPPlugin pl) {
        if(ignoredPositive(u)) return;
        if(Bukkit.getPlayer(u)==null || ignoredPositive(u)) return;
        sendInstantUpdate(u,pl);
    }

    public static void sendInstantUpdate(final UUID u, final PPVPPlugin pl) {
        Utils.send(Bukkit.getPlayer(u), Utils.parse(pl.actionbar_message(),
                "pvpprefix",
                pl.actionbar_pvp_prefixes()[PVPManager.pvpPositive(u) ? 0 : 1],
                "pvpstatus",
                pl.actionbar_pvp_statuses()[PVPManager.pvpPositive(u) ? 0 : 1],
                "worldtime", (hours<10?"0":"")+hours+":"+(minutes<10?"0":"")+minutes+suffix),
                false, true);
    }

    public static boolean toggleHidden(final UUID uuid) {
        boolean c = ignoredValues.contains(uuid);
        if(c) ignoredValues.remove(uuid);
        else ignoredValues.add(uuid);
        return PPVPPlugin.inst().default_actionbar_status() == c;
    }

    public static void sendJoinDuration(final UUID u, final PPVPPlugin pl) {
        for(int i=0;i<pl.actionbar_login_duration();i++) {
            pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, () -> TaskManager.sendInstantUpdate(u, pl), (i+1) * 20L);
        }
    }

    public static List<UUID> ignoredValues() {return ignoredValues;}

    public static void load() {
        if(Utils.loaded().get(1)!=null) ignoredValues = Utils.loaded().get(1);
    }

    public static void addUuid(final UUID u) {
        onlineUuids.add(u);
    }
    public static void remUuid(final UUID u) {
        onlineUuids.remove(u);
    }

}
