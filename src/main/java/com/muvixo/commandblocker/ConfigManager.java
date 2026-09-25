package com.muvixo.commandblocker;

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

    // settings
    private boolean useWhitelist = false;
    private boolean caseInsensitive = true;
    private boolean logBlocked = true;
    private boolean blockTabComplete = true;
    private boolean blockNamespaced = true;
    private boolean blockDangerousArgs = true;

    // messages (defaults - overridden by config.yml)
    private String prefix = "&b&lMineStorm &7\u00BB &r";
    private String blockedMsg = "&cYou don't have permission to use this command!";
    private String blockedNamespaced = "&cThat command is not allowed here!";
    private String blockedArgs = "&cThat sub-command is restricted!";
    private String creatorMessage = "&bCreated by &f&lMuvixo";
    private String reloadSuccess = "&aConfiguration reloaded successfully!";
    private String reloadFailed = "&cFailed to reload configuration. Check console.";
    private String noPermission = "&cYou don't have permission to use this command.";
    private String usage = "&bUsage: &f/commandblocker <reload|creator>";

    // lists
    private List<String> blockedCommands = new ArrayList<>();
    private List<String> allowedCommands = new ArrayList<>();
    private List<String> dangerousArgs = new ArrayList<>();

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

                Object settingsObj = root.get("settings");
                if (settingsObj instanceof Map<?, ?> settings) {
                    useWhitelist = getBool(settings, "use-whitelist", false);
                    caseInsensitive = getBool(settings, "case-insensitive", true);
                    logBlocked = getBool(settings, "log-blocked", true);
                    blockTabComplete = getBool(settings, "block-tab-complete", true);
                    blockNamespaced = getBool(settings, "block-namespaced", true);
                    blockDangerousArgs = getBool(settings, "block-dangerous-args", true);
                }

                Object messagesObj = root.get("messages");
                if (messagesObj instanceof Map<?, ?> messages) {
                    prefix = getStr(messages, "prefix", prefix);
                    blockedMsg = getStr(messages, "blocked", blockedMsg);
                    blockedNamespaced = getStr(messages, "blocked-namespaced", blockedNamespaced);
                    blockedArgs = getStr(messages, "blocked-args", blockedArgs);
                    creatorMessage = getStr(messages, "creator", creatorMessage);
                    reloadSuccess = getStr(messages, "reload-success", reloadSuccess);
                    reloadFailed = getStr(messages, "reload-failed", reloadFailed);
                    noPermission = getStr(messages, "no-permission", noPermission);
                    usage = getStr(messages, "usage", usage);
                }

                blockedCommands = toStringList(root.get("blocked-commands"));
                allowedCommands = toStringList(root.get("allowed-commands"));
                dangerousArgs = toStringList(root.get("dangerous-args"));

                logger.info("Config loaded: {} blocked, {} allowed, {} dangerous-args (whitelist: {})",
                        blockedCommands.size(), allowedCommands.size(),
                        dangerousArgs.size(), useWhitelist);
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

    /** Returns true if the given command name should be blocked. */
    public boolean isBlocked(String commandName) {
        String cmd = caseInsensitive ? commandName.toLowerCase(Locale.ROOT) : commandName;

        // Strip leading '/' just in case
        if (cmd.startsWith("/")) cmd = cmd.substring(1);

        if (useWhitelist) {
            return !allowedCommands.contains(cmd);
        }
        return blockedCommands.contains(cmd);
    }

    /** Returns true if the first argument is dangerous. */
    public boolean isDangerousArg(String arg) {
        if (!blockDangerousArgs) return false;
        String a = caseInsensitive ? arg.toLowerCase(Locale.ROOT) : arg;
        return dangerousArgs.contains(a);
    }

    // Getters
    public boolean isUseWhitelist() { return useWhitelist; }
    public boolean isCaseInsensitive() { return caseInsensitive; }
    public boolean isLogBlocked() { return logBlocked; }
    public boolean isBlockTabComplete() { return blockTabComplete; }
    public boolean isBlockNamespaced() { return blockNamespaced; }
    public boolean isBlockDangerousArgs() { return blockDangerousArgs; }
    public String getPrefix() { return prefix; }
    public String getBlockedMsg() { return blockedMsg; }
    public String getBlockedNamespaced() { return blockedNamespaced; }
    public String getBlockedArgs() { return blockedArgs; }
    public String getCreatorMessage() { return creatorMessage; }
    public String getReloadSuccess() { return reloadSuccess; }
    public String getReloadFailed() { return reloadFailed; }
    public String getNoPermission() { return noPermission; }
    public String getUsage() { return usage; }
    public List<String> getBlockedCommands() { return blockedCommands; }
    public List<String> getAllowedCommands() { return allowedCommands; }
    public List<String> getDangerousArgs() { return dangerousArgs; }
}
