package xyz.cosmicity.personalpvp.commands.pvp;

import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;
import xyz.cosmicity.personalpvp.commands.Command;
import xyz.cosmicity.personalpvp.commands.CommandDetails;
import xyz.cosmicity.personalpvp.managers.PVPManager;

public class PVPCommand extends Command{
    public void register(final CommandDetails details) {
        details.registerPlayerCommand((p, args) -> {
            if(Utils.togglePersonal(p, PPVPPlugin.inst())) notifyConsole(p.getName(), PVPManager.isPvpEnabled(p.getUniqueId()));
        });
    }
    private void notifyConsole(final String tName, final boolean setTo) {
        if(PPVPPlugin.inst().pvp_toggle_log_to_console()) Utils.send(Utils.parse(PPVPPlugin.inst().console_format(),"name",tName,"pvpstatus", PPVPPlugin.inst().console_pvpstatuses()[(setTo?0:1)]));
    }
}
