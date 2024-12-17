package me.moirai.discordbot.infrastructure.outbound.persistence.world;

import static me.moirai.discordbot.infrastructure.outbound.persistence.SearchPredicates.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;
import me.moirai.discordbot.core.application.usecase.world.request.SearchWorldLorebookEntries;
import me.moirai.discordbot.core.application.usecase.world.result.SearchWorldLorebookEntriesResult;
import me.moirai.discordbot.core.domain.world.WorldLorebookEntry;
import me.moirai.discordbot.core.domain.world.WorldLorebookEntryRepository;
import me.moirai.discordbot.infrastructure.outbound.persistence.mapper.WorldLorebookPersistenceMapper;

@Repository
public class WorldLorebookEntryRepositoryImpl implements WorldLorebookEntryRepository {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_ITEMS = 10;

    private static final String NAME = "name";
    private static final String DEFAULT_SORT_BY_FIELD = NAME;

    private final WorldLorebookEntryJpaRepository jpaRepository;
    private final WorldLorebookPersistenceMapper mapper;

    public WorldLorebookEntryRepositoryImpl(WorldLorebookEntryJpaRepository jpaRepository,
            WorldLorebookPersistenceMapper mapper) {

        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public WorldLorebookEntry save(WorldLorebookEntry world) {

        WorldLorebookEntryEntity entity = mapper.mapToEntity(world);

        return mapper.mapFromEntity(jpaRepository.save(entity));
    }

    @Override
    public Optional<WorldLorebookEntry> findById(String lorebookEntryId) {

        return jpaRepository.findById(lorebookEntryId)
                .map(mapper::mapFromEntity);
    }

    @Override
    public List<WorldLorebookEntry> findAllByRegex(String valueToSearch, String worldId) {

        return jpaRepository.findAllByNameRegex(valueToSearch, worldId)
                .stream()
                .map(mapper::mapFromEntity)
                .toList();
    }

    @Override
    public List<WorldLorebookEntry> findAllByWorldId(String worldId) {

        return jpaRepository.findAllByWorldId(worldId)
                .stream()
                .map(mapper::mapFromEntity)
                .toList();
    }

    @Override
    public Optional<WorldLorebookEntry> findByPlayerDiscordId(String playerDiscordId, String worldId) {

        return jpaRepository.findByPlayerDiscordId(playerDiscordId, worldId)
                .map(mapper::mapFromEntity);
    }

    @Override
    public void deleteById(String id) {

        jpaRepository.deleteById(id);
    }

    @Override
    public SearchWorldLorebookEntriesResult search(SearchWorldLorebookEntries query) {

        int page = query.getPage() == null ? DEFAULT_PAGE : query.getPage() - 1;
        int items = query.getItems() == null ? DEFAULT_ITEMS : query.getItems();
        String sortByField = isBlank(query.getSortByField()) ? DEFAULT_SORT_BY_FIELD : query.getSortByField();
        Direction direction = isBlank(query.getDirection()) ? ASC : Direction.fromString(query.getDirection());

        PageRequest pageRequest = PageRequest.of(page, items, Sort.by(direction, sortByField));
        Specification<WorldLorebookEntryEntity> filters = buildSearchQuery(query);
        Page<WorldLorebookEntryEntity> pagedResult = jpaRepository.findAll(filters, pageRequest);

        return mapper.mapToResult(pagedResult);
    }

    private Specification<WorldLorebookEntryEntity> buildSearchQuery(SearchWorldLorebookEntries query) {

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("worldId"), query.getWorldId()));

            if (StringUtils.isNotBlank(query.getName())) {
                predicates.add(contains(cb, root, NAME, query.getName()));
            }

            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
