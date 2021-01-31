package xyz.cosmicity.personalpvp.storage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;

public class Message {
    private final String message;
    private Component c;
    public final boolean text, action;
    public Message(final ConfigurationSection sect) {
        this.message = sect.getString("message");
        this.text = sect.getBoolean("text");
        this.action = sect.getBoolean("actionbar");
        this.c = this.message==null?null:MiniMessage.get().parse(this.message);
    }
    public boolean isEmpty() {return this.message.isEmpty();}
    public Message parse(final String... placeholders) {
        this.c = MiniMessage.get().parse(this.message, placeholders);
        return this;
    }
    public Component parse() {
        this.c = MiniMessage.get().parse(this.message);
        return this.c;
    }
    public Component get() {
        return this.c;
    }
}
