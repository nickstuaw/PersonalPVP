package xyz.nsgw.personalpvp.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.nsgw.personalpvp.PPVPPlugin;
import xyz.nsgw.personalpvp.Utils;
import xyz.nsgw.personalpvp.config.GeneralConfig;
import xyz.nsgw.personalpvp.managers.PVPManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandHandler {

    private static final List<String> permissions = new ArrayList<>();

    private final PaperCommandManager manager;

    public CommandHandler(PPVPPlugin plugin) {
        manager = new PaperCommandManager(plugin);

        manager.enableUnstableAPI("brigadier");
        manager.enableUnstableAPI("help");

        manager.registerCommand(new PVPCommand());
    }

    public void onDisable() {
        manager.unregisterCommands();
    }

    public static List<String> permissions() {return permissions;}

}
@CommandAlias("pvp")
class PVPCommand extends BaseCommand {

    @Default
    public void onPvp(Player p) {
        if(Utils.togglePersonal(p)) notifyConsole(p.getName(), PVPManager.isPvpEnabled(p.getUniqueId()));
    }

    private void notifyConsole(final String tName, final boolean setTo) {
        if(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_LOG_EVENTS_TO_CONSOLE)) Utils.send(Utils.parse(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE_FORMAT),"name",tName,"pvpstatus", setTo?PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE__PVP_ENABLED_PFX):PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE__PVP_DISABLED_PFX)));
    }

    @Subcommand("control")
    public class onPvpCtrlr extends BaseCommand {
        private final String title = "<gradient:green:aqua>- - - -</gradient> <white><bold>PVP Control</bold> <gradient:aqua:green>- - - -</gradient>";

        @Default
        public void onControl(final CommandSender s) {
            Utils.send(s, Utils.parse(this.title+"\n" + PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPCTRL_PERSONAL_LINES) +(s.hasPermission("personalpvp.control.admin")?"\n<green><underlined>Admin</underlined>\n"+PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPCTRL_LINES)+"\n":"\n")+this.title), true, false);
        }

        @Subcommand("resetglobal")
        public void onGlobalReset(final CommandSender s) {
            if (!s.hasPermission("personalpvp.control.admin")) {
                throw new InvalidCommandArgument("Operation not found.");
            }
            PVPManager.players().forEach(PVPManager::remove);
            Utils.send(s, Utils.parse("<yellow>You <hover:show_text:'" + (PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) ? "<aqua>ENABLED" : "<green>DISABLED") + "'>reset</hover> PVP <hover:show_text:'Including offline players.'>for every player</hover>."), true, false);
        }

        @Subcommand("toggleme")
        public void onToggleMe(final Player p) {
            if (Utils.togglePersonal(p)) notifyConsole(p.getName(),PVPManager.isPvpEnabled(p.getUniqueId()));
        }

        @Subcommand("mystatus")
        public void onMyStatus(final Player p) {
            Utils.send(p, Utils.parse("<yellow>You have PVP " + (PVPManager.pvpPositive(p.getUniqueId()) ? "<aqua>ENABLED<yellow>." : "<green>DISABLED<yellow>.")), true, false);
        }

        private void notifyConsole(final String tName, final boolean setTo) {
            if(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_LOG_EVENTS_TO_CONSOLE)) Utils.send(Utils.parse(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE_FORMAT),"name",tName,"pvpstatus", (setTo?PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE__PVP_ENABLED_PFX):PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE__PVP_DISABLED_PFX))));
        }
    }

    @Subcommand("list")
    public void onList(final CommandSender s) {
        List<String> list = PVPManager.players().stream()
            .filter(PVPManager::pvpPositive)
            .map(Bukkit::getOfflinePlayer)
            .map(OfflinePlayer::getName).collect(Collectors.toList());
        Utils.send(s, Utils.parse((!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS)?"<aqua>PVP is enabled for: </aqua>":"<green>PVP is disabled for: </green>")+String.join(", ",list)),true,false);
    }

    @co.aikar.commands.annotation.HelpCommand
    public void onHelp(CommandSender s, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("other")
    @CommandPermission("personalpvp.control.other")
    public class onOther extends BaseCommand {

        @Subcommand("status")
        public void onStatus(final CommandSender s, final Player target) {
            Utils.send(s, Utils.parse("<yellow>"+target.getDisplayName()+" has PVP " + (PVPManager.pvpPositive(target.getUniqueId()) ? "<aqua>ENABLED." : "<green>DISABLED.")), true, false);
        }
        @Subcommand("toggle")
        public void onToggle(final CommandSender s, final Player target) {
            PVPManager.toggle(target.getUniqueId());
            String msg = "<gray>toggled</gray><yellow> PVP for "+target.getName()+".</yellow>";
            Utils.send(s, Utils.parse("<yellow>You </yellow>"+msg), true, false);
            notifyConsole("<yellow>"+s.getName()+"</yellow> "+msg);
        }
        @Subcommand("reset")
        public void onReset(final CommandSender s, final Player target) {
            PVPManager.remove(target.getUniqueId());
            String msg = (PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS)?"<aqua>enabled</aqua>":"<green>disabled</green>")+"<yellow> PVP for "+target.getName()+".</yellow>";
            Utils.send(s, Utils.parse("<yellow>You </yellow>"+msg), true, false);
            notifyConsole("<yellow>"+s.getName()+"</yellow> "+msg);
        }
        @Subcommand("enable")
        public void onEnable(final CommandSender s, final Player target) {
            if(PVPManager.isPvpDisabled(target.getUniqueId())) {
                PVPManager.toggle(target.getUniqueId());
                String msg = "<aqua>enabled</aqua><yellow> PVP for "+target.getName()+".</yellow>";
                Utils.send(s, Utils.parse("<yellow>You </yellow>"+msg), true, false);
                notifyConsole("<yellow>"+s.getName()+"</yellow> "+msg);
            }
        }
        @Subcommand("disable")
        public void onDisable(final CommandSender s, final Player target) {
            if(PVPManager.isPvpEnabled(target.getUniqueId())) {
                PVPManager.toggle(target.getUniqueId());
                String msg = "<green>disabled</green><yellow> PVP for "+target.getName()+".</yellow>";
                Utils.send(s, Utils.parse("<yellow>You </yellow>"+msg), true, false);
                notifyConsole("<yellow>"+s.getName()+"</yellow> "+msg);
            }
        }

        private void notifyConsole(final String msg) {
            if(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_LOG_EVENTS_TO_CONSOLE)) Utils.send(Utils.parse(msg));
        }
    }

    @Subcommand("reset")
    @CommandCompletion("players")
    public void onReset(final Player p) {
        PVPManager.remove(p.getUniqueId());
        Utils.send(p, Utils.parse(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS)?"<aqua>":"<green>"+"You reset your PVP status."), true, false);
    }
    @Subcommand("lock")
    public class onLock extends BaseCommand {

        @Subcommand("toggle")
        @CommandCompletion("@players")
        public void onToggle(final CommandSender s, final Player target) {UUID u = target.getUniqueId();
            String name = target.getName();
            String locked = PVPManager.toggleLocked(u)?"locked":"unlocked";
            Utils.send(s,
                    Utils.parse(
                            "<hover:show_text:'<yellow>PVP "+(PVPManager.isPvpEnabled(u)?"<aqua>Enabled</aqua>":"<green>Disabled</green>")+" for "+name+"</yellow>'>"+
                                    "<blue>PVP "+locked+" for "+target.getName()+
                                    ".</blue>"), true, false);
        }

        @Subcommand("toggleoffline")
        @CommandCompletion("@nothing")
        public void onOfflineToggle(final CommandSender s, final OfflinePlayer target) {
            UUID u = target.getUniqueId();
            String name = target.getName();
            String locked = PVPManager.toggleLocked(u)?"locked":"unlocked";
            Utils.send(s,
                    Utils.parse(
                            "<hover:show_text:'<yellow>PVP "+(PVPManager.isPvpEnabled(u)?"<aqua>Enabled</aqua>":"<green>Disabled</green>")+" for "+name+"</yellow>'>"+
                                    "<blue>PVP "+locked+" for "+target.getName()+
                                    ".</blue>"), true, false);
        }

        @Subcommand("status")
        @CommandCompletion("@players")
        public void onStatus(final CommandSender s, final Player target) {
            UUID u = target.getUniqueId();
            String name = target.getName();
            String locked = PVPManager.isLocked(u)?"locked":"unlocked";
            Utils.send(s,
                    Utils.parse("<hover:show_text:'<yellow>PVP "+(PVPManager.isPvpEnabled(u)?"<aqua>Enabled</aqua>":"<green>Disabled</green>")+" for "+name+"</yellow>'><blue>"+name+" has PVP "+locked+".</blue></hover>"), true, false);
        }
    }
    @Subcommand("reload")
    public void onReload(final CommandSender s) {
        PPVPPlugin.inst().reloadConfigs();
        Utils.send(s, Utils.parse("<green>PVP Config Reloaded."), true, false);
    }
    @Subcommand("togglebar")
    public void onToggleBar(final Player p) {
        Utils.send(p, Utils.parse(PPVPPlugin.inst().toggleHiddenActionbar(p) ? "<green>Action bar enabled.":"<green>Action bar disabled."), true, false);
    }
}