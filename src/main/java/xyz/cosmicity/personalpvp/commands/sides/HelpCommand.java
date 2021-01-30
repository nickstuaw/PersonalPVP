package xyz.cosmicity.personalpvp.commands.sides;

import org.bukkit.entity.Player;
import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.Utils;
import xyz.cosmicity.personalpvp.commands.Command;
import xyz.cosmicity.personalpvp.commands.CommandDetails;
import xyz.cosmicity.personalpvp.commands.CommandHandler;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HelpCommand extends Command {
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
