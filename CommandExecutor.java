package fr.azuria.proxy.api.commands;

import fr.azuria.proxy.api.AzuriaPlayer;

/*
 * Copyright (c) 2020.
 * This file is entirely part of Azuria and cannot be copied without Choukas permission
 */
public interface CommandExecutor {

    void run(AzuriaPlayer sender, String[] args);
}
