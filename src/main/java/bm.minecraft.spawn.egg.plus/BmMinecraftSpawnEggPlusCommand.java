package bm.minecraft.spawn.egg.plus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Handles administrative commands for Spawn Egg PLUS. */
public final class BmMinecraftSpawnEggPlusCommand implements CommandExecutor, TabCompleter {
    private final BmMinecraftSpawnEggPlusPlugin plugin;

    public BmMinecraftSpawnEggPlusCommand(BmMinecraftSpawnEggPlusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }
        if (!plugin.canManage(sender)) {
            plugin.language().send(sender, "no-permission");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "0", "1" -> {
                boolean enabled = args[0].equals("1");
                plugin.setFeatureEnabled(enabled);
                plugin.language().send(sender, enabled ? "enabled" : "disabled");
            }
            case "reload" -> {
                plugin.reloadAll();
                plugin.language().send(sender, "reloaded");
            }
            case "info" -> plugin.language().send(sender, "info", Map.of(
                    "version", plugin.getPluginMeta().getVersion()));
            case "status" -> plugin.language().send(sender, "status", Map.of(
                    "enabled", plugin.language().message(plugin.isFeatureEnabled() ? "status-on" : "status-off")));
            default -> help(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1 || !plugin.canManage(sender)) {
            return List.of();
        }
        String input = args[0].toLowerCase(Locale.ROOT);
        return List.of("0", "1", "info", "status", "reload").stream()
                .filter(option -> option.startsWith(input))
                .toList();
    }

    private void help(CommandSender sender) {
        for (String key : List.of(
                "help-header", "help-craft", "help-capture",
                "help-toggle", "help-reload", "help-info", "help-status", "help-footer")) {
            plugin.language().send(sender, key);
        }
    }
}
