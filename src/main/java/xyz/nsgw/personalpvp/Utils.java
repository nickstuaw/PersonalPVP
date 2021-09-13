package xyz.nsgw.personalpvp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.nsgw.personalpvp.config.GeneralConfig;
import xyz.nsgw.personalpvp.managers.PVPManager;

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
        if (text) {
            sender.sendMessage(component);
        }
        if (actionbar) {
            sender.sendActionBar(component);
        }
    }
    public static void sendText(final Player player, final Component component) {
        player.sendMessage(component);
    }

    public static void send(final Component component) {
        Bukkit.getConsoleSender().sendMessage(component);
    }

    public static Component parse(String text, final String... placeholders) {
        return MiniMessage.get().parse(text, placeholders);
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
            if (PVPManager.isLocked(p.getUniqueId())) {
                Utils.sendText(p,MiniMessage.get().parse("Oops! Your PVP status has been locked."));
                return false;
            }
        }
        if(PVPManager.coolingDown(p)) {
            int remaining = PVPManager.getRemainingSeconds(p.getUniqueId());
            Utils.sendText(p, MiniMessage.get().parse("<red>You can do that again in <yellow><bold><seconds></bold></yellow>.","<seconds>",remaining+(remaining>1?" seconds":" second")));
            return false;
        }
        PVPManager.coolDown(p);
        Utils.sendText(p, PVPManager.toggle(p.getUniqueId()) ? MiniMessage.get().parse("<aqua>PVP enabled.") : MiniMessage.get().parse("<green>PVP disabled."));
        return true;
    }
}
