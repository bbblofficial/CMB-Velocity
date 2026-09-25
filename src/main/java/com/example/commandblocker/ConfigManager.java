package com.example.commandblocker;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigManager {

    private final Path dataDirectory;
    private final Logger logger;

    // Settings
    private boolean useWhitelist = false;
    private boolean caseInsensitive = true;
    private boolean logBlocked = true;

    // Messages
    private String prefix = "&8[&cCommandBlocker&8] &r";
    private String blockedMsg = "&cYou don't have permission to use this command!";
    private String reloadSuccess = "&aConfiguration reloaded successfully!";
    private String reloadFailed = "&cFailed to reload configuration. Check console.";
    private String noPermission = "&cYou don't have permission to use this command.";
    private String usage = "&eUsage: &f/commandblocker reload";

    // Lists
    private List<String> blockedCommands = new ArrayList<>();
    private List<String> allowedCommands = new ArrayList<>();

    public ConfigManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    @SuppressWarnings("unchecked")
    public void load() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configFile = dataDirectory.resolve("config.yml");
            if (!Files.exists(configFile)) {
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile);
                        logger.info("Default config.yml created.");
                    } else {
                        logger.warn("Could not find default config.yml in resources!");
                    }
                }
            }

            try (InputStream in = Files.newInputStream(configFile)) {
                Yaml yaml = new Yaml();
                Map<String, Object> root = yaml.load(in);
                if (root == null) root = new HashMap<>();

                // --- settings ---
                Object settingsObj = root.get("settings");
                if (settingsObj instanceof Map<?, ?> settings) {
                    useWhitelist = getBool(settings, "use-whitelist", false);
                    caseInsensitive = getBool(settings, "case-insensitive", true);
                    logBlocked = getBool(settings, "log-blocked", true);
                }

                // --- messages ---
                Object messagesObj = root.get("messages");
                if (messagesObj instanceof Map<?, ?> messages) {
                    prefix = getStr(messages, "prefix", prefix);
                    blockedMsg = getStr(messages, "blocked", blockedMsg);
                    reloadSuccess = getStr(messages, "reload-success", reloadSuccess);
                    reloadFailed = getStr(messages, "reload-failed", reloadFailed);
                    noPermission = getStr(messages, "no-permission", noPermission);
                    usage = getStr(messages, "usage", usage);
                }

                // --- lists ---
                blockedCommands = toStringList(root.get("blocked-commands"));
                allowedCommands = toStringList(root.get("allowed-commands"));

                logger.info("Config loaded: {} blocked, {} allowed (whitelist mode: {})",
                        blockedCommands.size(), allowedCommands.size(), useWhitelist);
            }
        } catch (IOException e) {
            logger.error("Failed to load config.yml", e);
        }
    }

    private static boolean getBool(Map<?, ?> map, String key, boolean def) {
        Object o = map.get(key);
        if (o == null) return def;
        return Boolean.parseBoolean(String.valueOf(o));
    }

    private static String getStr(Map<?, ?> map, String key, String def) {
        Object o = map.get(key);
        return o == null ? def : String.valueOf(o);
    }

    private List<String> toStringList(Object obj) {
        List<String> list = new ArrayList<>();
        if (obj instanceof List<?> raw) {
            for (Object o : raw) {
                String s = String.valueOf(o).trim();
                if (!s.isEmpty()) {
                    list.add(caseInsensitive ? s.toLowerCase(Locale.ROOT) : s);
                }
            }
        }
        return list;
    }

    public boolean isBlocked(String command) {
        String cmd = caseInsensitive ? command.toLowerCase(Locale.ROOT) : command;
        if (useWhitelist) {
            return !allowedCommands.contains(cmd);
        } else {
            return blockedCommands.contains(cmd);
        }
    }

    // Getters
    public boolean isUseWhitelist() { return useWhitelist; }
    public boolean isCaseInsensitive() { return caseInsensitive; }
    public boolean isLogBlocked() { return logBlocked; }
    public String getPrefix() { return prefix; }
    public String getBlockedMsg() { return blockedMsg; }
    public String getReloadSuccess() { return reloadSuccess; }
    public String getReloadFailed() { return reloadFailed; }
    public String getNoPermission() { return noPermission; }
    public String getUsage() { return usage; }
    public List<String> getBlockedCommands() { return blockedCommands; }
    public List<String> getAllowedCommands() { return allowedCommands; }
}
