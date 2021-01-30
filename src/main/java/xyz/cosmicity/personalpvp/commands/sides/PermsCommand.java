package xyz.cosmicity.personalpvp.commands.sides;

import org.bukkit.entity.Player;
import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;
import xyz.cosmicity.personalpvp.commands.Command;
import xyz.cosmicity.personalpvp.commands.CommandDetails;
import xyz.cosmicity.personalpvp.commands.CommandHandler;

import java.util.ArrayList;
import java.util.List;

public class PermsCommand extends Command {
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
