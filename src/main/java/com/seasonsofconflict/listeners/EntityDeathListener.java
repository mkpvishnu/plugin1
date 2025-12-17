package com.seasonsofconflict.listeners;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.BonusType;
import com.seasonsofconflict.models.PlayerData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {

    private final SeasonsOfConflict plugin;

    public EntityDeathListener(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        if (event.getEntity().getKiller() == null) return;

        Player killer = event.getEntity().getKiller();
        PlayerData data = plugin.getGameManager().getPlayerData(killer);

        if (data.getDailyMobKills() >= 100) return;

        data.incrementDailyMobKills();

        String mobType = event.getEntity().getType().name().toLowerCase();
        plugin.getQuestManager().updateQuestProgress(killer, mobType + "_kills", 1);
        plugin.getQuestManager().updateQuestProgress(killer, "mob_kills", 1);

        double xpBonus = plugin.getTerritoryManager().getTerritoryBonus(killer, BonusType.XP);
        if (xpBonus > 1.0) {
            event.setDroppedExp((int) (event.getDroppedExp() * xpBonus));
        }
    }
}
