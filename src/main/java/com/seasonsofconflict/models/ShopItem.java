package com.seasonsofconflict.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ShopItem {
    GOLDEN_APPLES("golden_apples", "Golden Apple x2", 150,
                  new String[]{"golden_apples", "apples", "gapples"}),
    IRON_ARMOR("iron_armor", "Iron Armor Set", 200,
               new String[]{"iron_armor", "armor", "iron_set"});

    private final String id;
    private final String displayName;
    private final int cost;
    private final String[] aliases;

    ShopItem(String id, String displayName, int cost, String[] aliases) {
        this.id = id;
        this.displayName = displayName;
        this.cost = cost;
        this.aliases = aliases;
    }

    /**
     * Parse a user input string to a ShopItem.
     * Case-insensitive matching against ID and aliases.
     *
     * @param input User input string
     * @return ShopItem if found, null otherwise
     */
    public static ShopItem fromString(String input) {
        if (input == null) return null;
        String lower = input.toLowerCase();

        for (ShopItem item : values()) {
            if (item.id.equals(lower)) return item;
            for (String alias : item.aliases) {
                if (alias.equals(lower)) return item;
            }
        }
        return null;
    }

    /**
     * Get the ItemStacks that should be given to the player.
     *
     * @return Array of ItemStacks for this shop item
     */
    public ItemStack[] getItems() {
        switch (this) {
            case GOLDEN_APPLES:
                return new ItemStack[] {
                    new ItemStack(Material.GOLDEN_APPLE, 2)
                };
            case IRON_ARMOR:
                return new ItemStack[] {
                    new ItemStack(Material.IRON_HELMET),
                    new ItemStack(Material.IRON_CHESTPLATE),
                    new ItemStack(Material.IRON_LEGGINGS),
                    new ItemStack(Material.IRON_BOOTS)
                };
            default:
                return new ItemStack[0];
        }
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCost() {
        return cost;
    }

    public String[] getAliases() {
        return aliases;
    }
}
