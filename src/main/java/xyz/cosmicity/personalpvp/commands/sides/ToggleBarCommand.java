package xyz.cosmicity.personalpvp.commands.sides;

import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;
import xyz.cosmicity.personalpvp.commands.Command;
import xyz.cosmicity.personalpvp.commands.CommandDetails;

public class ToggleBarCommand extends Command {
    public void register(final CommandDetails details) {
        details.registerPlayerCommand((p, args) -> {
            Utils.send(p, Utils.parse(PPVPPlugin.inst().toggleHiddenActionbar(p) ? "<green>Action bar enabled.":"<green>Action bar disabled."), true, false);
        });
    }
}
