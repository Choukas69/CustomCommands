package fr.azuria.proxy.api.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import fr.azuria.proxy.ProxyAPI;
import fr.azuria.proxy.api.AzuriaPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * Copyright (c) 2020.
 * This file is entirely part of Azuria and cannot be copied without Choukas permission
 */
public abstract class CustomCommand extends Command implements CommandExecutor {

    private ProxyAPI api;

    private String name;
    private String permission;
    private String[] aliases;
    private String description;
    private String notPermMessage;
    private HashMap<String, CommandUsage> usages;

    private static final String USAGE_HELP_FORMAT = "/%command% %usage% : %description%";

    private CustomCommand(Deserializer deserializer) {
        super(deserializer.name, deserializer.permission, deserializer.aliases);

        this.name = deserializer.name;
        this.permission = deserializer.permission;
        this.aliases = deserializer.aliases;
        this.description = deserializer.description;
        this.notPermMessage = deserializer.notPermMessage;
        this.usages = deserializer.usages;
    }

    public CustomCommand(Deserializer deserializer, ProxyAPI api) {
        this(deserializer);
        this.api = api;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            if (sender.hasPermission(this.permission)) {
                if (usages.containsKey(args[0])) {
                    CommandUsage usage = usages.get(args[0]);

                    if (sender.hasPermission(usage.permission)) {
                        String[] usageArgs = usage.usage.split(" ");

                        final long requiredArgs = Arrays.stream(usageArgs).filter((s) -> s.startsWith("<")).count();
                        final long facultativeArgs = usageArgs.length - requiredArgs - 1; // Facultatives args = args_length - required_args - 1 (sub command name)

                        if (args.length >= requiredArgs && args.length <= facultativeArgs) {
                            AzuriaPlayer player = api.getPlayerManager().getPlayersList().get(((ProxiedPlayer) sender).getUniqueId());

                            // Everything is ok -> run command
                            this.run(player, args);
                        } else {
                            // Adv Help
                            String help = this.getHelpFormat(usage);
                            sender.sendMessage(new TextComponent(help));
                        }
                    } else {
                        sender.sendMessage(new TextComponent(usage.notPermMessage));
                    }
                } else {
                    // Full help
                    sender.sendMessage(new TextComponent("Aide pour la commande " + this.name + " :"));

                    for (CommandUsage usage : this.usages.values()) {
                        String help = this.getHelpFormat(usage);
                        sender.sendMessage(new TextComponent(help));
                    }
                }
            } else {
                sender.sendMessage(new TextComponent(this.notPermMessage));
            }
        } else {
            sender.sendMessage(new TextComponent("Only players can execute a command"));
        }
    }

    private String getHelpFormat(CommandUsage usage) {
        return USAGE_HELP_FORMAT
                .replace("%command%", this.name)
                .replace("%usage%", usage.name)
                .replace("%description%", usage.description);
    }

    public static class Deserializer {

        private String name;
        private String permission;
        private String[] aliases;
        private String description = "Pas de description";
        private String notPermMessage = "Vous n'avez pas la permission d'éxécuter cette commande";
        private HashMap<String, CommandUsage> usages;

        public Deserializer(Map.Entry<String, JsonObject> entry) {
            this.name = entry.getKey();

            JsonObject object = entry.getValue();

            if (object.has("permission"))
                this.permission = object.get("permission").getAsString();

            if (object.has("aliases")) {
                if (object.isJsonArray()) {
                    JsonArray array = object.getAsJsonArray("aliases");
                    String[] aliases = new String[array.size()];

                    for (int i = 0; i < array.size(); i++) {
                        aliases[i] = array.get(i).getAsString();
                    }

                    this.aliases = aliases;
                }
            }

            if (object.has("description"))
                this.description = object.get("description").getAsString();

            if (object.has("notPermMessage"))
                this.notPermMessage = object.get("notPermMessage").getAsString();

            if (object.has("usages")) {
                Type type = new TypeToken<HashMap<String, CommandUsage>>() {}.getType();
                this.usages = new Gson().fromJson(object.get("usages"), type);
            }
        }
    }

    @SuppressWarnings("unused") // Fields are automatically loaded by Gson
    public static class CommandUsage {

        private String name;
        private String usage;
        private String description = "/";
        private String permission;
        private String notPermMessage = "Vous n'avez pas la permission d'éxécuter cette commande";
    }
}

