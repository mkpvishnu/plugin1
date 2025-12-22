package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.*;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages skill tree GUI creation and display
 */
public class SkillTreeGUI {

    private final SeasonsOfConflict plugin;
    private final Map<UUID, SkillTree> pendingResets;

    public SkillTreeGUI(SeasonsOfConflict plugin) {
        this.plugin = plugin;
        this.pendingResets = new HashMap<>();
    }

    /**
     * Open the main skill tree menu (4 tree selection)
     */
    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "" + ChatColor.BOLD + "🌟 SKILL TREES 🌟");

        PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player.getUniqueId());
        XPManager.PlayerXPData xpData = plugin.getXPManager().getPlayerXP(player.getUniqueId());

        // Decorative border (gray stained glass pane)
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(i + 45, border);
        }

        // Info display (top center)
        ItemStack infoItem = createItem(Material.EXPERIENCE_BOTTLE,
            ChatColor.YELLOW + "" + ChatColor.BOLD + "Your Progress",
            ChatColor.GRAY + "Available Points: " + ChatColor.GREEN + skills.getSkillPointsAvailable(),
            ChatColor.GRAY + "Spent Points: " + ChatColor.RED + skills.getSkillPointsSpent(),
            ChatColor.GRAY + "Ultimates: " + ChatColor.GOLD + skills.getUltimateCount() + "/2",
            "",
            ChatColor.GRAY + "XP Progress: " + ChatColor.AQUA +
                String.format("%.1f%%", plugin.getXPManager().getProgressPercent(player.getUniqueId())),
            ChatColor.GRAY + "Total XP: " + ChatColor.WHITE + xpData.getTotalXP()
        );
        inv.setItem(4, infoItem);

        // Combat Tree (slot 20)
        inv.setItem(20, createTreeIcon(SkillTree.COMBAT, skills));

        // Gathering Tree (slot 24)
        inv.setItem(24, createTreeIcon(SkillTree.GATHERING, skills));

        // Survival Tree (slot 29)
        inv.setItem(29, createTreeIcon(SkillTree.SURVIVAL, skills));

        // Teamwork Tree (slot 33)
        inv.setItem(33, createTreeIcon(SkillTree.TEAMWORK, skills));

        // Close button (slot 49)
        ItemStack closeButton = createItem(Material.BARRIER,
            ChatColor.RED + "" + ChatColor.BOLD + "❌ CLOSE",
            ChatColor.GRAY + "Click to close menu"
        );
        inv.setItem(49, closeButton);

        player.openInventory(inv);
    }

    /**
     * Open individual tree view
     */
    public void openTreeView(Player player, SkillTree tree) {
        Inventory inv = Bukkit.createInventory(null, 54,
            ChatColor.GOLD + "" + ChatColor.BOLD + tree.getFormattedName() + " TREE");

        PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player.getUniqueId());

        // Decorative border - tree-themed colors
        ItemStack border = createTreeThemedBorder(tree);
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(i + 45, border);
        }

        // Tree info (top center)
        int pointsSpent = skills.getPointsSpentInTree(tree);
        boolean hasUltimate = skills.hasSkill(tree, SkillTier.ULTIMATE);
        ItemStack treeInfo = createItem(Material.BOOK,
            ChatColor.YELLOW + "" + ChatColor.BOLD + tree.getDisplayName() + " Tree",
            "",
            ChatColor.GRAY + tree.getDescription(),
            "",
            ChatColor.GOLD + "Points Spent: " + ChatColor.WHITE + pointsSpent,
            ChatColor.GOLD + "Ultimate: " + (hasUltimate ? ChatColor.GREEN + "✓ Unlocked" : ChatColor.RED + "✗ Locked"),
            "",
            ChatColor.GRAY + "Available Points: " + ChatColor.GREEN + skills.getSkillPointsAvailable()
        );
        inv.setItem(4, treeInfo);

        // Tier 1 skills (slots 10, 12, 14)
        Skill[] tier1Skills = Skill.getSkillsInTier(tree, SkillTier.TIER_1);
        if (tier1Skills.length > 0) placeSkillsInSlots(inv, tier1Skills, new int[]{10, 12, 14}, skills);

        // Tier 2 skills (slots 19, 21, 23)
        Skill[] tier2Skills = Skill.getSkillsInTier(tree, SkillTier.TIER_2);
        if (tier2Skills.length > 0) placeSkillsInSlots(inv, tier2Skills, new int[]{19, 21, 23}, skills);

        // Tier 3 skills (slots 28, 30, 32)
        Skill[] tier3Skills = Skill.getSkillsInTier(tree, SkillTier.TIER_3);
        if (tier3Skills.length > 0) placeSkillsInSlots(inv, tier3Skills, new int[]{28, 30, 32}, skills);

        // Tier 4 skills (slots 37, 39, 41)
        Skill[] tier4Skills = Skill.getSkillsInTier(tree, SkillTier.TIER_4);
        if (tier4Skills.length > 0) placeSkillsInSlots(inv, tier4Skills, new int[]{37, 39, 41}, skills);

        // Ultimate skill (slot 49)
        Skill[] ultimateSkills = Skill.getSkillsInTier(tree, SkillTier.ULTIMATE);
        if (ultimateSkills.length > 0) {
            inv.setItem(49, createSkillItem(ultimateSkills[0], skills));
        }

        // Back button (slot 45)
        ItemStack backButton = createItem(Material.ARROW,
            ChatColor.YELLOW + "" + ChatColor.BOLD + "⬅ BACK",
            ChatColor.GRAY + "Return to main menu"
        );
        inv.setItem(45, backButton);

        // Reset button (slot 53)
        int resetCost = plugin.getConfig().getInt("skills.reset_costs.single_tree", 500);
        ItemStack resetButton = createItem(Material.TNT,
            ChatColor.RED + "" + ChatColor.BOLD + "🔄 RESET TREE",
            ChatColor.GRAY + "Cost: " + ChatColor.GOLD + resetCost + " team points",
            "",
            ChatColor.YELLOW + "⚠ This will refund all skill points",
            ChatColor.YELLOW + "⚠ from this tree!",
            "",
            ChatColor.GRAY + "Click to reset (requires confirmation)"
        );
        inv.setItem(53, resetButton);

        player.openInventory(inv);
    }

    /**
     * Place skills in specific slots
     */
    private void placeSkillsInSlots(Inventory inv, Skill[] skills, int[] slots, PlayerSkills playerSkills) {
        for (int i = 0; i < skills.length && i < slots.length; i++) {
            inv.setItem(slots[i], createSkillItem(skills[i], playerSkills));
        }
    }

    /**
     * Create tree selection icon for main menu
     */
    private ItemStack createTreeIcon(SkillTree tree, PlayerSkills skills) {
        Material iconMaterial = switch (tree) {
            case COMBAT -> Material.DIAMOND_SWORD;
            case GATHERING -> Material.DIAMOND_PICKAXE;
            case SURVIVAL -> Material.SHIELD;
            case TEAMWORK -> Material.BEACON;
        };

        int pointsSpent = skills.getPointsSpentInTree(tree);
        boolean hasUltimate = skills.hasSkill(tree, SkillTier.ULTIMATE);

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + tree.getDescription());
        lore.add("");
        lore.add(ChatColor.GOLD + "Points Spent: " + ChatColor.WHITE + pointsSpent);

        if (hasUltimate) {
            lore.add(ChatColor.GOLD + "★ " + ChatColor.YELLOW + "Ultimate Unlocked!");
        }

        lore.add("");
        lore.add(ChatColor.GREEN + "Click to view tree");

        return createItem(iconMaterial, tree.getFormattedName(), lore.toArray(new String[0]));
    }

    /**
     * Create skill item for display in tree view
     */
    private ItemStack createSkillItem(Skill skill, PlayerSkills skills) {
        // Determine if THIS SPECIFIC skill is unlocked (not just any skill in the tier)
        String unlockedSkillName = skills.getSkill(skill.getTree(), skill.getTier());
        boolean unlocked = (unlockedSkillName != null && unlockedSkillName.equals(skill.getInternalName()));
        boolean tierHasSkill = skills.hasSkill(skill.getTree(), skill.getTier());
        boolean canAfford = skills.canAfford(skill.getTier());
        boolean hasPrereq = skills.hasPrerequisite(skill.getTree(), skill.getTier());
        boolean canUnlock = canAfford && hasPrereq && !tierHasSkill;

        // Choose material and color based on state
        Material material;
        ChatColor nameColor;

        if (unlocked) {
            material = getSkillMaterial(skill, true, false);
            nameColor = ChatColor.GREEN;
        } else if (tierHasSkill) {
            // Another skill in this tier is already unlocked
            material = Material.BLACK_STAINED_GLASS_PANE;  // Black = tier occupied
            nameColor = ChatColor.DARK_GRAY;
        } else if (canUnlock) {
            material = getSkillMaterial(skill, false, true);
            nameColor = ChatColor.YELLOW;
        } else if (!hasPrereq) {
            material = Material.GRAY_STAINED_GLASS_PANE;  // Gray = tier locked
            nameColor = ChatColor.GRAY;
        } else {
            material = Material.RED_STAINED_GLASS_PANE;  // Red = can't afford
            nameColor = ChatColor.RED;
        }

        // Create lore
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.AQUA + "" + ChatColor.BOLD + skill.getDisplayName());
        lore.add("");

        // Description (word wrap at ~40 chars)
        String[] descWords = skill.getDescription().split(" ");
        StringBuilder line = new StringBuilder(ChatColor.GRAY.toString());
        for (String word : descWords) {
            if (line.length() + word.length() > 40) {
                lore.add(line.toString());
                line = new StringBuilder(ChatColor.GRAY + word + " ");
            } else {
                line.append(word).append(" ");
            }
        }
        if (line.length() > 0) {
            lore.add(line.toString().trim());
        }

        lore.add("");
        lore.add(ChatColor.GOLD + "Cost: " + ChatColor.WHITE + skill.getTier().getCost() + " points");
        lore.add(ChatColor.GOLD + "Tier: " + ChatColor.WHITE + skill.getTier().getDisplayName());
        lore.add(ChatColor.GOLD + "Type: " + ChatColor.WHITE + skill.getType().getDisplayName());

        lore.add("");

        // Status and instructions
        if (unlocked) {
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "✓ UNLOCKED");
        } else if (tierHasSkill) {
            // Show which skill is unlocked in this tier
            lore.add(ChatColor.DARK_GRAY + "⚠ Tier occupied by: " + ChatColor.GRAY + unlockedSkillName);
        } else if (canUnlock) {
            lore.add(ChatColor.YELLOW + "" + ChatColor.BOLD + "Click to unlock!");
        } else if (!hasPrereq) {
            SkillTier prevTier = skill.getTier().getPreviousTier();
            if (prevTier != null) {
                lore.add(ChatColor.RED + "⚠ Requires: " + prevTier.getDisplayName() + " tier");
            }
        } else if (!canAfford) {
            int needed = skill.getTier().getCost() - skills.getSkillPointsAvailable();
            lore.add(ChatColor.RED + "⚠ Need " + needed + " more skill points");
        }

        // Ultimate warning
        if (skill.getTier().isUltimate() && !unlocked) {
            if (skills.hasMaxUltimates()) {
                lore.add(ChatColor.RED + "⚠ Already have 2 ultimates!");
            } else {
                lore.add(ChatColor.GOLD + "★ Ultimate Ability");
            }
        }

        return createItem(material, nameColor + skill.getDisplayName(), lore.toArray(new String[0]));
    }

    /**
     * Helper to create ItemStack with name and lore
     */
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && lore.length > 0) {
                meta.setLore(List.of(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Create tree-themed border based on skill tree
     */
    private ItemStack createTreeThemedBorder(SkillTree tree) {
        Material borderMaterial = switch (tree) {
            case COMBAT -> Material.RED_STAINED_GLASS_PANE;
            case GATHERING -> Material.GREEN_STAINED_GLASS_PANE;
            case SURVIVAL -> Material.ORANGE_STAINED_GLASS_PANE;
            case TEAMWORK -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        };
        return createItem(borderMaterial, " ", null);
    }

    /**
     * Get thematic material for skill based on tree and tier
     */
    private Material getSkillMaterial(Skill skill, boolean unlocked, boolean canUnlock) {
        // For unlocked or affordable skills, use thematic items
        if (unlocked || canUnlock) {
            return switch (skill.getTree()) {
                case COMBAT -> switch (skill.getTier()) {
                    case TIER_1 -> Material.WOODEN_SWORD;
                    case TIER_2 -> Material.STONE_SWORD;
                    case TIER_3 -> Material.IRON_SWORD;
                    case TIER_4 -> Material.DIAMOND_SWORD;
                    case ULTIMATE -> Material.NETHERITE_SWORD;
                };
                case GATHERING -> switch (skill.getTier()) {
                    case TIER_1 -> Material.WOODEN_PICKAXE;
                    case TIER_2 -> Material.STONE_PICKAXE;
                    case TIER_3 -> Material.IRON_PICKAXE;
                    case TIER_4 -> Material.DIAMOND_PICKAXE;
                    case ULTIMATE -> Material.NETHERITE_PICKAXE;
                };
                case SURVIVAL -> switch (skill.getTier()) {
                    case TIER_1 -> Material.LEATHER_CHESTPLATE;
                    case TIER_2 -> Material.CHAINMAIL_CHESTPLATE;
                    case TIER_3 -> Material.IRON_CHESTPLATE;
                    case TIER_4 -> Material.DIAMOND_CHESTPLATE;
                    case ULTIMATE -> Material.NETHERITE_CHESTPLATE;
                };
                case TEAMWORK -> switch (skill.getTier()) {
                    case TIER_1 -> Material.WHITE_BANNER;
                    case TIER_2 -> Material.YELLOW_BANNER;
                    case TIER_3 -> Material.LIGHT_BLUE_BANNER;
                    case TIER_4 -> Material.CYAN_BANNER;
                    case ULTIMATE -> Material.LIGHT_BLUE_BANNER;
                };
            };
        }

        // For locked skills, use colored glass panes
        return Material.GRAY_STAINED_GLASS_PANE;
    }

    /**
     * Handle click in skill tree GUI
     * @return true if GUI should stay open, false if it should close
     */
    public boolean handleClick(Player player, Inventory inv, int slot, ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR) return true;

        // Check if this is reset confirmation GUI (has LIME_WOOL at slot 11 and RED_WOOL at slot 15)
        if (inv.getSize() == 27 && inv.getItem(11) != null && inv.getItem(11).getType() == Material.LIME_WOOL &&
            inv.getItem(15) != null && inv.getItem(15).getType() == Material.RED_WOOL) {
            return handleResetConfirmationClick(player, slot);
        }

        String invTitle = inv.getViewers().isEmpty() ? "" :
            ChatColor.stripColor(Bukkit.getServer().createInventory(null, inv.getSize()).getType().name());

        // Main menu clicks
        if (inv.getItem(4) != null && inv.getItem(4).getType() == Material.EXPERIENCE_BOTTLE) {
            return handleMainMenuClick(player, slot);
        }
        // Tree view clicks
        else {
            return handleTreeViewClick(player, inv, slot, clicked);
        }
    }

    /**
     * Handle clicks in main menu
     */
    private boolean handleMainMenuClick(Player player, int slot) {
        return switch (slot) {
            case 20 -> {
                openTreeView(player, SkillTree.COMBAT);
                yield true;
            }
            case 24 -> {
                openTreeView(player, SkillTree.GATHERING);
                yield true;
            }
            case 29 -> {
                openTreeView(player, SkillTree.SURVIVAL);
                yield true;
            }
            case 33 -> {
                openTreeView(player, SkillTree.TEAMWORK);
                yield true;
            }
            case 49 -> false; // Close button
            default -> true;
        };
    }

    /**
     * Handle clicks in tree view
     */
    private boolean handleTreeViewClick(Player player, Inventory inv, int slot, ItemStack clicked) {
        // Back button
        if (slot == 45) {
            openMainMenu(player);
            return true;
        }

        // Reset button
        if (slot == 53) {
            SkillTree tree = getTreeFromInventory(inv);
            if (tree != null) {
                openResetConfirmation(player, tree);
            } else {
                MessageUtils.sendMessage(player, "&cError: Could not determine skill tree!");
            }
            return true;
        }

        // Skill unlock attempt
        if (isSkillSlot(slot)) {
            return handleSkillUnlock(player, inv, slot, clicked);
        }

        return true;
    }

    /**
     * Check if slot contains a skill
     */
    private boolean isSkillSlot(int slot) {
        // Tier 1: 10, 12, 14
        // Tier 2: 19, 21, 23
        // Tier 3: 28, 30, 32
        // Tier 4: 37, 39, 41
        // Ultimate: 49
        return slot == 10 || slot == 12 || slot == 14 ||
               slot == 19 || slot == 21 || slot == 23 ||
               slot == 28 || slot == 30 || slot == 32 ||
               slot == 37 || slot == 39 || slot == 41 ||
               slot == 49;
    }

    /**
     * Handle skill unlock attempt
     */
    private boolean handleSkillUnlock(Player player, Inventory inv, int slot, ItemStack clicked) {
        // Extract skill name from item lore
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasLore()) return true;

        List<String> lore = clicked.getItemMeta().getLore();
        if (lore == null || lore.size() < 2) return true;

        // Skill name is in lore line 1 (after empty line 0)
        String skillNameLine = ChatColor.stripColor(lore.get(1));

        // Find skill by display name
        Skill skill = findSkillByDisplayName(skillNameLine);
        if (skill == null) return true;

        // Check if THIS SPECIFIC skill is already unlocked
        PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player.getUniqueId());
        String unlockedSkillName = skills.getSkill(skill.getTree(), skill.getTier());
        if (unlockedSkillName != null && unlockedSkillName.equals(skill.getInternalName())) {
            MessageUtils.sendMessage(player, "&cYou already have this skill unlocked!");
            return true;
        }

        // Check if tier is occupied by another skill
        if (skills.hasSkill(skill.getTree(), skill.getTier())) {
            MessageUtils.sendMessage(player, "&cThis tier is already occupied by: &e" + unlockedSkillName);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return true;
        }

        // Attempt unlock
        SkillManager.UnlockResult result = plugin.getSkillManager().unlockSkill(
            player.getUniqueId(), skill.getTree(), skill.getTier(), skill.getInternalName()
        );

        if (result.isSuccess()) {
            MessageUtils.sendMessage(player, "&a&l✓ " + result.getMessage());
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

            // Apply skill effects immediately
            applySkillEffectsImmediately(player, skill);

            // Refresh GUI
            openTreeView(player, skill.getTree());
        } else {
            MessageUtils.sendMessage(player, "&c&l✗ " + result.getMessage());
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }

        return true;
    }

    /**
     * Find skill by display name
     */
    private Skill findSkillByDisplayName(String displayName) {
        for (Skill skill : Skill.values()) {
            if (skill.getDisplayName().equals(displayName)) {
                return skill;
            }
        }
        return null;
    }

    /**
     * Get which tree is being viewed from inventory
     */
    private SkillTree getTreeFromInventory(Inventory inv) {
        ItemStack treeInfo = inv.getItem(4);
        if (treeInfo == null || !treeInfo.hasItemMeta()) return null;

        String displayName = ChatColor.stripColor(treeInfo.getItemMeta().getDisplayName());
        if (displayName.contains("Combat")) return SkillTree.COMBAT;
        if (displayName.contains("Gathering")) return SkillTree.GATHERING;
        if (displayName.contains("Survival")) return SkillTree.SURVIVAL;
        if (displayName.contains("Teamwork")) return SkillTree.TEAMWORK;

        return null;
    }

    /**
     * Open reset confirmation GUI
     */
    public void openResetConfirmation(Player player, SkillTree tree) {
        Inventory inv = Bukkit.createInventory(null, 27,
            ChatColor.RED + "" + ChatColor.BOLD + "⚠ CONFIRM RESET ⚠");

        PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player.getUniqueId());
        TeamData team = plugin.getTeamManager().getTeam(player);

        int resetCost = plugin.getConfig().getInt("skills.reset_costs.single_tree", 500);
        int pointsToRefund = skills.getPointsSpentInTree(tree);

        // Decorative border
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, border);
        }

        // Info display
        List<String> infoLore = new ArrayList<>();
        infoLore.add("");
        infoLore.add(ChatColor.GRAY + "Tree: " + ChatColor.WHITE + tree.getDisplayName());
        infoLore.add(ChatColor.GRAY + "Points to Refund: " + ChatColor.GREEN + pointsToRefund);
        infoLore.add(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + resetCost + " team points");
        infoLore.add("");
        if (team != null) {
            infoLore.add(ChatColor.GRAY + "Team Points: " + ChatColor.WHITE + team.getQuestPoints());
            if (team.getQuestPoints() < resetCost) {
                infoLore.add("");
                infoLore.add(ChatColor.RED + "❌ Not enough team points!");
            }
        } else {
            infoLore.add(ChatColor.RED + "❌ You are not on a team!");
        }

        ItemStack infoItem = createItem(Material.PAPER,
            ChatColor.YELLOW + "" + ChatColor.BOLD + "Reset " + tree.getDisplayName() + " Tree",
            infoLore.toArray(new String[0])
        );
        inv.setItem(13, infoItem);

        // Confirm button (slot 11)
        ItemStack confirmButton = createItem(Material.LIME_WOOL,
            ChatColor.GREEN + "" + ChatColor.BOLD + "✓ CONFIRM RESET",
            "",
            ChatColor.GRAY + "All skills in this tree will be reset",
            ChatColor.GRAY + "You will get " + ChatColor.GREEN + pointsToRefund + " skill points" + ChatColor.GRAY + " back",
            ChatColor.GRAY + "Cost: " + ChatColor.GOLD + resetCost + " team points",
            "",
            ChatColor.YELLOW + "Click to confirm"
        );
        inv.setItem(11, confirmButton);

        // Cancel button (slot 15)
        ItemStack cancelButton = createItem(Material.RED_WOOL,
            ChatColor.RED + "" + ChatColor.BOLD + "✗ CANCEL",
            "",
            ChatColor.GRAY + "Return to skill tree"
        );
        inv.setItem(15, cancelButton);

        // Store pending reset
        pendingResets.put(player.getUniqueId(), tree);

        player.openInventory(inv);
    }

    /**
     * Handle reset confirmation click
     */
    public boolean handleResetConfirmationClick(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();
        SkillTree tree = pendingResets.get(playerUUID);

        if (tree == null) {
            player.closeInventory();
            return true;
        }

        // Confirm button
        if (slot == 11) {
            int resetCost = plugin.getConfig().getInt("skills.reset_costs.single_tree", 500);
            boolean success = plugin.getSkillManager().resetTree(playerUUID, tree, resetCost);

            if (success) {
                MessageUtils.sendMessage(player, "&a&l✓ " + tree.getDisplayName() + " tree has been reset!");
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                pendingResets.remove(playerUUID);
                openTreeView(player, tree);
            } else {
                MessageUtils.sendMessage(player, "&c&l✗ Failed to reset tree! Not enough team points.");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.closeInventory();
                pendingResets.remove(playerUUID);
            }
            return true;
        }

        // Cancel button
        if (slot == 15) {
            pendingResets.remove(playerUUID);
            openTreeView(player, tree);
            return true;
        }

        return true;
    }

    /**
     * Apply skill effects immediately after unlocking
     */
    private void applySkillEffectsImmediately(Player player, Skill skill) {
        // Apply health bonuses immediately
        switch (skill.getInternalName()) {
            case "iron_skin":  // Combat: +2 hearts
                plugin.getSkillEffectManager().applyIronSkin(player);
                MessageUtils.sendMessage(player, "&a+2 hearts added!");
                break;
            case "hardy":  // Survival: +3 hearts
                plugin.getSkillEffectManager().applyHardy(player);
                MessageUtils.sendMessage(player, "&a+3 hearts added!");
                break;
            case "swift_strikes":  // Combat: +15% attack speed
                plugin.getSkillEffectManager().applySwiftStrikes(player);
                MessageUtils.sendMessage(player, "&a+15% attack speed!");
                break;
            case "titans_grip":  // Combat: +30% knockback resistance
                plugin.getSkillEffectManager().applyTitansGripKnockbackRes(player);
                MessageUtils.sendMessage(player, "&a+30% knockback resistance!");
                break;
        }
    }
}
