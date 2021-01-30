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

    public static void start() {
        actionbarTask = PPVPPlugin.inst().getServer().getScheduler().scheduleSyncRepeatingTask(PPVPPlugin.inst(), () -> {
            if(onlineUuids.size()>0) {
                PPVPPlugin pl = PPVPPlugin.inst();
                String actionbarMessage = pl.actionbar_message();
                int time = (int) (Bukkit.getWorld(pl.worldtime_in_world()).getTime()) + 6000, tmpHours = 0, tmpMinutes = 0;
                final String suffix = (24000 <= time && time <= 30000) || time <= 12000 ? "am" : "pm";
                if (actionbarMessage.contains("<worldtime>")) {
                    tmpHours = (int) (time / 1000f) - 12;
                    tmpHours += tmpHours < 1 ? 12 : (tmpHours > 12 ? -12 : 0);
                    tmpMinutes = (int) Math.floor((time % 1000) / 16.7);
                }
                final int hours = tmpHours, minutes = tmpMinutes;
                onlineUuids.forEach(u -> sendUpdate(u, pl, hours, minutes, suffix, actionbarMessage));
            }
            }, 20L, 20L);//17L
    }

    public static boolean ignoredPositive(final UUID u) {
        return PPVPPlugin.inst().default_actionbar_status() == ignoredValues.contains(u);
    }

    public static void sendUpdate(final UUID u, final PPVPPlugin pl, final int hours, final int minutes, final String suffix, final String actionbarMessage) {
        if(ignoredPositive(u)) return;
        if(Bukkit.getPlayer(u)==null || ignoredPositive(u)) return;
        Utils.send(Bukkit.getPlayer(u), Utils.parse(actionbarMessage,
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
