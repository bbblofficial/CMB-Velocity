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

    // ------------------------------------------------------------
    // 1) Block execution
    // ------------------------------------------------------------
    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) return;
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

        // A) Direct block (includes namespaced commands if listed)
        if (config.isBlocked(normalized)) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            send(player, config.getBlockedMsg());
            if (config.isLogBlocked()) {
                logger.info("[Blocked] {} tried /{}", player.getUsername(), raw);
            }
            return;
        }

        // B) Namespaced variant block  e.g. /bukkit:op  /minecraft:give
        if (config.isBlockNamespaced() && normalized.contains(":")) {
            String afterColon = normalized.substring(normalized.indexOf(':') + 1);
            if (config.isBlocked(afterColon)) {
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
        if (partial == null) return;

        // We only filter suggestions starting with '/' (command suggestions)
        if (!partial.startsWith("/")) return;

        String prefixRaw = partial.substring(1); // strip '/'
        String prefixLower = config.isCaseInsensitive()
                ? prefixRaw.toLowerCase(Locale.ROOT) : prefixRaw;

        List<String> original = event.getSuggestions();
        if (original.isEmpty()) return;

        List<String> filtered = new ArrayList<>(original.size());
        for (String suggestion : original) {
            String clean = suggestion.startsWith("/") ? suggestion.substring(1) : suggestion;
            String cleanLower = config.isCaseInsensitive()
                    ? clean.toLowerCase(Locale.ROOT) : clean;

            // Keep the suggestion only if it's NOT blocked
            boolean blocked = config.isBlocked(cleanLower);

            // Namespaced suggestions
            if (!blocked && config.isBlockNamespaced() && cleanLower.contains(":")) {
                String after = cleanLower.substring(cleanLower.indexOf(':') + 1);
                blocked = config.isBlocked(after);
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
