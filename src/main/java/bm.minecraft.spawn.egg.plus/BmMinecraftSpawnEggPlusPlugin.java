package bm.minecraft.spawn.egg.plus;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

/** Captures mobs in eggs crafted from nine eggs and releases them later. */
public final class BmMinecraftSpawnEggPlusPlugin extends JavaPlugin {
    public static final String USE_PERMISSION = "bm-minecraft-spawn-egg-plus.use";
    private static final String ADMIN_PERMISSION = "bm-minecraft-spawn-egg-plus.admin";
    private static final String COMMAND_NAME = "bm-minecraft-spawn-egg-plus";
    private static final String RECIPE_ID = "capture-egg";

    private BmMinecraftSpawnEggPlusLanguageManager languageManager;
    private BmMinecraftSpawnEggPlusItems items;
    private BmMinecraftSpawnEggPlusListener listener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        languageManager = new BmMinecraftSpawnEggPlusLanguageManager(this);
        languageManager.reload();
        items = new BmMinecraftSpawnEggPlusItems(this);
        registerCommand();
        listener = new BmMinecraftSpawnEggPlusListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        syncRecipe();
        listener.restoreLoadedItems();
        getLogger().info(languageManager.getConsole("enabled").replace("{version}", getPluginMeta().getVersion()));
    }

    @Override
    public void onDisable() {
        if (listener != null && items != null) {
            listener.revertLoadedItems();
        }
        if (languageManager != null) {
            getLogger().info(languageManager.getConsole("disabled"));
        }
    }

    public BmMinecraftSpawnEggPlusLanguageManager language() {
        return languageManager;
    }

    public BmMinecraftSpawnEggPlusItems items() {
        return items;
    }

    public boolean isFeatureEnabled() {
        return getConfig().getBoolean("enabled", true);
    }

    public boolean canUse(CommandSender sender) {
        return sender.hasPermission(USE_PERMISSION);
    }

    public boolean canManage(CommandSender sender) {
        return !(sender instanceof Player)
                || !getConfig().getBoolean("admin-require-op", true)
                || sender.isOp()
                || sender.hasPermission(ADMIN_PERMISSION);
    }

    public void setFeatureEnabled(boolean enabled) {
        getConfig().set("enabled", enabled);
        saveConfig();
        syncRecipe();
    }

    public void reloadAll() {
        reloadConfig();
        languageManager.reload();
        syncRecipe();
        listener.restoreLoadedItems();
    }

    public NamespacedKey recipeKey() {
        return new NamespacedKey(this, RECIPE_ID);
    }

    private void registerCommand() {
        PluginCommand command = getCommand(COMMAND_NAME);
        if (command == null) {
            throw new IllegalStateException(languageManager.getConsole("missing-command")
                    .replace("{command}", COMMAND_NAME));
        }
        BmMinecraftSpawnEggPlusCommand executor = new BmMinecraftSpawnEggPlusCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void syncRecipe() {
        NamespacedKey key = recipeKey();
        getServer().removeRecipe(key);
        boolean enabled = isFeatureEnabled();
        if (enabled) {
            ShapedRecipe recipe = new ShapedRecipe(key, items.createEmpty());
            recipe.shape("EEE", "EEE", "EEE");
            recipe.setIngredient('E', RecipeChoice.exactChoice(new ItemStack(Material.EGG)));
            getServer().addRecipe(recipe);
        }
        for (Player player : getServer().getOnlinePlayers()) {
            if (enabled && canUse(player)) {
                player.discoverRecipe(key);
            } else {
                player.undiscoverRecipe(key);
            }
        }
    }
}
