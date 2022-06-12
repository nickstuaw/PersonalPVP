/*
 * Copyright (c) 2022.
 */

package com.nsgwick.personalpvp;

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
import com.nsgwick.personalpvp.config.GeneralConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Utils {

    /**
    The list of potion effects to block.
     */
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

    /**
    The instance of the main plugin.
     */
    private static PPVPPlugin pl;

    /**
    MiniMessage is used for coloured messages.
     */
    private static MiniMessage mm = MiniMessage.miniMessage();
    /**
    A list of the data loaded from the file.
     */
    private static List<List<UUID>> loaded = new ArrayList<>(Arrays.asList(
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()));

    /**
    Save the plugin instance.
     */
    public static void setPlugin(final PPVPPlugin plugin) {
        pl = plugin;
    }

    /**
     * Send a text component to a command sender as text or an actionbar.
     * @param sender The CommandSender to send the text component to.
     * @param component The text component to send.
     * @param text Send the component in chat?
     * @param actionbar Send the component as an actionbar?
     */
    public static void send(final CommandSender sender, final Component component, final boolean text,
                            final boolean actionbar) {
        /*
        If the component will be sent in chat,
         */
        if (text) {
            /*
            Send the component in chat.
             */
            sender.sendMessage(component);
        }
        /*
        If the target is a player and the component is being sent as an actionbar,
         */
        if (actionbar && sender instanceof Player) {
            /*
            Send the actionbar.
             */
            sender.sendActionBar(component);
        }
    }

    /**
     * Send a component in chat to a player.
     * @param player The online player to send it to.
     * @param component The text component to send.
     */
    public static void sendText(final Player player, final Component component) {
        /*
        Send the component to the player in chat.
         */
        player.sendMessage(component);
    }

    /**
     * Send a text component to the console.
     * @param component The text component to send.
     */
    public static void send(final Component component) {
        /*
        Send the component to the console.
         */
        Bukkit.getConsoleSender().sendMessage(component);
    }

    /**
     * Parse and send MiniMessage format with custom placeholders. Include PAPI.
     * @param p The player to pass to PAPI.
     * @param text The raw text with MiniMessage tags.
     * @param placeholders Custom placeholders (in pairs)... E.g. value_to_find, replace_with_this, etc.
     * @return The resulting component.
     */
    public static Component parse(Player p, String text, final String... placeholders) {
        /*
        If PlaceholderAPI is on the server,
         */
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
            Set the placeholders.
             */
            text = PlaceholderAPI.setPlaceholders(p, text);
        }
        /*
        Parse and return the component.
         */
        return getComponent(text, placeholders);
    }

    /**
     * Insert placeholders and deserialize text into a MiniMessage component.
     * @param text The raw text.
     * @param placeholders The placeholder (ordered in pairs). E.g. value_to_find, replace_with_this, etc.
     * @return The resulting component.
     */
    @NotNull
    public static Component getComponent(String text, String... placeholders) {
        /*
        Create a TagResolver builder to carry out replacements.
         */
        TagResolver.Builder tagResolver = TagResolver.builder();
        /*
        Loop through the placeholders pair by pair.
         */
        for(int i = 0; i < placeholders.length; i += 2) {
            /*
            Add each replacement pair to the tag resolver.
             */
            tagResolver.tag(placeholders[i], Tag.inserting(mm.deserialize(placeholders[i + 1])));
        }
        /*
        Deserialize the text with the TagResolver.
         */
        return mm.deserialize(text, tagResolver.build());
    }

    /**
     * The same as getComponent.
     * @param text The raw text.
     * @param placeholders The placeholder (ordered in pairs). E.g. value_to_find, replace_with_this, etc.
     * @return The resulting component.
     */
    public static Component parse(String text, final String... placeholders) {
        return getComponent(text, placeholders);
    }

    /**
     * Load the lists from the data file.
     */
    public static void loadObjects() throws IOException, ClassNotFoundException {
        /*
        Create an object input stream.
         */
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(
                        new File(pl.getDataFolder(),PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.FILE))));
        /*
        Create a list to store the objects in once read.
         */
        List<List<UUID>> objs = new ArrayList<>();
        /*
        Try to read the objects from the file.
         */
        try {
            /*
            Read the first object.
             */
            Object o = ois.readObject();
            /*
            If the first object wasn't null, continue to the next object until the end of the file is reached..
             */
            while (o != null) {
                objs.add(new ArrayList<>((List<UUID>) o));
                o = ois.readObject();
            }
            /*
            An EOFException would only be thrown if the end of the file is reached.
             */
        } catch(EOFException e){/*ignore*/}
        /*
        Close the object input stream.
         */
        ois.close();
        /*
        If only 2 objects were read, add an empty list (which is the list of locked players).
         */
        if(objs.size()==2) objs.add(new ArrayList<>());
        /*
        Pass the list to the wider scope.
         */
        loaded = objs;
    }

    /**
     * Save the lists to the data file.
     * @param file The name of the file.
     * @param objects The objects to save.
     */
    public static void saveObjects(final String file, final Object... objects) {
        try {
            /*
            Create an object output stream to write to the file.
             */
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pl.getDataFolder(),file)));
            /*
            For each object, write it to the file.
             */
            Arrays.stream(objects).forEach(o -> {try {
                    oos.writeObject(o);
                } catch (IOException e) {e.printStackTrace();}
            });
            /*
            Write null at the end to indicate the end of the file.
             */
            oos.writeObject(null);
            /*
            Flush and close the object output stream.
             */
            oos.flush();
            oos.close();
        } catch (IOException e) {e.printStackTrace();}
    }

    /**
     * Get the list of loaded information.
     * @return The list of lists.
     */
    public static List<List<UUID>> loaded() {
        return loaded;
    }

    /**
     * Toggle a pvp status.
     * @param p The player to toggle the pvp status of.
     * @return true if successful. False if unsuccessful.
     */
    public static boolean togglePersonal(final Player p) {
        /*
        If locking is enabled,
         */
        if(pl.conf().get().getProperty(GeneralConfig.IS_STATUS_LOCKING)) {
            /*
            If the player's lock is enabled,
             */
            if (pl.pvp().isLocked(p.getUniqueId())) {
                /*
                Cancel and notify the player.
                 */
                Utils.sendText(p,Utils.parse("Oops! Your PVP status is locked."));
                return false;
            }
        }
        /*
        If the player's cooldown is still in progress,
         */
        if(pl.pvp().coolingDown(p)) {
            /*
            Send them the remaining time in seconds until they can try toggling their pvp status again then cancel.
             */
            int remaining = pl.pvp().getRemainingSeconds(p.getUniqueId());
            Utils.sendText(p, Utils.parse("<red>You can do that again in <yellow><bold><seconds></bold></yellow>.","seconds",remaining+(remaining>1?" seconds":" second")));
            return false;
        }
        /*
        Activate the player's cooldown.
         */
        pl.pvp().coolDown(p);
        /*
        Toggle the player's PVP status and send feedback.
         */
        Utils.sendText(p, pl.pvp().toggle(p.getUniqueId()) ? mm.deserialize("<aqua>PVP enabled.") :
                mm.deserialize("<green>PVP disabled."));
        return true;
    }

    /**
     * Methods for managing tamed animals.
     */
    public static class Tameables {
        /**
         * Check whether tameables are involved. If they are, look for any owners and compare PVP statuses.
         * @param attacker The attacking entity.
         * @param defender The defending entity.
         * @return True if the event should be cancelled. False otherwise.
         */
        public static boolean shouldTameablesCancel(final Entity attacker, final Entity defender) {
            /*
            If tamed animal protection is disabled, skip.
             */
            if (!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.PREVENT_TAMEDDAMAGE)) return false;
            /*
            If no possibly tamed animals are involved, skip.
             */
            if (!(attacker instanceof Tameable || defender instanceof Tameable)) return false;
            /*
             * The current possibly tamed animal.
             */
            Tameable animal;
            /*
            If the attacker is possibly tamed,
             */
            if (attacker instanceof Tameable) {
                /*
                Save it as a Tameable.
                 */
                animal = (Tameable) attacker;
                /*
                If it has no owner or the owner isn't a player, don't cancel.
                 */
                if (animal.getOwner() == null
                        || !(animal.getOwner() instanceof Player)) return false;
                /*
                If the owner has PVP disabled, cancel the event.
                 */
                if (PPVPPlugin.inst().pvp().pvpNegative(animal.getOwner().getUniqueId())) return true;
                /*
                If the defending entity is a player,
                 */
                if (defender instanceof Player) {
                    /*
                    If the defending player has PVP disabled, cancel.
                     */
                    if (PPVPPlugin.inst().pvp().pvpNegative(defender.getUniqueId())) return true;
                    return false;
                }
            }
            /*
            If the defending entity is possibly tamed,
             */
            if (defender instanceof Tameable) {
                /*
                Save the defender as a Tameable.
                 */
                animal = (Tameable) defender;
                /*
                If the tameable doesn't have an owner, don't cancel.
                 */
                if (animal.getOwner() == null) return false;
                /*
                If the owner is not a player, don't cancel.
                 */
                if (!(animal.getOwner() instanceof Player)) return false;
                /*
                If the attacker is a player,
                 */
                if (attacker instanceof Player) {
                    /*
                    If the defending animal is the attacker's pet, don't cancel.
                     */
                    if (animal.getOwner().equals(attacker)) return false;
                }
                return checkOwner((Tameable) defender);
            }
            /*
            Unreachable.
             */
            return false;
        }

        /**
         * Check the owner of the defender.
         * @param animal The tameable animal.
         * @return True if the event should be cancelled; false if not.
         */
        private static boolean checkOwner(Tameable animal) {
            /*
            If the animal doesn't have a player owner, don't cancel.
             */
            if (animal.getOwner() == null || !(animal.getOwner() instanceof Player)) return false;
            /*
            If the animal's owner has PVP disabled, cancel.
             */
            if (PPVPPlugin.inst().pvp().pvpNegative(animal.getOwner().getUniqueId())) return true;
            /*
            If the animal's owner has PVP enabled, don't cancel.
             */
            return false;
        }
    }
}
