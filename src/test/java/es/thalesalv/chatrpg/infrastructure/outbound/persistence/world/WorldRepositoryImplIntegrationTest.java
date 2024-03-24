package es.thalesalv.chatrpg.infrastructure.outbound.persistence.world;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import es.thalesalv.chatrpg.AbstractIntegrationTest;
import es.thalesalv.chatrpg.core.application.query.world.GetWorldResult;
import es.thalesalv.chatrpg.core.application.query.world.SearchWorldsResult;
import es.thalesalv.chatrpg.core.application.query.world.SearchWorldsWithReadAccess;
import es.thalesalv.chatrpg.core.application.query.world.SearchWorldsWithWriteAccess;
import es.thalesalv.chatrpg.core.domain.world.World;
import es.thalesalv.chatrpg.core.domain.world.WorldFixture;
import es.thalesalv.chatrpg.core.domain.world.WorldRepository;

public class WorldRepositoryImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorldRepository repository;

    @Autowired
    private WorldJpaRepository jpaRepository;

    @BeforeEach
    public void before() {
        jpaRepository.deleteAllInBatch();
    }

    @Test
    public void createWorld() {

        // Given
        World world = WorldFixture.privateWorld()
                .id(null)
                .build();

        // When
        World createdWorld = repository.save(world);

        // Then
        assertThat(createdWorld).isNotNull();

        assertThat(createdWorld.getCreationDate()).isNotNull();
        assertThat(createdWorld.getLastUpdateDate()).isNotNull();

        assertThat(createdWorld.getName()).isEqualTo(world.getName());
        assertThat(createdWorld.getVisibility()).isEqualTo(world.getVisibility());
        assertThat(createdWorld.getWriterUsers()).hasSameElementsAs(world.getWriterUsers());
        assertThat(createdWorld.getReaderUsers()).hasSameElementsAs(world.getReaderUsers());
    }

    @Test
    public void retrieveWorldById() {

        // Given
        String ownerDiscordId = "586678721356875";
        World world = repository.save(WorldFixture.privateWorld()
                .id(null)
                .build());

        // When
        Optional<World> retrievedWorldOptional = repository.findById(world.getId(), ownerDiscordId);

        // Then
        assertThat(retrievedWorldOptional).isNotNull().isNotEmpty();

        World retrievedWorld = retrievedWorldOptional.get();
        assertThat(retrievedWorld.getId()).isEqualTo(world.getId());
    }

    @Test
    public void emptyResultWhenUserCantSeeAsset() {

        // Given
        String requesterDiscordId = "123456";
        World world = repository.save(WorldFixture.privateWorld()
                .id(null)
                .build());

        // When
        Optional<World> retrievedWorldOptional = repository.findById(world.getId(), requesterDiscordId);

        // Then
        assertThat(retrievedWorldOptional).isNotNull().isEmpty();
    }

    @Test
    public void emptyResultWhenAssetDoesntExist() {

        // Given
        String worldId = "WRLDID";
        String requesterDiscordId = "123456";

        // When
        Optional<World> retrievedWorldOptional = repository.findById(worldId, requesterDiscordId);

        // Then
        assertThat(retrievedWorldOptional).isNotNull().isEmpty();
    }

    @Test
    public void deleteWorld() {

        // Given
        String ownerDiscordId = "586678721356875";
        World world = repository.save(WorldFixture.privateWorld()
                .id(null)
                .build());

        // When
        repository.deleteById(world.getId());

        // Then
        assertThat(repository.findById(world.getId(), ownerDiscordId)).isNotNull().isEmpty();
    }

    @Test
    public void returnAllWorldsWhenSearchingWithoutParameters() {

        // Given
        String ownerDiscordId = "586678721356875";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .ownerDiscordId(ownerDiscordId)
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .ownerDiscordId("580485734")
                .usersAllowedToRead(Collections.singletonList(ownerDiscordId))
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .ownerDiscordId("580485734")
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchWorldsWithReadAccess query = SearchWorldsWithReadAccess.builder().build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void returnOnlyWorldsWithReadAccessWhenSearchingWithoutParametersAsc() {

        // Given
        String ownerDiscordId = "586678721356875";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchWorldsWithReadAccess query = SearchWorldsWithReadAccess.builder().build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt3516k.getName());
        assertThat(worlds.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void returnAllWorldsWhenSearchingWithoutParametersDesc() {

        // Given
        String ownerDiscordId = "586678721356875";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchWorldsWithReadAccess query = SearchWorldsWithReadAccess.builder()
                .direction("DESC")
                .build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt3516k.getName());
        assertThat(worlds.get(2).getName()).isEqualTo(gpt4128k.getName());
    }

    @Test
    public void searchWorldOrderByNameAsc() {

        // Given
        String ownerDiscordId = "586678721356875";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchWorldsWithReadAccess query = SearchWorldsWithReadAccess.builder()
                .sortByField("name")
                .page(1)
                .items(10)
                .build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt3516k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4128k.getName());
        assertThat(worlds.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchWorldOrderByNameDesc() {

        // Given
        String ownerDiscordId = "586678721356875";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchWorldsWithReadAccess query = SearchWorldsWithReadAccess.builder()
                .sortByField("name")
                .direction("DESC")
                .build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(3);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4128k.getName());
        assertThat(worlds.get(2).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void searchWorldFilterByName() {

        // Given
        String ownerDiscordId = "586678721356875";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchWorldsWithReadAccess query = SearchWorldsWithReadAccess.builder()
                .name("Number 2")
                .build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void returnAllWorldsWhenSearchingWithoutParametersShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .ownerDiscordId(ownerDiscordId)
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .ownerDiscordId("580485734")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .ownerDiscordId("580485734")
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchWorldsWithWriteAccess query = SearchWorldsWithWriteAccess.builder().build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void returnOnlyWorldsWithWriteAccessWhenSearchingWithoutParametersAsc() {

        // Given
        String ownerDiscordId = "586678721358363";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .ownerDiscordId(ownerDiscordId)
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchWorldsWithWriteAccess query = SearchWorldsWithWriteAccess.builder().build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void returnAllWorldsWhenSearchingWithoutParametersDescShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .ownerDiscordId(ownerDiscordId)
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchWorldsWithWriteAccess query = SearchWorldsWithWriteAccess.builder()
                .direction("DESC")
                .build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void searchWorldOrderByNameAscShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .ownerDiscordId(ownerDiscordId)
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchWorldsWithWriteAccess query = SearchWorldsWithWriteAccess.builder()
                .sortByField("name")
                .page(1)
                .items(10)
                .build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt3516k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt4128k.getName());
    }

    @Test
    public void searchWorldOrderByNameDescShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .ownerDiscordId(ownerDiscordId)
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchWorldsWithWriteAccess query = SearchWorldsWithWriteAccess.builder()
                .sortByField("name")
                .direction("DESC")
                .build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(worlds.get(1).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void searchWorldFilterByNameShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchWorldsWithWriteAccess query = SearchWorldsWithWriteAccess.builder()
                .name("Number 2")
                .build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetWorldResult> worlds = result.getResults();
        assertThat(worlds.get(0).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void emptyResultWhenSearchingForWorldWithWriteAccessIfUserHasNoAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        WorldEntity gpt4128k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 1")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .build();

        WorldEntity gpt3516k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 2")
                .build();

        WorldEntity gpt354k = WorldEntityFixture.privateWorld()
                .id(null)
                .name("Number 3")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchWorldsWithWriteAccess query = SearchWorldsWithWriteAccess.builder()
                .name("Number 2")
                .build();

        // When
        SearchWorldsResult result = repository.searchWorldsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isEmpty();
    }
}