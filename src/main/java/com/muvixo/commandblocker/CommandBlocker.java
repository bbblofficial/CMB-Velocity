package com.muvixo.commandblocker;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
    id = "commandblocker",
    name = "VelocityCommandBlocker",
    version = "1.0.0",
    description = "Block dangerous commands for members across the whole proxy",
    authors = {"Muvixo"}
)
public class CommandBlocker {

    public static final String CREATOR = "Muvixo";

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private ConfigManager configManager;

    @Inject
    public CommandBlocker(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.configManager = new ConfigManager(dataDirectory, logger);
        this.configManager.load();

        // Register command blocking listener
        server.getEventManager().register(this, new CommandListener(configManager, logger));

        // Register /commandblocker (aliases: /cb /vcb /cmdblock)
        CommandManager cm = server.getCommandManager();
        CommandMeta meta = cm.metaBuilder("commandblocker")
                .aliases("cb", "vcb", "cmdblock")
                .plugin(this)
                .build();
        cm.register(meta, new ReloadCommand(configManager, logger));

        logger.info("=================================================");
        logger.info("  VelocityCommandBlocker  -  Created by " + CREATOR);
        logger.info("  Prefix: MineStorm");
        logger.info("  Blocked commands loaded: " + configManager.getBlockedCommands().size());
        logger.info("  Whitelist mode: " + configManager.isUseWhitelist());
        logger.info("=================================================");
    }
}
