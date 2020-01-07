package fr.azuria.proxy.api.commands;

import fr.azuria.proxy.api.AzuriaPlayer;

public interface CommandExecutor {

    void run(AzuriaPlayer sender, String[] args);
}
