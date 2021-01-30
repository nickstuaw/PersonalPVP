package xyz.cosmicity.personalpvp.commands.sides;

import dev.jorel.commandapi.CommandAPI;
import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;
import xyz.cosmicity.personalpvp.commands.Command;
import xyz.cosmicity.personalpvp.commands.CommandDetails;

import java.io.File;

public class UpdateCommand extends Command {
    public void register(final CommandDetails details) {
        details.registerCommand((s, args) -> {
            final PPVPPlugin pl = PPVPPlugin.inst();
            if(pl.config_version() >= 1.0){
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
