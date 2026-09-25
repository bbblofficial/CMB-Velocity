package com.example.commandblocker;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReloadCommand implements SimpleCommand {

    public static final String ADMIN_PERMISSION = "commandblocker.admin";

    private final ConfigManager config;
    private final Logger logger;

    public ReloadCommand(ConfigManager config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    private void send(CommandSource source, String message) {
        source.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getPrefix() + message));
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!source.hasPermission(ADMIN_PERMISSION)) {
            send(source, config.getNoPermission());
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            send(source, config.getUsage());
            return;
        }

        try {
            config.load();
            send(source, config.getReloadSuccess());
            logger.info("Config reloaded by {}", source);
        } catch (Exception ex) {
            send(source, config.getReloadFailed());
            logger.error("Error reloading config", ex);
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(ADMIN_PERMISSION);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(List.of("reload"));
    }
}
