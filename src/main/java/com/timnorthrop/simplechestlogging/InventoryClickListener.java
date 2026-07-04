package com.timnorthrop.simplechestlogging;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.HashMap;

public class InventoryClickListener implements Listener {
    private final SimpleChestLogging plugin;
    private final Map<Inventory, BukkitTask> pendingSnapshots = new HashMap<>();

    public InventoryClickListener(SimpleChestLogging plugin) {
        this.plugin = plugin;
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().isAir() || item.getAmount() <= 0 || item.isEmpty();
    }

    private static boolean isNotContainer(InventoryType t) {
        return t != InventoryType.BARREL && t != InventoryType.CHEST
                && t != InventoryType.DISPENSER && t != InventoryType.DROPPER
                && t != InventoryType.ENDER_CHEST && t != InventoryType.HOPPER
                && t != InventoryType.SHULKER_BOX;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || plugin.isOff()) {
            return;
        }

        Inventory clicked = event.getClickedInventory();
        if (clicked == null) {
            return;
        }

        Inventory chest = event.getView().getTopInventory();
        if (chest.getSize() <= 0 || isNotContainer(chest.getType())) {
            return;
        }

        if (pendingSnapshots.containsKey(chest)) {
            return;
        }

        ItemStack[] before = new ItemStack[chest.getSize()];

        for (int i = 0; i < chest.getSize(); i++) {
            ItemStack item = chest.getItem(i);
            before[i] = item == null ? null : item.clone();
        }

        BukkitTask task = Bukkit.getScheduler().runTask(plugin, () -> {
            pendingSnapshots.remove(chest);

            ItemStack[] after = chest.getContents();

            diffAndLog(before, after, chest, player);
        });

        pendingSnapshots.put(chest, task);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || plugin.isOff()) {
            return;
        }

        Inventory chest = event.getView().getTopInventory();
        if (chest.getSize() <= 0 || isNotContainer(chest.getType())) {
            return;
        }

        Map<Integer, ItemStack> newItems = event.getNewItems();
        if (newItems.isEmpty()) {
            return;
        }

        ItemStack[] before = new ItemStack[chest.getSize()];
        ItemStack[] after = new ItemStack[chest.getSize()];
        newItems.forEach((k, v) -> {
            if (k < chest.getSize()) {
                after[k] = v;
            }
        });

        diffAndLog(before, after, chest, player);
    }

    private void diffAndLog(ItemStack[] before, ItemStack[] after, Inventory chest, Player player) {
        Map<ItemType, Integer> diffs = new HashMap<>();

        String chestType = chest.getType().toString();
        Location chestLoc = chest.getLocation();
        if (chestLoc == null) {
            return;
        }
        String chestLocStr = "(" + chestLoc.getBlockX()
                + ", " + chestLoc.getBlockY()
                + ", " + chestLoc.getBlockZ() + ")";

        for (int i = 0; i < before.length; i++) {
            ItemStack bef = before[i];
            ItemStack aft = after[i];

            if (isEmpty(bef) && isEmpty(aft)) {
                continue;
            }

            if (isEmpty(bef)) {
                addToDiff(aft.getType().asItemType(), aft.getAmount(), diffs);
                continue;
            }
            if (isEmpty(aft)) {
                addToDiff(bef.getType().asItemType(), (-bef.getAmount()), diffs);
                continue;
            }

            if (bef.getType().equals(aft.getType()) && bef.getAmount() == aft.getAmount()) {
                continue;
            }

            addToDiff(aft.getType().asItemType(), aft.getAmount(), diffs);
            addToDiff(bef.getType().asItemType(), (-bef.getAmount()), diffs);

        }

        diffs.forEach((k, v) -> {
            if (v > 0) {
                plugin.getLogger().info(player.getName() + " put " + v + "x " + k.getKey().getKey()
                        + " in " + chestType + " at " + chestLocStr);
            } else if (v < 0) {
                plugin.getLogger().info(player.getName() + " took " + (-v) + "x " + k.getKey().getKey()
                        + " from " + chestType + " at " + chestLocStr);
            }
        });
    }

    private void addToDiff(ItemType material, int amount, Map<ItemType, Integer> diffs) {
        diffs.merge(material, amount, Integer::sum);
    }
}