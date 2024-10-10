package me.moirai.discordbot.infrastructure.outbound.persistence.world;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.discordbot.AbstractIntegrationTest;
import me.moirai.discordbot.core.domain.world.World;
import me.moirai.discordbot.core.domain.world.WorldDomainRepository;
import me.moirai.discordbot.core.domain.world.WorldFixture;

public class WorldRepositoryImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorldDomainRepository repository;

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
        assertThat(createdWorld.getUsersAllowedToWrite()).hasSameElementsAs(world.getUsersAllowedToWrite());
        assertThat(createdWorld.getUsersAllowedToRead()).hasSameElementsAs(world.getUsersAllowedToRead());
    }

    @Test
    public void retrieveWorldById() {

        // Given
        World world = repository.save(WorldFixture.privateWorld()
                .id(null)
                .build());

        // When
        Optional<World> retrievedWorldOptional = repository.findById(world.getId());

        // Then
        assertThat(retrievedWorldOptional).isNotNull().isNotEmpty();

        World retrievedWorld = retrievedWorldOptional.get();
        assertThat(retrievedWorld.getId()).isEqualTo(world.getId());
    }

    @Test
    public void emptyResultWhenAssetDoesntExist() {

        // Given
        String worldId = "WRLDID";

        // When
        Optional<World> retrievedWorldOptional = repository.findById(worldId);

        // Then
        assertThat(retrievedWorldOptional).isNotNull().isEmpty();
    }

    @Test
    public void deleteWorld() {

        // Given
        World world = repository.save(WorldFixture.privateWorld()
                .id(null)
                .build());

        // When
        repository.deleteById(world.getId());

        // Then
        assertThat(repository.findById(world.getId())).isNotNull().isEmpty();
    }
}