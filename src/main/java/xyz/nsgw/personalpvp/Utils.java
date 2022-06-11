/*
 * Copyright (c) 2021.
 */

package xyz.nsgw.personalpvp;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.nsgw.personalpvp.config.GeneralConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Utils {

    public static final PotionEffectType[] BAD_EFFECTS = new PotionEffectType[]{
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION,
            PotionEffectType.HARM,
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.WEAKNESS
    };

    private static PPVPPlugin pl;

    private static MiniMessage mm = MiniMessage.miniMessage();
    private static List<List<UUID>> loaded = new ArrayList<>(Arrays.asList(new ArrayList<>(),new ArrayList<>(),new ArrayList<>()));

    public static void setPlugin(final PPVPPlugin plugin) {
        pl = plugin;
    }

    public static void send(final CommandSender sender, final Component component, final boolean text, final boolean actionbar) {
        if (text) {
            sender.sendMessage(component);
        }
        if (actionbar && sender instanceof Player) {
            sender.sendActionBar(component);
        }
    }
    public static void sendText(final Player player, final Component component) {
        player.sendMessage(component);
    }

    public static void send(final Component component) {
        Bukkit.getConsoleSender().sendMessage(component);
    }

    public static Component parse(Player p, String text, final String... placeholders) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPI.setPlaceholders(p, text);
        }
        return getComponent(text, placeholders);
    }

    @NotNull
    public static Component getComponent(String text, String... placeholders) {
        TagResolver.Builder builder = TagResolver.builder();
        for(int i = 0; i < placeholders.length; i += 2) {
            builder.tag(placeholders[i], Tag.inserting(mm.deserialize(placeholders[i + 1])));
        }
        return mm.deserialize(text, builder.build());
    }

    public static Component parse(String text, final String... placeholders) {
        return getComponent(text, placeholders);
    }

    public static void loadObjects() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(new File(pl.getDataFolder(),PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.FILE))));
        List<List<UUID>> objs = new ArrayList<>();
        try {
            Object o = ois.readObject();
            while (o != null) {
                objs.add(new ArrayList<>((List<UUID>) o));
                o = ois.readObject();
            }
        } catch(EOFException e){/*ignore*/}
        ois.close();
        if(objs.size()==2) objs.add(new ArrayList<>());
        loaded = objs;
    }
    public static void saveObjects(final String file, final Object... objects) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pl.getDataFolder(),file)));
            Arrays.stream(objects).forEach(o -> {try {
                    oos.writeObject(o);
                } catch (IOException e) {e.printStackTrace();}
            });
            oos.writeObject(null);
            oos.flush();
            oos.close();
        } catch (IOException e) {e.printStackTrace();}
    }
    public static List<List<UUID>> loaded() {
        return loaded;
    }

    public static boolean togglePersonal(final Player p) {
        if(pl.conf().get().getProperty(GeneralConfig.IS_STATUS_LOCKING)) {
            if (pl.pvp().isLocked(p.getUniqueId())) {
                Utils.sendText(p,Utils.parse("Oops! Your PVP status has been locked."));
                return false;
            }
        }
        if(pl.pvp().coolingDown(p)) {
            int remaining = pl.pvp().getRemainingSeconds(p.getUniqueId());
            Utils.sendText(p, Utils.parse("<red>You can do that again in <yellow><bold><seconds></bold></yellow>.","seconds",remaining+(remaining>1?" seconds":" second")));
            return false;
        }
        pl.pvp().coolDown(p);
        Utils.sendText(p, pl.pvp().toggle(p.getUniqueId()) ? mm.deserialize("<aqua>PVP enabled.") : mm.deserialize("<green>PVP disabled."));
        return true;
    }
    public static class Tameables {
        public static boolean shouldTameablesCancel(final Entity attacker, final Entity defender) {
            if (!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.PREVENT_TAMEDDAMAGE)) return false;
            if (!(attacker instanceof Tameable || defender instanceof Tameable)) return false;
            Tameable animal;
            if (attacker instanceof Tameable) {
                animal = (Tameable) attacker;
                if (animal.getOwner() == null || !(animal.getOwner() instanceof Player)) return false;
                if (PPVPPlugin.inst().pvp().isPvpDisabled(animal.getOwner().getUniqueId())) return true;
                if (defender instanceof Player) {
                    if (PPVPPlugin.inst().pvp().isPvpDisabled(defender.getUniqueId())) return true;
                }
            }
            if (defender instanceof Tameable) {
                animal = (Tameable) defender;
                if (animal.getOwner() == null) return false;
                if (!(animal.getOwner() instanceof Player)) return false;
                if (attacker instanceof Player) {
                    if (animal.getOwner().equals(attacker)) return false;
                }
                return checkOwners((Tameable) defender);
            }
            return false;
        }

        private static boolean checkOwners(Tameable animal) {
            if (animal.getOwner() == null || !(animal.getOwner() instanceof Player)) return false;
            if (PPVPPlugin.inst().pvp().isPvpDisabled(animal.getOwner().getUniqueId())) return true;
            return false;
        }
    }
}
