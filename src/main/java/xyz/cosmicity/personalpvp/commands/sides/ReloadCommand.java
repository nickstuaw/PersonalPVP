package xyz.cosmicity.personalpvp.commands.sides;

import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;
import xyz.cosmicity.personalpvp.commands.Command;
import xyz.cosmicity.personalpvp.commands.CommandDetails;

public class ReloadCommand extends Command {
    public void register(final CommandDetails details) {
        details.registerCommand((sender, args) -> {
            PPVPPlugin.inst().reloadConfig();
            Utils.send(sender, Utils.parse("<green>PVP Config Reloaded."), true, false);
        });
    }

}
