package xyz.cosmicity.personalpvp.storage;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;

public class CommandDetails {

    private final boolean on;
    private final String label, description;
    private final CommandPermission permission;
    private final String[] aliases;

    public CommandDetails(final ConfigurationSection section){
        this.on = section.getBoolean("enabled");
        this.label = section.getString("label");
        this.permission = CommandPermission.fromString(section.getString("permission"));
        this.aliases = section.getStringList("aliases").toArray(String[]::new);
        this.description = section.getString("description");
        handleDupes();
    }

    public boolean isOn() {return this.on;}

    public String[] aliases() {return this.aliases;}

    public String label() {return this.label;}

    public CommandPermission permission() {return this.permission;}

    public String description(){return this.description;}

    private void handleDupes() {
        CommandAPI.unregister(this.label);
        Arrays.stream(this.aliases).forEach(CommandAPI::unregister);
    }

    public void registerPlayerCommand(final PlayerCommandExecutor e) {
        new CommandAPICommand(this.label).withAliases(this.aliases).withPermission(this.permission).executesPlayer(e).register();
    }
    public void registerCommand(final CommandExecutor e) {
        new CommandAPICommand(this.label)
                .withAliases(this.aliases)
                .withPermission(this.permission)
                .executes(e)
                .register();
    }
    public void registerCommand(final CommandExecutor e, final String permissionSuffix, final Argument... args) {
        new CommandAPICommand(this.label)
                .withAliases(this.aliases)
                .withPermission(CommandPermission.fromString(this.permission.toString()+"."+permissionSuffix))
                .withArguments(args)
                .executes(e)
                .register();
    }
    public void registerCommand(final CommandExecutor e, final Argument... args) {
        new CommandAPICommand(this.label)
                .withAliases(this.aliases)
                .withPermission(this.permission)
                .withArguments(args)
                .executes(e)
                .register();
    }

}


