package fr.azuria.proxy.tools.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCommand extends Command {

    private String name;
    private String permission;
    private String[] aliases;
    private String description = "Pas de description";
    private String notPermMessage = "Vous n'avez pas la permission d'éxécuter cette commande";
    private HashMap<String, CommandUsage> usages;

    public AbstractCommand(String name, String permission, String[] aliases) {
        super(name, permission, aliases);

        this.name = name;
        this.permission = permission;
        this.aliases = aliases;
    }

    public void build(JsonObject jsonObject) {
        if (jsonObject.has("description")) {
            description = jsonObject.get("description").getAsString();
        }

        if (jsonObject.has("notPermMessage")) {
            notPermMessage = jsonObject.get("notPermMessage").getAsString();
        }

        if (jsonObject.has("usages")) {
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, CommandUsage>>() {
            }.getType();

            usages = gson.fromJson(jsonObject.get("usages"), type);

            for (Map.Entry<String, CommandUsage> entry : usages.entrySet()) {
                entry.getValue().name = entry.getKey();
            }
        }
    }

    public abstract void run(CommandSender sender, String[] args);

    @Override
    public void execute(CommandSender sender, String[] args) {
        /** Les traitements à effectuer pré-éxécution (verif des perms, ...) sont à faire ici**/

        // Si tout est bon, on peut appeler le run
        this.run(sender, args);
    }

    public static class CommandUsage {
        private String name;
        private String usage;
        private String description = "/";
        private String permission;
        private String notPermMessage = "Vous n'avez pas la permission d'éxécuter cette commande";
    }
}

