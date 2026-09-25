package com.example.commandblocker;

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
    description = "Block commands for members on the whole proxy",
    authors = {"You"}
)
public class CommandBlocker {

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

        // Register command blocker listener
        server.getEventManager().register(this, new CommandListener(configManager, logger));

        // Register /commandblocker reload command
        CommandManager cm = server.getCommandManager();
        CommandMeta meta = cm.metaBuilder("commandblocker")
                .aliases("cb", "cmdblock", "vcb")
                .plugin(this)
                .build();
        cm.register(meta, new ReloadCommand(configManager, logger));

        logger.info("VelocityCommandBlocker enabled - blocking commands for members.");
    }
}
