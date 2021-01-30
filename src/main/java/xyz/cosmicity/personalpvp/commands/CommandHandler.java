package xyz.cosmicity.personalpvp.commands;

import dev.jorel.commandapi.CommandPermission;
import org.bukkit.configuration.ConfigurationSection;
import xyz.cosmicity.personalpvp.PPVPPlugin;
import xyz.cosmicity.personalpvp.commands.pvp.PVPCommand;
import xyz.cosmicity.personalpvp.commands.pvp.PVPControlCommand;
import xyz.cosmicity.personalpvp.commands.pvp.PVPListCommand;
import xyz.cosmicity.personalpvp.commands.pvp.PVPOtherCommand;
import xyz.cosmicity.personalpvp.commands.sides.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
