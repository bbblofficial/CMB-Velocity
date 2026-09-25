package com.muvixo.commandblocker;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.util.Locale;

public class CommandListener {

    public static final String BYPASS_PERMISSION = "commandblocker.bypass";

    private final ConfigManager config;
    private final Logger logger;

    public CommandListener(ConfigManager config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    private void send(Player player, String message) {
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getPrefix() + message));
    }

    /**
     * Returns true if the player already has permission for this command
     * from another plugin (essentials.*, minecraft.command.*, bukkit.command.*, etc.)
     */
    private boolean hasExternalPermission(Player player, String commandName) {
        if (!config.isRespectOtherPermissions()) return false;

        String cmd = commandName.toLowerCase(Locale.ROOT);
        if (cmd.contains(":")) {
            cmd = cmd.substring(cmd.indexOf(':') + 1);
        }

        String[] candidates = new String[] {
            "essentials." + cmd,
            "essentials.command." + cmd,
            "minecraft.command." + cmd,
            "bukkit.command." + cmd,
            "spigot.command." + cmd,
            "paper.command." + cmd,
            "velocity.command." + cmd,
            "bungeecord.command." + cmd,
            "command." + cmd,
            "commandblocker.command." + cmd,
            cmd,
        };

        for (String perm : candidates) {
            if (player.hasPermission(perm)) {
                return true;
            }
        }
        return false;
    }

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) return;

        // Global bypass
        if (player.hasPermission(BYPASS_PERMISSION)) return;

        String raw = event.getCommand();
        if (raw == null) return;
        raw = raw.trim();
        if (raw.isEmpty()) return;
        if (raw.startsWith("/")) raw = raw.substring(1);

        String[] parts = raw.split(" ", 2);
        String name = parts[0];
        String normalized = config.isCaseInsensitive()
                ? name.toLowerCase(Locale.ROOT) : name;

        // A) Direct block
        if (config.isBlocked(normalized)) {
            if (hasExternalPermission(player, normalized)) return;

            event.setResult(CommandExecuteEvent.CommandResult.denied());
            send(player, config.getBlockedMsg());
            if (config.isLogBlocked()) {
                logger.info("[Blocked] {} tried /{}", player.getUsername(), raw);
            }
            return;
        }

        // B) Namespaced block  /bukkit:op  /minecraft:give
        if (config.isBlockNamespaced() && normalized.contains(":")) {
            String afterColon = normalized.substring(normalized.indexOf(':') + 1);
            if (config.isBlocked(afterColon)) {
                if (hasExternalPermission(player, afterColon)) return;

                event.setResult(CommandExecuteEvent.CommandResult.denied());
                send(player, config.getBlockedNamespaced());
                if (config.isLogBlocked()) {
                    logger.info("[Blocked-ns] {} tried /{}", player.getUsername(), raw);
                }
                return;
            }
        }

        // C) Dangerous first-argument block
        if (config.isBlockDangerousArgs() && parts.length > 1) {
            String firstArg = parts[1].split(" ", 2)[0];
            if (config.isDangerousArg(firstArg)) {
                if (hasExternalPermission(player, normalized)) return;

                event.setResult(CommandExecuteEvent.CommandResult.denied());
                send(player, config.getBlockedArgs());
                if (config.isLogBlocked()) {
                    logger.info("[Blocked-arg] {} tried /{}", player.getUsername(), raw);
                }
            }
        }
    }
}