/*
 * Copyright (c) 2022.
 */

package com.nsgwick.personalpvp.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.nsgwick.personalpvp.PPVPPlugin;
import com.nsgwick.personalpvp.Utils;
import com.nsgwick.personalpvp.config.GeneralConfig;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandHandler {

    private final PaperCommandManager manager;

    public CommandHandler(PPVPPlugin plugin) {

        /*
        Initialise the command manager.
         */
        manager = new PaperCommandManager(plugin);

        /*
        Enable some extra functionality.
         */
        manager.enableUnstableAPI("brigadier");
        manager.enableUnstableAPI("help");

        /*
        Register the /pvp command.
         */
        manager.registerCommand(new PVPCommand());
    }

    public void onDisable() {
        /*
        Unregister any registered commands.
         */
        manager.unregisterCommands();
    }

}
/*
The class for the /pvp command.
 */
@CommandAlias("pvp")
class PVPCommand extends BaseCommand {
    /*
    @Default = this runs when no arguments are included.
     */
    @Default
    /*
    Requires the command sender to have this permission.
     */
    @CommandPermission("personalpvp.togglepvp")
    public void onPvp(Player p) {
        /*
        If the player is not an operator,
         */
        if(!p.isOp()) {
            /*
            If the player has a permission that affects their pvp status,
             */
            if (p.hasPermission("personalpvp.always.on") || p.hasPermission("personalpvp.always.off")) {
                /*
                Tell the player that they can't toggle their own status.
                 */
                Utils.sendText(p, Utils.parse("<red>Sorry, you can't do that.</red>"));
                return;
            }
        }
        /*
        Toggle the status. If the status has been successfully toggled, attempt to notify the console.
         */
        if(Utils.togglePersonal(p)) notifyConsole( PPVPPlugin.inst().pvp().pvpPositive(p.getUniqueId()), p);
    }

