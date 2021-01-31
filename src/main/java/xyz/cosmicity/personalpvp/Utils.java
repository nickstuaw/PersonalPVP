package xyz.cosmicity.personalpvp;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.cosmicity.personalpvp.managers.PVPManager;
import xyz.cosmicity.personalpvp.storage.Message;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Utils {

    private static PPVPPlugin pl;

    private static List<List<UUID>> loaded = new ArrayList<>(Arrays.asList(new ArrayList<>(),new ArrayList<>(),new ArrayList<>()));

    public static void setPlugin(final PPVPPlugin plugin) {
        pl = plugin;
    }

    public static void send(final CommandSender sender, final Component component, final boolean text, final boolean actionbar) {
        if(sender instanceof Player) {
            Audience a = BukkitAudiences.create(pl).player((Player)sender);
            if (text) {
                a.sendMessage(component);
            }
            if (actionbar) {
                a.sendActionBar(component);
            }
        } else {
            if (text) {
                BukkitAudiences.create(pl).sender(sender).sendMessage(component);
            }
            if (actionbar) {
                BukkitAudiences.create(pl).sender(sender).sendActionBar(component);
            }
        }
    }
    public static void sendText(final Player player, final Component component) {
        BukkitAudiences.create(pl).player(player).sendMessage(component);
    }
    public static void send(final Message message, final Player player) {
        Audience audience = BukkitAudiences.create(pl).player(player);
        if(message.action) audience.sendActionBar(message.get());
        if(message.text) audience.sendMessage(message.get());
    }

    public static void send(final Component component) {
        BukkitAudiences.create(pl).console().sendMessage(component);
    }
    public static void sendConsole(final Message message) {
        BukkitAudiences.create(pl).console().sendMessage(message.get());
    }

    public static Component parse(String text, final String... placeholders) {
        return MiniMessage.get().parse(text, placeholders);
    }

    public static void loadObjects() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(new File(pl.getDataFolder(),Config.data_filename())));
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
        if(Config.use_locking()) {
            if (PVPManager.isLocked(p.getUniqueId())) {
                Utils.send(Config.locked_message(), p);
                return false;
            }
        }
        if(PVPManager.coolingDown(p)) {
            int remaining = PVPManager.getRemainingSeconds(p.getUniqueId());
            Utils.send( Config.pvp_cooldown_prompt().parse("seconds",remaining+(remaining>1?" seconds":" second")),p);
            return false;
        }
        PVPManager.coolDown(p);
        Utils.send(PVPManager.toggle(p.getUniqueId()) ? Config.pvp_enabled_message() : Config.pvp_disabled_message(), p);
        return true;
    }

    public static Argument offlinePlayerArgument(final String name) {
        return new CustomArgument<>(name, (input)->{
            OfflinePlayer p = Bukkit.getServer().getOfflinePlayerIfCached(input);
            if(p == null)
                throw new CustomArgument.CustomArgumentException(input+" has never joined the server.");
            else return p;
        });
    }
}
