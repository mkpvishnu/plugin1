package com.seasonsofconflict.managers;

import com.seasonsofconflict.SeasonsOfConflict;
import com.seasonsofconflict.models.*;
import com.seasonsofconflict.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class QuestManager {

    private final SeasonsOfConflict plugin;

    public QuestManager(SeasonsOfConflict plugin) {
        this.plugin = plugin;
    }

    /**
     * Assign 3 random daily quests to a player, weighted by season
     */
    public void assignDailyQuests(Player player) {
        PlayerData data = plugin.getGameManager().getPlayerData(player);

        // Clear existing daily quests
        data.clearDailyQuests();

        // Get current season
        Season currentSeason = plugin.getGameManager().getGameState().getCurrentSeason();

        // Get all daily quest templates (exclude weekly quests)
        List<QuestTemplate> dailyQuests = Arrays.stream(QuestTemplate.values())
            .filter(q -> q.getCategory() != QuestCategory.WEEKLY)
            .collect(Collectors.toList());

        // Create weighted quest pool based on season
        List<QuestTemplate> weightedPool = createWeightedQuestPool(dailyQuests, currentSeason);

        // Randomly select 3 unique quests
        Set<QuestTemplate> selectedQuests = new HashSet<>();
        Random random = new Random();

        while (selectedQuests.size() < 3 && !weightedPool.isEmpty()) {
            int index = random.nextInt(weightedPool.size());
            QuestTemplate quest = weightedPool.get(index);

            if (!selectedQuests.contains(quest)) {
                selectedQuests.add(quest);
            }
        }

        // Assign selected quests to player
        for (QuestTemplate template : selectedQuests) {
            PlayerQuest quest = new PlayerQuest(player.getUniqueId(), template);
            data.addQuest(quest);
        }

        // Save player data
        plugin.getGameManager().savePlayerData(player.getUniqueId());

        // Notify player
        MessageUtils.sendSuccess(player, "Daily quests assigned!");
        MessageUtils.sendMessage(player, "&7Type &e/quest &7to view your quests.");
    }

    /**
     * Create weighted quest pool based on season
     */
    private List<QuestTemplate> createWeightedQuestPool(List<QuestTemplate> quests, Season season) {
        List<QuestTemplate> weightedPool = new ArrayList<>();

        for (QuestTemplate quest : quests) {
            double weight = getSeasonalWeight(quest.getCategory(), season);
            int count = (int) Math.ceil(weight * 10); // Convert weight to count

            for (int i = 0; i < count; i++) {
                weightedPool.add(quest);
            }
        }

        return weightedPool;
    }

    /**
     * Get seasonal weight for quest category
     */
    private double getSeasonalWeight(QuestCategory category, Season season) {
        return switch (season) {
            case SPRING -> switch (category) {
                case GATHERING -> 2.0;
                case EXPLORATION -> 1.5;
                default -> 1.0;
            };
            case SUMMER -> switch (category) {
                case COMBAT -> 2.0;
                case GATHERING -> 1.5;
                default -> 1.0;
            };
            case FALL -> switch (category) {
                case GATHERING -> 1.5;
                default -> 1.0;
            };
            case WINTER -> switch (category) {
                case SURVIVAL -> 2.0;
                default -> 1.0;
            };
        };
    }

    /**
     * Update quest progress for a player
     */
    public void updateQuestProgress(Player player, String progressKey, int amount) {
        PlayerData data = plugin.getGameManager().getPlayerData(player);

        boolean questCompleted = false;

        for (PlayerQuest quest : data.getActiveQuests()) {
            if (quest.isCompleted()) continue;

            if (quest.getProgressKey().equals(progressKey)) {
                quest.incrementProgress(amount);

                // Check if quest was just completed
                if (quest.isCompleted() && !questCompleted) {
                    completeQuest(player, quest);
                    questCompleted = true;
                }
            }
        }

        // Save player data if any progress was made
        if (questCompleted) {
            plugin.getGameManager().savePlayerData(player.getUniqueId());
        }
    }

    /**
     * Complete a quest - award points to team and mark as completed
     */
    public void completeQuest(Player player, PlayerQuest quest) {
        PlayerData data = plugin.getGameManager().getPlayerData(player);

        // Mark quest as completed
        quest.setCompleted(true);

        // Award points to team
        int teamId = data.getTeamId();
        if (teamId > 0) {
            TeamData team = plugin.getTeamManager().getTeam(teamId);
            if (team != null) {
                int points = quest.getRewardPoints();
                team.addPoints(points);
                plugin.getTeamManager().saveTeam(team);

                // Notify player
                MessageUtils.sendSuccess(player, "Quest completed: " + quest.getDescription());
                MessageUtils.sendMessage(player, "&aAwarded &e" + points + " points &ato " + team.getColoredName());

                // Broadcast to team if it's a high-value quest
                if (points >= 30) {
                    broadcastToTeam(team, player.getName() + " &acompleted a quest! &e+" + points + " points");
                }
            }
        }

        // Increment daily quests completed
        data.incrementDailyQuestsCompleted();

        // Save player data
        plugin.getGameManager().savePlayerData(player.getUniqueId());

        plugin.getLogger().info(player.getName() + " completed quest: " + quest.getDescription());
    }

    /**
     * Get active quests for a player
     */
    public List<PlayerQuest> getActiveQuests(Player player) {
        PlayerData data = plugin.getGameManager().getPlayerData(player);
        return data.getActiveQuests();
    }

    /**
     * Assign weekly team quests
     */
    public void assignWeeklyQuests() {
        // Get all weekly quest templates
        List<QuestTemplate> weeklyQuests = Arrays.stream(QuestTemplate.values())
            .filter(q -> q.getCategory() == QuestCategory.WEEKLY)
            .collect(Collectors.toList());

        if (weeklyQuests.isEmpty()) {
            plugin.getLogger().warning("No weekly quests available");
            return;
        }

        Random random = new Random();

        // Assign a random weekly quest to each team
        for (TeamData team : plugin.getTeamManager().getAllTeams()) {
            if (team.isEliminated()) continue;

            QuestTemplate weeklyQuest = weeklyQuests.get(random.nextInt(weeklyQuests.size()));

            team.setWeeklyQuestId(weeklyQuest.ordinal());
            team.setWeeklyQuestProgress(0);

            plugin.getTeamManager().saveTeam(team);

            // Notify team
            broadcastToTeam(team, "&e&lWeekly Quest: &7" + weeklyQuest.getDescription());
        }

        MessageUtils.broadcast("&eWeekly team quests have been assigned!");
        plugin.getLogger().info("Assigned weekly quests to all teams");
    }

    /**
     * Broadcast a message to all members of a team
     */
    private void broadcastToTeam(TeamData team, String message) {
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getGameManager().getPlayerData(player);
            if (data.getTeamId() == team.getTeamId()) {
                MessageUtils.sendMessage(player, message);
            }
        }
    }

    /**
     * Update weekly quest progress for a team
     */
    public void updateWeeklyQuestProgress(TeamData team, String progressKey, int amount) {
        int questId = team.getWeeklyQuestId();

        if (questId < 0 || questId >= QuestTemplate.values().length) {
            return;
        }

        QuestTemplate template = QuestTemplate.values()[questId];

        if (template.getProgressKey().equals(progressKey)) {
            team.setWeeklyQuestProgress(team.getWeeklyQuestProgress() + amount);

            // Check if quest is completed
            if (team.getWeeklyQuestProgress() >= template.getTargetAmount()) {
                completeWeeklyQuest(team, template);
            }

            plugin.getTeamManager().saveTeam(team);
        }
    }

    /**
     * Complete a weekly quest for a team
     */
    private void completeWeeklyQuest(TeamData team, QuestTemplate template) {
        int points = template.getRewardPoints();
        team.addPoints(points);

        // Broadcast completion
        MessageUtils.broadcast(team.getColoredName() + " &acompleted their weekly quest! &e+" + points + " points");

        // Reset weekly quest
        team.setWeeklyQuestId(-1);
        team.setWeeklyQuestProgress(0);

        plugin.getTeamManager().saveTeam(team);

        plugin.getLogger().info(team.getName() + " completed weekly quest: " + template.getDescription());
    }

    /**
     * Get weekly quest for a team
     */
    public QuestTemplate getWeeklyQuest(TeamData team) {
        int questId = team.getWeeklyQuestId();

        if (questId < 0 || questId >= QuestTemplate.values().length) {
            return null;
        }

        return QuestTemplate.values()[questId];
    }

    /**
     * Check if a player has a specific quest
     */
    public boolean hasQuest(Player player, QuestTemplate template) {
        PlayerData data = plugin.getGameManager().getPlayerData(player);

        for (PlayerQuest quest : data.getActiveQuests()) {
            if (quest.getTemplate() == template) {
                return true;
            }
        }

        return false;
    }
}
