package xyz.cosmicity.personalpvp;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.cosmicity.personalpvp.managers.PVPManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Utils {

    private static PPVPPlugin pl;

    private static List<List<UUID>> loaded = new ArrayList<>(Arrays.asList(new ArrayList<>(),new ArrayList<>()));

    public static void setPlugin(final PPVPPlugin plugin) {
        pl = plugin;
    }

    public static void send(final CommandSender sender, final Component component, final boolean text, final boolean actionbar) {
        if(sender instanceof Player) {
            if (actionbar) {
                BukkitAudiences.create(pl).player((Player) sender).sendActionBar(component);
            }
            if (text) {
                BukkitAudiences.create(pl).player((Player) sender).sendMessage(component);
            } return;
        }
        if (actionbar) {
            BukkitAudiences.create(pl).sender(sender).sendActionBar(component);
        }
        BukkitAudiences.create(pl).sender(sender).sendMessage(component);
    }

    public static void send(final Component component) {
        BukkitAudiences.create(pl).console().sendMessage(component);
    }

    public static Component parse(String text, final String... placeholders) {
        return MiniMessage.get().parse(text, placeholders);
    }

    public static void loadObjects() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(new File(pl.getDataFolder(),pl.data_filename())));
        List<List<UUID>> objs = new ArrayList<>();
        try {
            Object o = ois.readObject();
            while (o != null) {
                objs.add(new ArrayList<>((List<UUID>) o));
                o = ois.readObject();
            }
        } catch(EOFException e){/*ignore*/}
        ois.close();
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

    public static boolean togglePersonal(final Player p, final PPVPPlugin pl) {
        if(PVPManager.coolingDown(p)) {
            int remaining = PVPManager.getRemainingSeconds(p.getUniqueId());
            Utils.send(p, Utils.parse(PPVPPlugin.inst().pvp_cooldown_prompt(),"seconds",remaining+(remaining>1?" seconds":" second")),true,false);
            return false;
        }
        else {
            PVPManager.coolDown(p);
            Utils.send(p, Utils.parse(
                    pl.getConfig().getString("command-settings.pvp-toggle." + (PVPManager.toggle(p.getUniqueId()) ? "enabled" : "disabled"))
            ), true, false);
            return true;
        }
    }
}
