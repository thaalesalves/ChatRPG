package me.moirai.discordbot.infrastructure.inbound.api.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelConfigResponse {

    private String id;
    private String name;
    private String worldId;
    private String personaId;
    private String discordChannelId;
    private String visibility;
    private String aiModel;
    private String moderation;
    private String gameMode;
    private Integer maxTokenLimit;
    private Integer messageHistorySize;
    private Double temperature;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private List<String> stopSequences;
    private Map<String, Double> logitBias;
    private String ownerDiscordId;
    private List<String> usersAllowedToRead;
    private List<String> usersAllowedToWrite;
    private OffsetDateTime creationDate;
    private OffsetDateTime lastUpdateDate;

    public ChannelConfigResponse() {
    }

    private ChannelConfigResponse(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.worldId = builder.worldId;
        this.personaId = builder.personaId;
        this.discordChannelId = builder.discordChannelId;
        this.visibility = builder.visibility;
        this.aiModel = builder.aiModel;
        this.moderation = builder.moderation;
        this.maxTokenLimit = builder.maxTokenLimit;
        this.messageHistorySize = builder.messageHistorySize;
        this.temperature = builder.temperature;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.presencePenalty = builder.presencePenalty;
        this.stopSequences = builder.stopSequences;
        this.logitBias = builder.logitBias;
        this.ownerDiscordId = builder.ownerDiscordId;
        this.usersAllowedToRead = builder.usersAllowedToRead;
        this.usersAllowedToWrite = builder.usersAllowedToWrite;
        this.creationDate = builder.creationDate;
        this.lastUpdateDate = builder.lastUpdateDate;
        this.gameMode = builder.gameMode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWorldId() {
        return worldId;
    }

    public String getPersonaId() {
        return personaId;
    }

    public String getDiscordChannelId() {
        return discordChannelId;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getAiModel() {
        return aiModel;
    }

    public String getModeration() {
        return moderation;
    }

    public String getGameMode() {
        return gameMode;
    }

    public Integer getMaxTokenLimit() {
        return maxTokenLimit;
    }

    public Integer getMessageHistorySize() {
        return messageHistorySize;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public List<String> getStopSequences() {
        return stopSequences;
    }

    public Map<String, Double> getLogitBias() {
        return logitBias;
    }

    public String getOwnerDiscordId() {
        return ownerDiscordId;
    }

    public List<String> getUsersAllowedToRead() {
        return usersAllowedToRead;
    }

    public List<String> getUsersAllowedToWrite() {
        return usersAllowedToWrite;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public OffsetDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public static final class Builder {

        private String id;
        private String name;
        private String worldId;
        private String personaId;
        private String discordChannelId;
        private String visibility;
        private String aiModel;
        private String moderation;
        private String gameMode;
        private Integer maxTokenLimit;
        private Integer messageHistorySize;
        private Double temperature;
        private Double frequencyPenalty;
        private Double presencePenalty;
        private List<String> stopSequences;
        private Map<String, Double> logitBias;
        private String ownerDiscordId;
        private List<String> usersAllowedToRead;
        private List<String> usersAllowedToWrite;
        private OffsetDateTime creationDate;
        private OffsetDateTime lastUpdateDate;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder worldId(String worldId) {
            this.worldId = worldId;
            return this;
        }

        public Builder personaId(String personaId) {
            this.personaId = personaId;
            return this;
        }

        public Builder discordChannelId(String discordChannelId) {
            this.discordChannelId = discordChannelId;
            return this;
        }

        public Builder visibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder aiModel(String aiModel) {
            this.aiModel = aiModel;
            return this;
        }

        public Builder moderation(String moderation) {
            this.moderation = moderation;
            return this;
        }

        public Builder gameMode(String gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder maxTokenLimit(Integer maxTokenLimit) {
            this.maxTokenLimit = maxTokenLimit;
            return this;
        }

        public Builder messageHistorySize(Integer messageHistorySize) {
            this.messageHistorySize = messageHistorySize;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder stopSequences(List<String> stopSequences) {
            this.stopSequences = stopSequences;
            return this;
        }

        public Builder logitBias(Map<String, Double> logitBias) {
            this.logitBias = logitBias;
            return this;
        }

        public Builder ownerDiscordId(String ownerDiscordId) {
            this.ownerDiscordId = ownerDiscordId;
            return this;
        }

        public Builder usersAllowedToRead(List<String> usersAllowedToRead) {
            this.usersAllowedToRead = usersAllowedToRead;
            return this;
        }

        public Builder usersAllowedToWrite(List<String> usersAllowedToWrite) {
            this.usersAllowedToWrite = usersAllowedToWrite;
            return this;
        }

        public Builder creationDate(OffsetDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Builder lastUpdateDate(OffsetDateTime lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public ChannelConfigResponse build() {
            return new ChannelConfigResponse(this);
        }
    }
}