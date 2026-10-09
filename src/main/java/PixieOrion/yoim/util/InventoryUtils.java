package PixieOrion.yoim.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public final class InventoryUtils {
    private InventoryUtils() {}

    public static boolean isSword(Item item) {
        return item == Items.WOODEN_SWORD || item == Items.STONE_SWORD || item == Items.IRON_SWORD || item == Items.GOLDEN_SWORD || item == Items.DIAMOND_SWORD || item == Items.NETHERITE_SWORD;
    }

    public static int findBestSword(MinecraftClient client) {
        Item[] swords = {Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD};
        for (int i = swords.length - 1; i >= 0; i--) {
            for (int slot = 8; slot >= 0; slot--) {
                if (client.player.getInventory().getStack(slot).isOf(swords[i])) return slot;
            }
        }
        return -1;
    }
}
