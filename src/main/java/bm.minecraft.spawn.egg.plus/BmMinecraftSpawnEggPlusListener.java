package bm.minecraft.spawn.egg.plus;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;

import net.kyori.adventure.text.Component;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/** Handles crafting, capturing, releasing, and item appearance while the plugin is loaded. */
public final class BmMinecraftSpawnEggPlusListener implements Listener {
    private final BmMinecraftSpawnEggPlusPlugin plugin;
    private final Map<UUID, Integer> actedTick = new HashMap<>();

    public BmMinecraftSpawnEggPlusListener(BmMinecraftSpawnEggPlusPlugin plugin) {
        this.plugin = plugin;
    }

    public void restoreLoadedItems() {
        rewriteLoaded(true);
    }

    public void revertLoadedItems() {
        rewriteLoaded(false);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.isFeatureEnabled() && plugin.canUse(player)) {
            player.discoverRecipe(plugin.recipeKey());
        }
        rewritePlayer(player, true);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        rewriteChunk(event.getChunk(), true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        for (ItemStack item : matrix) {
            if (plugin.items().isCapture(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
        if (!plugin.items().isCapture(event.getInventory().getResult())) {
            return;
        }
        if (!plugin.isFeatureEnabled()
                || !(event.getView().getPlayer() instanceof Player player)
                || !plugin.canUse(player)) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (event.getInventory() instanceof AnvilInventory inventory
                && (plugin.items().isCapture(inventory.getFirstItem())
                || plugin.items().isCapture(inventory.getSecondItem()))) {
            event.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        if (event.getInventory() instanceof GrindstoneInventory inventory
                && (plugin.items().isCapture(inventory.getUpperItem())
                || plugin.items().isCapture(inventory.getLowerItem()))) {
            event.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack held = itemIn(player, event.getHand());
        if (!plugin.items().isCapture(held) || alreadyActed(player)) {
            return;
        }
        markActed(player);
        if (plugin.items().isFilled(held) || player.isSneaking()) {
            return;
        }
        Entity target = targetOf(event.getRightClicked());
        if (!(target instanceof LivingEntity)) {
            return;
        }
        if (plugin.isFeatureEnabled() && plugin.canUse(player) && !(target instanceof Player)) {
            event.setCancelled(true);
        }
        capture(player, event.getHand(), target, held);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack held = event.getItem();
        Player player = event.getPlayer();
        if (!plugin.items().isCapture(held) || alreadyActed(player)) {
            return;
        }
        if (action == Action.RIGHT_CLICK_AIR && player.getTargetEntity(5) != null) {
            return;
        }
        event.setUseItemInHand(Event.Result.DENY);
        if (player.isSneaking() || !plugin.items().isFilled(held)) {
            return;
        }
        if (!plugin.isFeatureEnabled()) {
            plugin.language().send(player, "feature-disabled");
            return;
        }
        if (!plugin.canUse(player)) {
            plugin.language().send(player, "no-use-permission");
            return;
        }
        markActed(player);
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        release(player, event.getHand(), held, event.getClickedBlock(), event.getBlockFace());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLaunch(PlayerLaunchProjectileEvent event) {
        if (plugin.items().isCapture(event.getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if (plugin.items().isCapture(event.getItem())) {
            event.setCancelled(true);
        }
    }

    private void capture(Player player, EquipmentSlot hand, Entity entity, ItemStack held) {
        if (!plugin.isFeatureEnabled()) {
            plugin.language().send(player, "feature-disabled");
            return;
        }
        if (!plugin.canUse(player)) {
            plugin.language().send(player, "no-use-permission");
            return;
        }
        if (!plugin.items().canCapture(entity)) {
            plugin.language().send(player, "cannot-capture");
            return;
        }
        Component label = plugin.items().entityLabel(entity);
        ItemStack filled = plugin.items().capture(entity);
        if (filled == null) {
            plugin.language().send(player, "capture-failed");
            return;
        }
        entity.remove();
        swapHand(player, hand, held, filled);
        plugin.language().sendName(player, "captured", label);
    }

    private void release(Player player, EquipmentSlot hand, ItemStack held, Block clicked, BlockFace face) {
        if (!plugin.isFeatureEnabled()) {
            plugin.language().send(player, "feature-disabled");
            return;
        }
        if (!plugin.canUse(player)) {
            plugin.language().send(player, "no-use-permission");
            return;
        }
        org.bukkit.Location location = releaseLocation(player, clicked, face);
        if (location == null || !spawn(held, location)) {
            plugin.language().send(player, "release-failed");
            return;
        }
        Component label = plugin.items().label(held);
        swapHand(player, hand, held, plugin.items().createEmpty());
        plugin.language().sendName(player, "released", label);
    }

    private boolean spawn(ItemStack held, Location location) {
        EntitySnapshot snapshot = plugin.items().snapshot(held);
        if (snapshot == null || location.getWorld() == null) {
            return false;
        }
        try {
            // createEntity(Location) already spawns the copy. Spawning it again fails and would delete it.
            Entity spawned = snapshot.createEntity(location.getWorld());
            if (spawned == null) {
                return false;
            }
            if (!spawned.spawnAt(location, CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)) {
                return false;
            }
            spawned.setVelocity(new Vector());
            if (spawned instanceof LivingEntity living) {
                living.setFallDistance(0.0F);
            }
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private Location releaseLocation(Player player, Block clicked, BlockFace face) {
        float yaw = player.getLocation().getYaw();
        if (clicked != null) {
            BlockFace side = face == null ? BlockFace.UP : face;
            Location atSide = standingLocation(clicked.getRelative(side), yaw);
            if (atSide != null) {
                return atSide;
            }
            Location above = standingLocation(clicked.getRelative(BlockFace.UP), yaw);
            if (above != null) {
                return above;
            }
        }
        Location ahead = standingLocation(forwardBlock(player), yaw);
        if (ahead != null) {
            return ahead;
        }
        return standingLocation(player.getLocation().getBlock(), yaw);
    }

    private Block forwardBlock(Player player) {
        Location feet = player.getLocation().clone();
        Vector forward = feet.getDirection();
        forward.setY(0.0D);
        if (forward.lengthSquared() < 1.0E-4D) {
            forward = new Vector(0.0D, 0.0D, 1.0D);
        }
        return feet.add(forward.normalize()).getBlock();
    }

    private Location standingLocation(Block block, float yaw) {
        if (block == null) {
            return null;
        }
        Block feet = canReleaseInto(block) ? block : block.getRelative(BlockFace.UP);
        if (!canReleaseInto(feet)) {
            return null;
        }
        Location location = feet.getLocation().add(0.5D, 0.0D, 0.5D);
        location.setYaw(yaw);
        location.setPitch(0.0F);
        return location;
    }

    private boolean canReleaseInto(Block block) {
        if (block == null) {
            return false;
        }
        World world = block.getWorld();
        int y = block.getY();
        if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
            return false;
        }
        return block.isPassable() || block.isLiquid();
    }

    private Entity targetOf(Entity entity) {
        if (entity instanceof EnderDragonPart part && part.getParent() != null) {
            return part.getParent();
        }
        return entity;
    }

    private void swapHand(Player player, EquipmentSlot hand, ItemStack held, ItemStack created) {
        if (held.getAmount() <= 1) {
            setHand(player, hand, created);
            return;
        }
        ItemStack remaining = held.clone();
        remaining.setAmount(held.getAmount() - 1);
        setHand(player, hand, remaining);
        giveOrDrop(player, created);
    }

    private void giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (leftover.isEmpty()) {
            return;
        }
        for (ItemStack remain : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), remain);
        }
        plugin.language().send(player, "dropped");
    }

    private void setHand(Player player, EquipmentSlot hand, ItemStack stack) {
        if (hand == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(stack);
        } else {
            player.getInventory().setItemInMainHand(stack);
        }
    }

    private ItemStack itemIn(Player player, EquipmentSlot hand) {
        if (hand == EquipmentSlot.OFF_HAND) {
            return player.getInventory().getItemInOffHand();
        }
        if (hand == EquipmentSlot.HAND) {
            return player.getInventory().getItemInMainHand();
        }
        return null;
    }

    private boolean alreadyActed(Player player) {
        Integer tick = actedTick.get(player.getUniqueId());
        return tick != null && tick == plugin.getServer().getCurrentTick();
    }

    private void markActed(Player player) {
        int tick = plugin.getServer().getCurrentTick();
        actedTick.put(player.getUniqueId(), tick);
        if (actedTick.size() > 128) {
            actedTick.entrySet().removeIf(entry -> tick - entry.getValue() > 2);
        }
    }

    private void rewriteLoaded(boolean restore) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            rewritePlayer(player, restore);
        }
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                rewriteEntity(entity, restore);
            }
            for (Chunk chunk : world.getLoadedChunks()) {
                rewriteChunk(chunk, restore);
            }
        }
    }

    private void rewritePlayer(Player player, boolean restore) {
        rewriteInventory(player.getInventory(), restore);
        rewriteInventory(player.getEnderChest(), restore);
        ItemStack cursor = player.getItemOnCursor();
        ItemStack mapped = restore ? plugin.items().withPlugin(cursor) : plugin.items().withoutPlugin(cursor);
        if (mapped != cursor) {
            player.setItemOnCursor(mapped);
        }
    }

    private void rewriteChunk(Chunk chunk, boolean restore) {
        for (Entity entity : chunk.getEntities()) {
            rewriteEntity(entity, restore);
        }
        for (BlockState state : chunk.getTileEntities(false)) {
            if (state instanceof InventoryHolder holder && holder.getInventory() != null) {
                rewriteInventory(holder.getInventory(), restore);
            }
        }
    }

    private void rewriteEntity(Entity entity, boolean restore) {
        if (entity instanceof Player) {
            return;
        }
        if (entity instanceof Item item) {
            replaceItem(item.getItemStack(), restore, item::setItemStack);
            return;
        }
        if (entity instanceof ItemFrame frame) {
            replaceItem(frame.getItem(), restore, frame::setItem);
            return;
        }
        if (entity instanceof LivingEntity living && living.getEquipment() != null) {
            for (EquipmentSlot slot : List.of(
                    EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HEAD,
                    EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                ItemStack current = living.getEquipment().getItem(slot);
                ItemStack updated = restore ? plugin.items().withPlugin(current) : plugin.items().withoutPlugin(current);
                if (updated != current) {
                    living.getEquipment().setItem(slot, updated);
                }
            }
        }
        if (entity instanceof InventoryHolder holder && holder.getInventory() != null
                && !(holder.getInventory() instanceof PlayerInventory)) {
            rewriteInventory(holder.getInventory(), restore);
        }
    }

    private void replaceItem(ItemStack current, boolean restore, Consumer<ItemStack> writer) {
        ItemStack updated = restore ? plugin.items().withPlugin(current) : plugin.items().withoutPlugin(current);
        if (updated != current) {
            writer.accept(updated);
        }
    }

    private void rewriteInventory(Inventory inventory, boolean restore) {
        if (inventory == null) {
            return;
        }
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack current = inventory.getItem(slot);
            ItemStack updated = restore ? plugin.items().withPlugin(current) : plugin.items().withoutPlugin(current);
            if (updated != current) {
                inventory.setItem(slot, updated);
            }
        }
    }
}
