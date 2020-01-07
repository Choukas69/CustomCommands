package fr.azuria.proxy.api.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import fr.azuria.proxy.ProxyAPI;
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
    private ProxyAPI api;

    public CommandsManager(ProxyPlugin plugin) {
        this.plugin = plugin;
        this.api = plugin.getAPI();

        this.registerCommands();
    }

    private void registerCommands() {
        Gson gson = new Gson();

        Type hashType = new TypeToken<HashMap<String, JsonObject>>() {}.getType();

        try {
            HashMap<String, JsonObject> commands = gson.fromJson(new FileReader(new File(this.plugin.getDataFolder(), "commands.json")), hashType);

            for (Map.Entry<String, JsonObject> entry : commands.entrySet()) {
                String name = entry.getKey();

                CustomCommand.Deserializer deserializer = new CustomCommand.Deserializer(entry);

                String packageName = "fr.azuria.proxy.core.commands";
                Class<?> clazz = Class.forName(packageName + "." + name.substring(0, 1).toUpperCase() + name.substring(1) + "Command");

                CustomCommand command = (CustomCommand) clazz.getDeclaredConstructor(CustomCommand.Deserializer.class, ProxyAPI.class).newInstance(deserializer, api);

                plugin.getProxy().getPluginManager().registerCommand(plugin, command); // Once the command is parsed, we save it
            }

        } catch (FileNotFoundException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
