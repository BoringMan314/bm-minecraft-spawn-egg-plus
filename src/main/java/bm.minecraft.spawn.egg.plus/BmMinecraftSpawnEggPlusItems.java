package bm.minecraft.spawn.egg.plus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Container;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/** Builds capture eggs and restores them to plain eggs when the plugin is removed. */
public final class BmMinecraftSpawnEggPlusItems {
    private static final int EMPTY_STACK_SIZE = 64;
    private static final int MAX_ENTITY_DATA = 262144;

    private final BmMinecraftSpawnEggPlusPlugin plugin;
    private final NamespacedKey markerKey;
    private final NamespacedKey entityKey;
    private final NamespacedKey typeKey;
    private final NamespacedKey nameKey;
    private final NamespacedKey appearance;

    public BmMinecraftSpawnEggPlusItems(BmMinecraftSpawnEggPlusPlugin plugin) {
        this.plugin = plugin;
        this.markerKey = new NamespacedKey(plugin, "marker");
        this.entityKey = new NamespacedKey(plugin, "entity");
        this.typeKey = new NamespacedKey(plugin, "type");
        this.nameKey = new NamespacedKey(plugin, "name");
        this.appearance = Material.HAPPY_GHAST_SPAWN_EGG.getKey();
    }

    public boolean isCapture(ItemStack stack) {
        Byte marker = read(stack, markerKey, PersistentDataType.BYTE);
        return marker != null && marker == (byte) 1;
    }

    public boolean isFilled(ItemStack stack) {
        return isCapture(stack) && read(stack, entityKey, PersistentDataType.STRING) != null;
    }

    public boolean canCapture(Entity entity) {
        if (!(entity instanceof LivingEntity) || entity instanceof Player || entity instanceof ArmorStand) {
            return false;
        }
        if (!entity.isValid() || entity.isDead() || entity.hasMetadata("NPC")) {
            return false;
        }
        EntityType type = entity.getType();
        return type.isAlive() && type.isSpawnable();
    }

