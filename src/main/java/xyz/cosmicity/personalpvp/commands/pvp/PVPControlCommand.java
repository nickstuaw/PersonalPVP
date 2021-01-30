package xyz.cosmicity.personalpvp.commands.pvp;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.TextArgument;
import org.bukkit.entity.Player;
import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;
import xyz.cosmicity.personalpvp.commands.Command;
import xyz.cosmicity.personalpvp.commands.CommandDetails;
import xyz.cosmicity.personalpvp.managers.PVPManager;

public class PVPControlCommand extends Command {
    private final String title = "<gradient:green:aqua>- - - -</gradient> <white><bold>PVP Control</bold> <gradient:aqua:green>- - - -</gradient>";
    public void register(final CommandDetails details) {
        PPVPPlugin pl = PPVPPlugin.inst();
        details.registerCommand((s, args) -> {
            Utils.send(s, Utils.parse(this.title+"\n" + pl.pvpcontrol_personal_lines() +(s.hasPermission(details.permission()+".admin")?"\n<green><underlined>Admin</underlined>\n"+pl.pvpcontrol_lines()+"\n":"\n")+this.title), true, false);
        });
        details.registerCommand((s, args) -> {
            switch ((String) args[0]) {
                case "resetglobal":
                    if(!s.hasPermission(details.permission()+".admin")) {
                        CommandAPI.fail("Unknown operation.");
                        break;
                    }
                    PVPManager.players().forEach(PVPManager::remove);
                    Utils.send(s, Utils.parse("<yellow>You <hover:show_text:'" + (pl.default_pvp_status() ? "<aqua>ENABLED" : "<green>DISABLED") + "'>reset</hover> PVP <hover:show_text:'Including offline players.'>for every player</hover>."), true, false);
                    return;
                case "toggleme":
                    if(!(s instanceof Player)) {
                        CommandAPI.fail("/pvp mystatus is player only.");
                        return;
                    }
                    if(Utils.togglePersonal((Player)s, pl)) notifyConsole((Player)s);
                    return;
                case "mystatus":
                    if(!(s instanceof Player)) {
                        CommandAPI.fail("/pvp mystatus is player only.");
                        return;
                    }
                    Utils.send(s, Utils.parse("<yellow>You have PVP " + (PVPManager.pvpPositive(((Player)s).getUniqueId()) ? "<aqua>ENABLED<yellow>." : "<green>DISABLED<yellow>.")), true, false);
                    return;
                default:
                    CommandAPI.fail("Unknown operation.");
                    break;
            }
        }, "other", new TextArgument("operation").overrideSuggestions(
                "resetglobal","toggleme","mystatus"
        ));
    }
    private void notifyConsole(final Player p) {
        if(PPVPPlugin.inst().pvp_toggle_log_to_console()) Utils.send(Utils.parse(PPVPPlugin.inst().console_format(),"name",p.getName(),"pvpstatus", PPVPPlugin.inst().console_pvpstatuses()[(PVPManager.isPvpEnabled(p.getUniqueId())?0:1)]));
    }

}
