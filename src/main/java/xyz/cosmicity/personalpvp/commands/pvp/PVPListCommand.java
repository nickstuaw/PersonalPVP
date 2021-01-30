package xyz.cosmicity.personalpvp.commands.pvp;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;
import xyz.cosmicity.personalpvp.commands.Command;
import xyz.cosmicity.personalpvp.commands.CommandDetails;
import xyz.cosmicity.personalpvp.managers.PVPManager;

import java.util.List;
import java.util.stream.Collectors;

public class PVPListCommand extends Command {
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
