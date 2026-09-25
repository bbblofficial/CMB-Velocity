package com.muvixo.commandblocker;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
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
     * Returns true if the player appears to have permission for this command
     * from another plugin (essentials, minecraft, bukkit, or a generic
     * "<plugin>.command.<cmd>" / "<plugin>.<cmd>" pattern).
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
            // very common short forms
            cmd,
        };

        for (String perm : candidates) {
            if (player.hasPermission(perm)) {
                return true;
            }
        }

        // Generic: <anyplugin>.command.<cmd>  - Velocity API doesn't expose
        // wildcard enumeration, so we check the common server plugins here.
        // Add more plugin names to config if needed.
        return false;
    }

    // ------------------------------------------------------------
    // 1) Block execution
    // ------------------------------------------------------------
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

        // A) Direct block - but skip if player already has the permission
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
                // Give external permission a chance here too, using the base command
                if (hasExternalPermission(player, normalized)) return;

                event.setResult(CommandExecuteEvent.CommandResult.denied());
                send(player, config.getBlockedArgs());
                if (config.isLogBlocked()) {
                    logger.info("[Blocked-arg] {} tried /{}", player.getUsername(), raw);
                }
            }
        }
    }

    // ------------------------------------------------------------
    // 2) Block tab-complete suggestions for blocked commands
    // ------------------------------------------------------------
    @Subscribe
    public void onTabComplete(TabCompleteEvent event) {
        if (!config.isBlockTabComplete()) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        if (player.hasPermission(BYPASS_PERMISSION)) return;

        String partial = event.getPartialMessage();
        if (partial == null || !partial.startsWith("/")) return;

        List<String> original = event.getSuggestions();
        if (original.isEmpty()) return;

        List<String> filtered = new ArrayList<>(original.size());
        for (String suggestion : original) {
            String clean = suggestion.startsWith("/") ? suggestion.substring(1) : suggestion;
            String cleanLower = config.isCaseInsensitive()
                    ? clean.toLowerCase(Locale.ROOT) : clean;

            boolean blocked = config.isBlocked(cleanLower);

            if (!blocked && config.isBlockNamespaced() && cleanLower.contains(":")) {
                String after = cleanLower.substring(cleanLower.indexOf(':') + 1);
                blocked = config.isBlocked(after);
            }

            // Don't filter out if player actually has the permission
            if (blocked && hasExternalPermission(player, cleanLower)) {
                blocked = false;
            }

            if (!blocked) {
                filtered.add(suggestion);
            }
        }

        if (filtered.size() != original.size()) {
            event.setSuggestions(filtered);
        }
    }
}
