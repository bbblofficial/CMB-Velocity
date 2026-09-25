package com.example.commandblocker;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

public class CommandListener {

    public static final String BYPASS_PERMISSION = "commandblocker.bypass";

    private final ConfigManager config;
    private final Logger logger;

    public CommandListener(ConfigManager config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        // Only handle commands from players
        if (!(event.getCommandSource() instanceof Player player)) return;

        // Bypass permission (admins / VIPs with the permission)
        if (player.hasPermission(BYPASS_PERMISSION)) return;

        String raw = event.getCommand();
        if (raw == null) return;
        raw = raw.trim();
        if (raw.isEmpty()) return;

        // Take first token (the command name without arguments)
        String name = raw.split(" ", 2)[0];
        if (name.startsWith("/")) name = name.substring(1);

        if (config.isBlocked(name)) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            String msg = config.getPrefix() + config.getBlockedMsg();
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));

            if (config.isLogBlocked()) {
                logger.info("Blocked command '{}' for player {}",
                        name, player.getUsername());
            }
        }
    }
}
