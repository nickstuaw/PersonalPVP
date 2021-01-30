package xyz.cosmicity.personalpvp.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;
import xyz.cosmicity.personalpvp.managers.PVPManager;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler {

    private static final List<CommandDetails> commands = new ArrayList<>();

    private static final List<String> permissions = new ArrayList<>();

    @SuppressWarnings("ConstantConditions")
    public CommandHandler() {
        PPVPPlugin pl = PPVPPlugin.inst();
        ConfigurationSection section;
        CommandDetails details;
        section = pl.commands().getConfigurationSection("commands.reload");
        details = new CommandDetails(section);
        setup(details,new ReloadCommand());
        section = pl.commands().getConfigurationSection("commands.update");
        details = new CommandDetails(section);
        setup(details,new UpdateCommand());
        section = pl.commands().getConfigurationSection("commands.pvp");
        details = new CommandDetails(section);
        setup(details,new PVPCommand());
        permissions.add(details.permission() + ".other");
        permissions.add(details.permission() + ".bypass");
        section = pl.commands().getConfigurationSection("commands.togglebar");
        details = new CommandDetails(section);
        setup(details,new ToggleBarCommand());
        section = pl.commands().getConfigurationSection("commands.pvpcontrol");
        details = new CommandDetails(section);
        setup(details,new PVPControlCommand());
        permissions.add(details.permission() + ".admin");
        section = pl.commands().getConfigurationSection("commands.pvpother");
        details = new CommandDetails(section);
        setup(details,new PVPOtherCommand());
        section = pl.commands().getConfigurationSection("commands.pvpresetplayer");
        details = new CommandDetails(section);
        setup(details,new PVPResetPlayerCommand());
        section = pl.commands().getConfigurationSection("commands.pvplist");
        details = new CommandDetails(section);
        setup(details,new PVPListCommand());
        section = pl.commands().getConfigurationSection("commands.help");
        details = new CommandDetails(section);
        setup(details,new HelpCommand());
        section = pl.commands().getConfigurationSection("commands.perms");
        details = new CommandDetails(section);
        if(details.isOn()) {
            commands.add(details);
            permissions().addAll(commands.stream().map(CommandDetails::permission).map(CommandPermission::toString).collect(Collectors.toList()));
            new PermsCommand().register(details);
        }
    }
    public static List<CommandDetails> commands() {return commands;}
    public static List<String> permissions() {return permissions;}

    public void setup(final CommandDetails d, final Command c) {
        if(d.isOn()) {
            commands.add(d);
            c.register(d);
        }
    }
}
class PVPCommand extends Command{
    public void register(final CommandDetails details) {
        details.registerPlayerCommand((p, args) -> {
            if(Utils.togglePersonal(p, PPVPPlugin.inst())) notifyConsole(p.getName(), PVPManager.isPvpEnabled(p.getUniqueId()));
        });
    }
    private void notifyConsole(final String tName, final boolean setTo) {
        if(PPVPPlugin.inst().pvp_toggle_log_to_console()) Utils.send(Utils.parse(PPVPPlugin.inst().console_format(),"name",tName,"pvpstatus", PPVPPlugin.inst().console_pvpstatuses()[(setTo?0:1)]));
    }
}
class PVPControlCommand extends Command {
    private final String title = "<gradient:green:aqua>- - - -</gradient> <white><bold>PVP Control</bold> <gradient:aqua:green>- - - -</gradient>";
    public void register(final CommandDetails details) {
        PPVPPlugin pl = PPVPPlugin.inst();
        details.registerCommand((s, args) -> Utils.send(s, Utils.parse(this.title+"\n" + pl.pvpcontrol_personal_lines() +(s.hasPermission(details.permission()+".admin")?"\n<green><underlined>Admin</underlined>\n"+pl.pvpcontrol_lines()+"\n":"\n")+this.title), true, false));
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
class PVPListCommand extends Command {
    public void register(final CommandDetails details) {
        details.registerCommand((s, args) -> {
            List<String> list = PVPManager.players().stream()
                    .filter(PVPManager::pvpPositive)
                    .map(Bukkit::getOfflinePlayer)
                    .map(OfflinePlayer::getName).collect(Collectors.toList());
            Utils.send(s, Utils.parse((!PPVPPlugin.inst().default_pvp_status()?"<aqua>PVP is enabled for: </aqua>":"<green>PVP is disabled for: </green>")+String.join(", ",list)),true,false);
        });
    }
}
class PVPOtherCommand extends Command {
    private CommandDetails details;
    public void register(final CommandDetails details) {
        this.details = details;
        this.details.registerCommand((s,a)->Utils.send(s, Utils.parse(""),true,false));
        this.details.registerCommand(this::execute,
                new TextArgument("operation").overrideSuggestions("status", "toggle","reset","enable","disable"),
                new EntitySelectorArgument("player", EntitySelectorArgument.EntitySelector.MANY_PLAYERS));
    }
    private void execute(final CommandSender s, final Object[] args) throws WrapperCommandSyntaxException {
        if(!s.hasPermission(this.details.permission()+".other")) return;
        List<Player> players = new ArrayList<>((Collection<Player>) args[1]);
        List<UUID> playerUuids = players.stream().map(Player::getUniqueId).collect(Collectors.toList());
        String msg, operation = ((String)args[0]).toLowerCase();
        final List<Integer> quantity = new ArrayList<>(Collections.singletonList(0));
        if(players.stream().anyMatch(Objects::isNull)) {
            CommandAPI.fail("Failed: "+(players.size()==1?"That player is":"Those players are")+" offline.");
            return;
        }
        switch (operation) {
            case "status":
                players.forEach(target-> Utils.send(s, Utils.parse("<yellow>"+target.getDisplayName()+" has PVP " + (PVPManager.pvpPositive(target.getUniqueId()) ? "<aqua>ENABLED." : "<green>DISABLED.")), true, false));
                return;
            case "toggle":
                playerUuids.forEach(u -> {
                    PVPManager.toggle(u);
                    quantity.set(0, quantity.get(0)+1);
                });
                msg = "<gray>toggled</gray>";
                break;
            case "reset":
                players.stream().map(Player::getUniqueId).filter(PVPManager::pvpPositive)
                        .forEach(u->{
                            PVPManager.remove(u);
                            quantity.set(0, quantity.get(0)+1);
                        });
                msg = PPVPPlugin.inst().default_pvp_status()?"<aqua>enabled</aqua>":"<green>disabled</green>";
                break;
            case "enable":
                playerUuids.stream().filter(PVPManager::isPvpDisabled)
                        .forEach(u->{
                            PVPManager.toggle(u);
                            quantity.set(0, quantity.get(0)+1);
                        });
                msg = "<aqua>enabled</aqua>";
                break;
            case "disable":
                playerUuids.stream().filter(PVPManager::isPvpEnabled)
                        .forEach(u->{
                            PVPManager.toggle(u);
                            quantity.set(0, quantity.get(0)+1);
                        });
                msg = "<green>disabled</green>";
                break;
            case "list":
                List<String> list = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId)
                        .filter(PVPManager::pvpPositive).map(Bukkit::getPlayer).filter(Objects::nonNull)
                        .map(Player::getName).collect(Collectors.toList());
                Utils.send(s, Utils.parse((PPVPPlugin.inst().default_pvp_status()?"<aqua>PVP is enabled for: </aqua>":"<green>PVP is disabled for: </green>")+String.join(", ",list)),true,false);
                return;
            default:
                CommandAPI.fail("Unknown operation.");
                return;
        }

        String raw = msg+" <yellow>PVP for <gray><hover:show_text:'<gray>"+ players.stream().map(Player::getName).collect(Collectors.joining(", ")) +"</gray>'>" + (quantity.get(0)>1?quantity.get(0) + " players</hover></gray>.</yellow>":players.get(0).getName()+"</hover></gray>.</yellow>");
        Utils.send(s, Utils.parse("<yellow>You </yellow>"+raw), true, false);
        notifyConsole("<yellow>"+s.getName()+"</yellow> "+raw);
    }
    private void notifyConsole(final String msg) {
        if(PPVPPlugin.inst().pvp_toggle_log_to_console()) Utils.send(Utils.parse(msg));
    }
}
class PVPResetPlayerCommand extends Command {
    public void register(final CommandDetails details) {
        details.registerCommand((s, args) -> {
            OfflinePlayer target = Bukkit.getServer().getOfflinePlayerIfCached((String) args[0]);
            if(target == null) {
                CommandAPI.fail("Player not found.");
                return;
            }
            PVPManager.remove(target.getUniqueId());
            Utils.send(s, Utils.parse(PPVPPlugin.inst().default_pvp_status()?"<aqua>":"<green>"+"PVP status reset for "+target.getName()), true, false);
        }, new TextArgument("player"));
    }
}
class HelpCommand extends Command {
    private final String title = "<gradient:green:aqua>- - - -</gradient> <white><hover:show_text:'"+ PPVPPlugin.VERSION +"'>PersonalPVP</hover> <bold>Help</bold> <gradient:aqua:green>- - - -</gradient>";
    private final String consoleTitle = "\n<yellow>- - - - <white>PersonalPVP Help</white> - - - -</yellow>";
    public void register(final CommandDetails details) {
        details.registerCommand((s, args) -> {
            Stream<CommandDetails> commandStream = CommandHandler.commands().stream().filter(details1 -> s.hasPermission(details1.permission().toString()));
            if(s instanceof Player) {
                Utils.send(s, Utils.parse(
                        this.title + "\n"
                                + commandStream
                                .map(this::getLine).collect(Collectors.joining("\n")) +
                                "\n" + this.title
                ), true, false);return;
            }
            Utils.send(Utils.parse(this.consoleTitle+"\n"+commandStream
                    .map(this::getConsoleLine).collect(Collectors.joining("\n"))+consoleTitle));
        });
    }

