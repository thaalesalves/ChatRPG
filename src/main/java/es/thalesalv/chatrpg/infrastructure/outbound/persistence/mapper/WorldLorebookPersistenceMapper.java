package es.thalesalv.chatrpg.infrastructure.outbound.persistence.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import es.thalesalv.chatrpg.core.application.usecase.world.result.GetWorldLorebookEntryResult;
import es.thalesalv.chatrpg.core.application.usecase.world.result.SearchWorldLorebookEntriesResult;
import es.thalesalv.chatrpg.core.domain.world.WorldLorebookEntry;
import es.thalesalv.chatrpg.infrastructure.outbound.persistence.world.WorldLorebookEntryEntity;

@Component
public class WorldLorebookPersistenceMapper {

    public WorldLorebookEntryEntity mapToEntity(WorldLorebookEntry entry) {

        return WorldLorebookEntryEntity.builder()
                .id(entry.getId())
                .name(entry.getName())
                .description(entry.getDescription())
                .regex(entry.getRegex())
                .playerDiscordId(entry.getPlayerDiscordId())
                .isPlayerCharacter(entry.isPlayerCharacter())
                .worldId(entry.getWorldId())
                .creatorDiscordId(entry.getCreatorDiscordId())
                .creationDate(entry.getCreationDate())
                .lastUpdateDate(entry.getLastUpdateDate())
                .build();
    }

    public WorldLorebookEntry mapFromEntity(WorldLorebookEntryEntity entry) {

        return WorldLorebookEntry.builder()
                .id(entry.getId())
                .name(entry.getName())
                .description(entry.getDescription())
                .regex(entry.getRegex())
                .playerDiscordId(entry.getPlayerDiscordId())
                .isPlayerCharacter(entry.isPlayerCharacter())
                .worldId(entry.getWorldId())
                .creatorDiscordId(entry.getCreatorDiscordId())
                .creationDate(entry.getCreationDate())
                .lastUpdateDate(entry.getLastUpdateDate())
                .build();
    }

    public GetWorldLorebookEntryResult mapToResult(WorldLorebookEntryEntity entry) {

        return GetWorldLorebookEntryResult.builder()
                .id(entry.getId())
                .name(entry.getName())
                .description(entry.getDescription())
                .regex(entry.getRegex())
                .playerDiscordId(entry.getPlayerDiscordId())
                .isPlayerCharacter(entry.isPlayerCharacter())
                .creationDate(entry.getCreationDate())
                .lastUpdateDate(entry.getLastUpdateDate())
                .build();
    }

    public SearchWorldLorebookEntriesResult mapToResult(Page<WorldLorebookEntryEntity> pagedResult) {
        return SearchWorldLorebookEntriesResult.builder()
                .results(pagedResult.getContent()
                        .stream()
                        .map(this::mapToResult)
                        .toList())
                .page(pagedResult.getNumber() + 1)
                .items(pagedResult.getNumberOfElements())
                .totalItems(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .build();
    }
}