    public ItemStack createEmpty() {
        ItemStack stack = new ItemStack(Material.EGG);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        meta.getPersistentDataContainer().set(markerKey, PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
        return withPlugin(stack);
    }

    public ItemStack capture(Entity entity) {
        try {
            EntitySnapshot snapshot = entity.createSnapshot();
            String data = snapshot.getAsString();
            if (data == null || data.isBlank() || data.length() > MAX_ENTITY_DATA) {
                return null;
            }
            ItemStack stack = new ItemStack(Material.EGG);
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                return null;
            }
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(markerKey, PersistentDataType.BYTE, (byte) 1);
            container.set(entityKey, PersistentDataType.STRING, data);
            container.set(typeKey, PersistentDataType.STRING, entity.getType().getKey().toString());
            Component custom = entity.customName();
            if (hasText(custom)) {
                container.set(nameKey, PersistentDataType.STRING, legacy().serialize(custom));
            }
            stack.setItemMeta(meta);
            return withPlugin(stack);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public EntitySnapshot snapshot(ItemStack stack) {
        String data = read(stack, entityKey, PersistentDataType.STRING);
        if (data == null || data.isBlank()) {
            return null;
        }
        try {
            return Bukkit.getEntityFactory().createEntitySnapshot(data);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public Component label(ItemStack stack) {
        if (!isFilled(stack)) {
            return emptyName();
        }
        return filledName(stack);
    }

    public Component entityLabel(Entity entity) {
        Component custom = entity.customName();
        if (hasText(custom)) {
            return custom;
        }
        return Component.translatable(entity.getType().translationKey())
                .decoration(TextDecoration.ITALIC, false);
    }

    /** Restores the happy-ghast look, name, and stack size while the plugin is loaded. */
    public ItemStack withPlugin(ItemStack stack) {
        return applyPresence(stack, true);
    }

    /** Leaves a plain egg after the plugin is removed. Captured data stays on the item. */
    public ItemStack withoutPlugin(ItemStack stack) {
        return applyPresence(stack, false);
    }

    private ItemStack applyPresence(ItemStack stack, boolean restore) {
        if (stack == null || stack.getType().isAir()) {
            return stack;
        }
        ItemStack result = stack.clone();
        ItemMeta meta = result.getItemMeta();
        boolean changed = false;
        if (meta != null && rewriteContents(meta, restore)) {
            result.setItemMeta(meta);
            changed = true;
            meta = result.getItemMeta();
        }
        if (isCapture(result) && meta != null) {
            applyLook(meta, result, restore);
            result.setItemMeta(meta);
            changed = true;
        }
        if (!changed || result.isSimilar(stack)) {
            return stack;
        }
        result.setAmount(stack.getAmount());
        return result;
    }

    private boolean rewriteContents(ItemMeta meta, boolean restore) {
        boolean changed = false;
        if (meta instanceof BundleMeta bundle && bundle.hasItems()) {
            List<ItemStack> updated = new ArrayList<>();
            boolean innerChanged = false;
            for (ItemStack item : bundle.getItems()) {
                if (item == null || item.getType().isAir()) {
                    continue;
                }
                ItemStack next = applyPresence(item, restore);
                if (next != item) {
                    innerChanged = true;
                }
                if (next != null && !next.getType().isAir()) {
                    updated.add(next);
                }
            }
            if (innerChanged) {
                bundle.setItems(updated);
                changed = true;
            }
        }
        if (meta instanceof BlockStateMeta blockStateMeta
                && blockStateMeta.hasBlockState()
                && blockStateMeta.getBlockState() instanceof Container container
                && rewriteInventory(container.getInventory(), restore)) {
            blockStateMeta.setBlockState(container);
            changed = true;
        }
        return changed;
    }

    private boolean rewriteInventory(Inventory inventory, boolean restore) {
        boolean changed = false;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }
            ItemStack updated = applyPresence(item, restore);
            if (updated == item) {
                continue;
            }
            inventory.setItem(slot, updated);
            changed = true;
        }
        return changed;
    }

    private void applyLook(ItemMeta meta, ItemStack stack, boolean restore) {
        if (!restore) {
            meta.setItemModel(null);
            meta.setEnchantmentGlintOverride(null);
            meta.itemName(null);
            meta.displayName(null);
            meta.customName(null);
            meta.lore(null);
            if (stack.getAmount() <= Material.EGG.getMaxStackSize()) {
                meta.setMaxStackSize(null);
            } else {
                meta.setMaxStackSize(EMPTY_STACK_SIZE);
            }
            return;
        }
        meta.setItemModel(appearance);
        meta.displayName(null);
        meta.customName(null);
        meta.lore(null);
        if (isFilled(stack)) {
            meta.setEnchantmentGlintOverride(Boolean.TRUE);
            meta.setMaxStackSize(1);
            meta.itemName(filledName(stack));
        } else {
            meta.setEnchantmentGlintOverride(null);
            meta.setMaxStackSize(EMPTY_STACK_SIZE);
            meta.itemName(emptyName());
        }
    }

    private Component emptyName() {
        return legacy().deserialize(plugin.language().message("item-name"))
                .decoration(TextDecoration.ITALIC, false);
    }

    private Component filledName(ItemStack stack) {
        String stored = read(stack, nameKey, PersistentDataType.STRING);
        if (stored != null && !stored.isBlank()) {
            Component custom = legacy().deserialize(stored);
            if (hasText(custom)) {
                return custom;
            }
        }
        String typeId = read(stack, typeKey, PersistentDataType.STRING);
        NamespacedKey key = typeId == null ? null : NamespacedKey.fromString(typeId);
        EntityType type = key == null ? null : Registry.ENTITY_TYPE.get(key);
        if (type != null) {
            return Component.translatable(type.translationKey()).decoration(TextDecoration.ITALIC, false);
        }
        return emptyName();
    }

    private boolean hasText(Component component) {
        if (component == null) {
            return false;
        }
        String plain = ChatColor.stripColor(legacy().serialize(component));
        return plain != null && !plain.isBlank();
    }

    private LegacyComponentSerializer legacy() {
        return LegacyComponentSerializer.legacySection();
    }

    private <T, Z> Z read(ItemStack stack, NamespacedKey key, PersistentDataType<T, Z> type) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) {
            return null;
        }
        return stack.getItemMeta().getPersistentDataContainer().get(key, type);
    }
}