    private void notifyConsole(final boolean setTo, Player p) {
        /*
        If console event notifications are enabled in the config,
         */
        if(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_LOG_EVENTS_TO_CONSOLE))
            /*
            Put together and send a notification to the console.
             */
            Utils.send(Utils.parse(
                    p,PPVPPlugin.inst().conf().get()
                    .getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE_FORMAT),
                    "name",p.getName(),
                    "pvpstatus", setTo ?
                            PPVPPlugin.inst().conf().get()
                                    .getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE__PVP_ENABLED_PFX) :
                            PPVPPlugin.inst().conf().get()
                                    .getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE__PVP_DISABLED_PFX)));
    }

    /*
    /pvp control
     */
    @Subcommand("control")
    @CommandPermission("personalpvp.pvpcontrol")
    public class onPvpCtrlr extends BaseCommand {
        /*
        The chat header.
         */
        private final String title = "<gradient:green:aqua>- - - -</gradient>" +
                " <white><bold>PVP Control</bold> <gradient:aqua:green>- - - -</gradient>";

        /*
        Default subroutine (for /pvp control)
         */
        @Default
        public void onControl(final CommandSender s) {
            /*
            If a player executed the command,
             */
            if(s instanceof Player) {
                /*
                Send the PVP-control message. Lines are stored in the config.
                 */
                Utils.send(s, Utils.parse((Player) s, this.title + "\n"
                        + PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPCTRL_PERSONAL_LINES)
                        + (s.hasPermission("personalpvp.pvpcontrol.admin") ?
                        "\n<green><underlined>Admin</underlined>\n"
                                + PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPCTRL_LINES)
                                + "\n" : "\n") + this.title), true, false);
            }
        }

        /*
        /pvp control resetglobal
         */
        @Subcommand("resetglobal")
        @CommandPermission("personalpvp.pvpcontrol.resetglobal")
        public void onGlobalReset(final CommandSender s) {
            /*
            Save a list of the uuids of all altered players.
             */
            List<UUID> uuids = PPVPPlugin.inst().pvp().alteredPlayers();
            /*
            Save the size.
             */
            int size = uuids.size();
            /*
            Loop through each uuid.
             */
            for(int i = 0; i < size; i++) {
                /*
                Reset each player by removing them from the list of altered players.
                 */
                PPVPPlugin.inst().pvp().remove(uuids.get(i));
            }
            /*
            Send command feedback to the player.
             */
            Utils.send(s, Utils.parse(
                    "<yellow>You <hover:show_text:'"
                    + (PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) ?
                            "<aqua>ENABLED" : "<green>DISABLED")
                            + "'>reset</hover> PVP <hover:show_text:'Including offline players.'>" +
                            "for every player</hover>."), true, false);
        }

        /*
        /pvp control toggleme
         */
        @Subcommand("toggleme")
        @Description("Toggle your own PVP status.")
        /*
        This can only be executed by a player.
         */
        public void onToggleMe(final Player p) {
            /*
            If the player is not an operator,
             */
            if(!p.isOp()) {
                /*
                If the player has a permission that declares status permanence,
                 */
                if (p.hasPermission("personalpvp.always.on") || p.hasPermission("personalpvp.always.off")) {
                    /*
                    Send command feedback and cancel the operation.
                     */
                    Utils.sendText(p, Utils.parse("<red>Sorry, you can't do that.</red>"));
                    return;
                }
            }
            /*
            Try to toggle the player's status. If the toggle was successful, attempt to notify the console.
             */
            if (Utils.togglePersonal(p)) notifyConsole(p.getName(),PPVPPlugin.inst().pvp()
                    .pvpPositive(p.getUniqueId()));
        }

        /*
        /pvp control mystatus
         */
        @Subcommand("mystatus")
        @Description("View your own PVP status.")
        /*
        This can only be executed by a player.
         */
        public void onMyStatus(final Player p) {
            /*
            Display the player's current PVP status to them in chat.
             */
            Utils.send(p, Utils.parse("<yellow>You have PVP " +
                    (PPVPPlugin.inst().pvp().pvpPositive(p.getUniqueId()) ?
                            "<aqua>ENABLED<yellow>." : "<green>DISABLED<yellow>.")),
                    true, false);
        }

        /*
        The subroutine used to attempt to send a console notification when a status has been toggled.
         */
        private void notifyConsole(final String tName, final boolean setTo) {
            /*
            If the toggle events should be logged.
             */
            if(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_LOG_EVENTS_TO_CONSOLE))
                /*
                Send the notification to the console.
                 */
                Utils.send(Utils.parse(
                        PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE_FORMAT),
                        "name",tName,
                        "pvpstatus", (setTo ?
                                PPVPPlugin.inst().conf().get()
                                        .getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE__PVP_ENABLED_PFX) :
                                PPVPPlugin.inst().conf().get()
                                        .getProperty(GeneralConfig.CMD_PVPTOGGLELOG_CONSOLE__PVP_DISABLED_PFX))));
        }
    }

    /*
    /pvp list
     */
    @Subcommand("list")
    @CommandPermission("personalpvp.listpvp")
    @Description("List the players who have an altered PVP status.")
    public void onList(final CommandSender s) {
        /*
        Save a list of the usernames of any (including offline) players.
         */
        List<String> list = PPVPPlugin.inst().pvp().alteredPlayers().stream()
            .filter(PPVPPlugin.inst().pvp()::pvpPositive)
            .map(Bukkit::getOfflinePlayer)
            .map(OfflinePlayer::getName).collect(Collectors.toList());
        /*
        Send command feedback showing a list of those with an altered PVP status.
         */
        Utils.send(s, Utils.parse(
                (!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) ?
                "<aqua>PVP is enabled for: </aqua>":"<green>PVP is disabled for: </green>")
                        + String.join(", ",list)),true,false);
    }

    /*
    The help subroutine.
     */
    @co.aikar.commands.annotation.HelpCommand
    public void onHelp(CommandSender s, CommandHelp help) {
        help.showHelp();
    }

    /*
    /pvp other
     */
    @Subcommand("other")
    @CommandPermission("personalpvp.pvpcontrol.other")
    public class onOther extends BaseCommand {

        /*
        /pvp other status
         */
        @Subcommand("status")
        @CommandPermission("personalpvp.pvpcontrol.other.status")
        @Description("View the PVP status of another player.")
        public void onStatus(final CommandSender s, final OnlinePlayer t) {
            /*
            Save te targeted player.
             */
            Player target = t.getPlayer();
            /*
            Send the target's PVP status as command feedback.
             */
            Utils.send(s, Utils.parse("<yellow>"+target.getName()+" has PVP "
                    + (PPVPPlugin.inst().pvp().pvpPositive(target.getUniqueId()) ?
                    "<aqua>ENABLED." : "<green>DISABLED.")), true, false);
        }
        /*
        /pvp other toggle
         */
        @Subcommand("toggle")
        @CommandPermission("personalpvp.pvpcontrol.other.toggle")
        @CommandCompletion("@players")
        @Description("Toggle a player's PVP status.")
        public void onToggle(final CommandSender s, final OnlinePlayer t) {
            /*
            Save the target player.
             */
            Player target = t.getPlayer();
            /*
            If the target player is not an operator,
             */
            if(!target.isOp()) {
                /*
                If the target player has a permission of status permanence,
                 */
                if (target.hasPermission("personalpvp.always.on") ||
                        target.hasPermission("personalpvp.always.off")) {
                    /*
                    Cancel the command with relevant feedback.
                     */
                    Utils.sendText(target,
                            Utils.parse("<red>Sorry, you can't do that. Check their permissions.</red>"));
                    return;
                }
            }
            /*
            Toggle the target's PVP status (skips lock checking).
             */
            PPVPPlugin.inst().pvp().toggle(target.getUniqueId());
            /*
            Build the message.
             */
            String msg = "<gray>toggled</gray><yellow> PVP for "+target.getName()+".</yellow>";
            /*
            Send the command feedback message.
             */
            Utils.send(s, Utils.parse("<yellow>You </yellow>"+msg), true, false);
            /*
            Attempt to notify the console.
             */
            notifyConsole("<yellow>"+s.getName()+"</yellow> "+msg);
        }
        /*
        /pvp other reset
         */
        @Subcommand("reset")
        @CommandPermission("personalpvp.pvpcontrol.other.reset")
        @CommandCompletion("@players")
        @Description("Reset a player's pvp status.")
        public void onReset(final CommandSender s, final OnlinePlayer t) {
            /*
            Save the target player.
             */
            Player target = t.getPlayer();
            /*
            If the target player isn't an operator,
             */
            if(!target.isOp()) {
                /*
                If the target player has a permission of status permanence,
                 */
                if (target.hasPermission("personalpvp.always.on") ||
                        target.hasPermission("personalpvp.always.off")) {
                    /*
                    Cancel the reset with feedback.
                     */
                    Utils.sendText(target,
                            Utils.parse("<red>Sorry, you can't do that. Check their permissions.</red>"));
                    return;
                }
            }
            /*
            Forcefully reset the target player's status.
             */
            PPVPPlugin.inst().pvp().remove(target.getUniqueId());
            /*
            Send the command feedback.
             */
            String msg = (PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) ?
                    "<aqua>enabled</aqua>":"<green>disabled</green>")
                    +"<yellow> PVP for "+target.getName()+".</yellow>";
            Utils.send(s, Utils.parse("<yellow>You </yellow>"+msg), true, false);
            /*
            Attempt to notify the console.
             */
            notifyConsole("<yellow>"+s.getName()+"</yellow> "+msg);
        }
        /*
        /pvp other enable
         */
        @Subcommand("enable")
        @CommandPermission("personalpvp.pvpcontrol.other.enable")
        @CommandCompletion("@players")
        @Description("Enable a player's pvp status.")
        public void onEnable(final CommandSender s, final OnlinePlayer t) {
            /*
            Save the target player.
             */
            Player target = t.getPlayer();
            /*
            If the target player isn't an operator,
             */
            if(!target.isOp()) {
                /*
                If the target player has a permission of status permanence,
                 */
                if (target.hasPermission("personalpvp.always.on") ||
                        target.hasPermission("personalpvp.always.off")) {
                    /*
                    Cancel the operation with feedback.
                     */
                    Utils.sendText(target,
                            Utils.parse("<red>Sorry, you can't do that. Check their permissions.</red>"));
                    return;
                }
            }
            /*
            If the player has pvp disabled,
             */
            if(PPVPPlugin.inst().pvp().pvpNegative(target.getUniqueId())) {
                /*
                Toggle it to enable it.
                 */
                PPVPPlugin.inst().pvp().toggle(target.getUniqueId());
                /*
                Send the command feedback.
                 */
                String msg = "<aqua>enabled</aqua><yellow> PVP for "+target.getName()+".</yellow>";
                Utils.send(s, Utils.parse("<yellow>You </yellow>"+msg), true, false);
                /*
                Attempt to notify the console.
                 */
                notifyConsole("<yellow>"+s.getName()+"</yellow> "+msg);
            }
        }
        /*
        /pvp other disable
         */
        @Subcommand("disable")
        @CommandPermission("personalpvp.pvpcontrol.other.disable")
        @CommandCompletion("@players")
        @Description("Disable a player's pvp status.")
        public void onDisable(final CommandSender s, final OnlinePlayer t) {
            /*
            Save the target player.
             */
            Player target = t.getPlayer();
            /*
            If the target player isn't an operator,
             */
            if(!target.isOp()) {
                /*
                If the target player has a permission of status permanence,
                 */
                if (target.hasPermission("personalpvp.always.on") ||
                        target.hasPermission("personalpvp.always.off")) {
                    /*
                    Cancel the operation with feedback.
                     */
                    Utils.sendText(target,
                            Utils.parse("<red>Sorry, you can't do that. Check their permissions.</red>"));
                    return;
                }
            }
            /*
            If the player has pvp enabled,
             */
            if(PPVPPlugin.inst().pvp().pvpPositive(target.getUniqueId())) {
                /*
                Toggle it to disable it.
                 */
                PPVPPlugin.inst().pvp().toggle(target.getUniqueId());
                /*
                Send the command feedback.
                 */
                String msg = "<green>disabled</green><yellow> PVP for "+target.getName()+".</yellow>";
                Utils.send(s, Utils.parse("<yellow>You </yellow>"+msg), true, false);
                /*
                Attempt to notify the console.
                 */
                notifyConsole("<yellow>"+s.getName()+"</yellow> "+msg);
            }
        }

        /*
        The method that attempts to notify the console of pvp status alterations.
         */
        private void notifyConsole(final String msg) {
            if(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.CMD_PVPTOGGLE_LOG_EVENTS_TO_CONSOLE))
                Utils.send(Utils.parse(msg));
        }
    }

    /*
    /pvp reset
     */
    @Subcommand("reset")
    @CommandPermission("personalpvp.resetplayer")
    @CommandCompletion("@players")
    @Description("Reset your own pvp status.")
    public void onReset(final Player p) {
        /*
        If the player isn't an operator,
         */
        if(!p.isOp()) {
            /*
            If the target player has a permission of status permanence,
             */
            if (p.hasPermission("personalpvp.always.on") || p.hasPermission("personalpvp.always.off")) {
                /*
                Cancel the operation with feedback.
                 */
                Utils.sendText(p, Utils.parse("<red>Sorry, you can't do that.</red>"));
                return;
            }
        }
        /*
        Reset the player's status.
         */
        PPVPPlugin.inst().pvp().remove(p.getUniqueId());
        /*
        Send command feedback.
         */
        Utils.send(p, Utils.parse(
                PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS) ?
                        "<aqua>":"<green>"+"You reset your PVP status."),
                true, false);
    }
    /*
    /pvp lock
     */
    @Subcommand("lock")
    @CommandPermission("personalpvp.lock")
    public class onLock extends BaseCommand {

        @Subcommand("toggle")
        @CommandPermission("personalpvp.lock.toggle")
        @CommandCompletion("@players")
        @Description("Toggle a player's pvp status lock.")
        public void onToggle(final CommandSender s, final OnlinePlayer t) {
            /*
            Save the target player.
             */
            Player target = t.getPlayer();
            /*
            If the target player has a permission of status permanence,
             */
            if(!target.isOp()) {
            /*
            If the target player has a permission of status permanence,
             */
                if (target.hasPermission("personalpvp.always.on") ||
                        target.hasPermission("personalpvp.always.off")) {
                    /*
                    Cancel the operation with feedback.
                     */
                    Utils.sendText(target,
                            Utils.parse("<red>Sorry, you can't do that. Check their permissions.</red>"));
                    return;
                }
            }
            /*
            Toggle the lock for the target.
             */
            toggleLock(s, target);
        }

        @Subcommand("toggleoffline")
        @CommandPermission("personalpvp.lock.toggleoffline")
        @CommandCompletion("@nothing")
        @Description("Toggle an offline player's pvp status lock.")
        public void onOfflineToggle(final CommandSender s, final OfflinePlayer target) {
            /*
            Toggle the target's pvp status lock.
             */
            toggleLock(s, target);
        }

        /*
        The method to toggle a player's lock.
         */
        private void toggleLock(CommandSender s, OfflinePlayer target) {
            /*
            Save the target's UUID.
             */
            UUID u = target.getUniqueId();
            /*
            Save the target's last known name.
             */
            String name = target.getName();
            /*
            Fetch the relevant message (locked/unlocked).
             */
            String locked = PPVPPlugin.inst().pvp().toggleLocked(u)?"locked":"unlocked";
            /*
            Send the command feedback.
             */
            Utils.send(s,
                    Utils.parse(
                            "<hover:show_text:'<yellow>PVP "+(PPVPPlugin.inst().pvp().pvpPositive(u) ?
                                    "<aqua>Enabled</aqua>":"<green>Disabled</green>")+" for "+name+"</yellow>'>"+
                                    "<blue>PVP "+locked+" for "+target.getName()+
                                    ".</blue>"), true, false);
        }

        /*
        /pvp lock status
         */
        @Subcommand("status")
        @CommandPermission("personalpvp.lock.status")
        @CommandCompletion("@players")
        @Description("View an online player's pvp lock status.")
        public void onStatus(final CommandSender s, final OnlinePlayer t) {
            /*
            Save the target player.
             */
            Player target = t.getPlayer();
            /*
            Save the target's uuid.
             */
            UUID u = target.getUniqueId();
            /*
            Save the target's name.
             */
            String name = target.getName();
            /*
            Fetch the relevant message (locked/unlocked).
             */
            String locked = PPVPPlugin.inst().pvp().isLocked(u)?"locked":"unlocked";
            /*
            Send the command feedback.
             */
            Utils.send(s,
                    Utils.parse("<hover:show_text:'<yellow>PVP "+(PPVPPlugin.inst().pvp().pvpPositive(u) ?
                            "<aqua>Enabled</aqua>":"<green>Disabled</green>") + " for "
                            + name + "</yellow>'><blue>" + name + " has PVP " + locked
                            + ".</blue></hover>"), true, false);
        }
    }
    /*
    /pvp reload
     */
    @Subcommand("reload")
    @CommandPermission("personalpvp.reload")
    @Description("Reload the PersonalPVP configuration file.")
    public void onReload(final CommandSender s) {
        /*
        Reload the configuration files.
         */
        PPVPPlugin.inst().reloadConfigs();
        /*
        Send the command feedback.
         */
        Utils.send(s, Utils.parse("<green>PVP Config Reloaded."), true, false);
    }
    /*
    /pvp togglebar
     */
    @Subcommand("togglebar")
    @CommandPermission("personalpvp.toggleactionbar")
    @Description("Toggle your actionbar visibility.")
    public void onToggleBar(final Player p) {
        /*
        Toggle the player's action bar and send the command feedback in the process.
         */
        Utils.send(p, Utils.parse(PPVPPlugin.inst().toggleHiddenActionbar(p) ?
                "<green>Action bar enabled.":"<green>Action bar disabled."),
                true, false);
    }
}