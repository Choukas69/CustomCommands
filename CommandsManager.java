package fr.azuria.proxy.tools.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import fr.azuria.proxy.ProxyPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CommandsManager {

    private ProxyPlugin plugin;

    public CommandsManager(ProxyPlugin plugin) {
        this.plugin = plugin;

        this.registerCommands();
    }

    private void registerCommands() {
        Gson gson = new Gson();

        Type hashType = new TypeToken<HashMap<String, JsonObject>>() {}.getType();

        try {
            HashMap<String, JsonObject> commands = gson.fromJson(new FileReader(new File(this.plugin.getDataFolder(), "commands.json")), hashType);

            for (Map.Entry<String, JsonObject> entry : commands.entrySet()) {
                JsonObject jsonObject = entry.getValue();

                String name = entry.getKey();

                String permission = null;
                if (jsonObject.has("permission")) {
                    permission = jsonObject.get("permission").getAsString();
                }

                String[] aliases = null;
                if (jsonObject.has("aliases")) {
                    if (jsonObject.get("aliases").isJsonArray()) {
                        JsonArray jsonArray = jsonObject.getAsJsonArray("aliases");
                        aliases = new String[jsonArray.size()];

                        for (int i = 0; i < jsonArray.size(); i++) {
                            aliases[i] = jsonArray.get(i).getAsString();
                        }
                    }
                }

                String packageName = "fr.azuria.proxy.core.commands";
                Class<?> clazz = Class.forName(packageName + "." + name.substring(0, 1).toUpperCase() + name.substring(1) + "Command");

                // New instance of the class found
                AbstractCommand command = (AbstractCommand) clazz.getDeclaredConstructor(String.class, String.class, String[].class).newInstance(name, permission, aliases);
                command.build(jsonObject); // The class continues to parse itself

                plugin.getProxy().getPluginManager().registerCommand(plugin, command); // Once the command is parsed, we save it
            }

        } catch (FileNotFoundException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
