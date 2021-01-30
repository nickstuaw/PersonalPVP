package xyz.cosmicity.personalpvp.commands.pvp;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;
import xyz.cosmicity.personalpvp.commands.Command;
import xyz.cosmicity.personalpvp.commands.CommandDetails;
import xyz.cosmicity.personalpvp.managers.PVPManager;

import java.util.*;
import java.util.stream.Collectors;

public class PVPOtherCommand extends Command {
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
        String msg = "", operation = ((String)args[0]).toLowerCase();
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

    private void notifyConsole(final String tName, final boolean setTo) {
        if(PPVPPlugin.inst().pvp_toggle_log_to_console()) Utils.send(Utils.parse(PPVPPlugin.inst().console_format(),"name",tName,"pvpstatus", PPVPPlugin.inst().console_pvpstatuses()[(setTo?0:1)]));
    }
    private void notifyConsole(final String msg) {
        if(PPVPPlugin.inst().pvp_toggle_log_to_console()) Utils.send(Utils.parse(msg));
    }
}
