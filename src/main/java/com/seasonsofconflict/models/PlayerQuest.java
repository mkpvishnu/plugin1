package com.seasonsofconflict.models;

import java.util.UUID;

public class PlayerQuest {
    private final UUID playerId;
    private final QuestTemplate template;
    private int progress;
    private boolean completed;

    public PlayerQuest(UUID playerId, QuestTemplate template) {
        this.playerId = playerId;
        this.template = template;
        this.progress = 0;
        this.completed = false;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public QuestTemplate getTemplate() {
        return template;
    }

    public String getDescription() {
        return template.getDescription();
    }

    public String getProgressKey() {
        return template.getProgressKey();
    }

    public int getTargetAmount() {
        return template.getTargetAmount();
    }

    public int getRewardPoints() {
        return template.getRewardPoints();
    }

    public QuestCategory getCategory() {
        return template.getCategory();
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (this.progress >= template.getTargetAmount()) {
            this.completed = true;
        }
    }

    public void incrementProgress() {
        this.progress++;
        if (this.progress >= template.getTargetAmount()) {
            this.completed = true;
        }
    }

    public void incrementProgress(int amount) {
        this.progress += amount;
        if (this.progress >= template.getTargetAmount()) {
            this.completed = true;
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getProgressPercentage() {
        return (int) ((double) progress / template.getTargetAmount() * 100);
    }

    public String getProgressString() {
        return progress + "/" + template.getTargetAmount();
    }
}