    public String getLine(final CommandDetails details) {
        return "<click:suggest_command:/"+details.label()+"><hover:show_text:'<gradient:green:aqua><italic>Aliases: /"+String.join(", /", details.aliases())+"'><gradient:green:aqua>/"+details.label()+"</gradient> <yellow> - " + details.description() + "</hover></click>";
    }
    public String getConsoleLine(final CommandDetails details) {
        return "<white>/"+details.label()+"</gradient></white> <yellow> - " + details.description() +"</yellow>";
    }
}
class PermsCommand extends Command {
    private final String title = "<gradient:green:aqua>- - - -</gradient> <white><hover:show_text:'"+ PPVPPlugin.VERSION +"'>PersonalPVP</hover> <bold>Permissions</bold> <gradient:aqua:green>- - - -</gradient>";
    private final String consoleTitle = "\n<yellow>- - - - <white>PersonalPVP Permissions</white> - - - -";
    public void register(final CommandDetails details) {
        List<String> permissionStrings = CommandHandler.permissions(), permissions = new ArrayList<>();
        permissionStrings.forEach(permission -> permissions.add("\n<gray>• </gray><color:#72a6cc><italic><hover:show_text:'<gradient:#437da8:#3378ab>Click to copy "+permission+"</gradient>'><click:copy_to_clipboard:"+permission+">"+permission+"</click></hover></italic></color:#72a6cc>"));
        details.registerCommand((s, args) -> {
            if(s instanceof Player) Utils.send(s, Utils.parse(
                    this.title+"\n<gray>• <hover:show_text:'<gradient:#437da8:#3378ab>Copy all</gradient>'><click:copy_to_clipboard:"+String.join(",", CommandHandler.permissions())+">Click to copy all.</gray></click></hover>"
                            +String.join("",permissions) + "\n"+this.title),true,false);
            else Utils.send(Utils.parse(this.consoleTitle+"\n"+String.join(",", CommandHandler.permissions())+this.consoleTitle));
        });
    }
}
class ReloadCommand extends Command {
    public void register(final CommandDetails details) {
        details.registerCommand((sender, args) -> {
            PPVPPlugin.inst().reloadConfig();
            Utils.send(sender, Utils.parse("<green>PVP Config Reloaded."), true, false);
        });
    }

}
class ToggleBarCommand extends Command {
    public void register(final CommandDetails details) {
        details.registerPlayerCommand((p, args) -> Utils.send(p, Utils.parse(PPVPPlugin.inst().toggleHiddenActionbar(p) ? "<green>Action bar enabled.":"<green>Action bar disabled."), true, false));
    }
}
class UpdateCommand extends Command {
    public void register(final CommandDetails details) {
        details.registerCommand((s, args) -> {
            final PPVPPlugin pl = PPVPPlugin.inst();
            if(pl.config_version() >= PPVPPlugin.CONFIG_VERSION){
                CommandAPI.fail("No configuration update was found.");
                return;
            }
            double oldVer = pl.config_version();
            File old = new File(pl.getDataFolder(),"old_config.yml");
            if(old.exists()) CommandAPI.fail("Update failed! Delete the existing old_config.yml and try again.");
            File oldConfig = new File(pl.getDataFolder(),"config.yml");
            oldConfig.renameTo(old);
            pl.saveResource("config.yml", true);
            pl.reloadConfig();
            Utils.send(s, Utils.parse("<hover:show_text:'<gradient:green:aqua>PersonalPvP</gradient>\n<bold><gold>You can now transfer previous settings from <gray>old_config.yml</gray> to <gray>config.yml</gray> and <yellow><italic>reload</italic></yellow>.</gold>\nRemember to delete <gray>old_config.yml</gray> when done.</bold>\n<blue>[<gray>v</gray><red>"+oldVer+"</red> -> <green>"+pl.config_version()+"</green>]</blue>'><green>Config updated and reloaded! <gray>Hover for further instructions.</hover>"), true, false);
        });
    }
}