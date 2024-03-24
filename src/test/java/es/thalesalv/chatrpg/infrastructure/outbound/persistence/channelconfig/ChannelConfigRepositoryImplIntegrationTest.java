package es.thalesalv.chatrpg.infrastructure.outbound.persistence.channelconfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import es.thalesalv.chatrpg.AbstractIntegrationTest;
import es.thalesalv.chatrpg.core.application.query.channelconfig.GetChannelConfigResult;
import es.thalesalv.chatrpg.core.application.query.channelconfig.SearchChannelConfigsResult;
import es.thalesalv.chatrpg.core.application.query.channelconfig.SearchChannelConfigsWithReadAccess;
import es.thalesalv.chatrpg.core.application.query.channelconfig.SearchChannelConfigsWithWriteAccess;
import es.thalesalv.chatrpg.core.domain.channelconfig.ChannelConfig;
import es.thalesalv.chatrpg.core.domain.channelconfig.ChannelConfigFixture;
import es.thalesalv.chatrpg.core.domain.channelconfig.ChannelConfigRepository;

public class ChannelConfigRepositoryImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ChannelConfigRepository repository;

    @Autowired
    private ChannelConfigJpaRepository jpaRepository;

    @BeforeEach
    public void before() {
        jpaRepository.deleteAllInBatch();
    }

    @Test
    public void createChannelConfig() {

        // Given
        ChannelConfig channelConfig = ChannelConfigFixture.sample()
                .id(null)
                .build();

        // When
        ChannelConfig createdChannelConfig = repository.save(channelConfig);

        // Then
        assertThat(createdChannelConfig).isNotNull();

        assertThat(createdChannelConfig.getCreationDate()).isNotNull();
        assertThat(createdChannelConfig.getLastUpdateDate()).isNotNull();

        assertThat(createdChannelConfig.getModelConfiguration().getAiModel().getInternalModelName())
                .isEqualTo((channelConfig.getModelConfiguration().getAiModel().getInternalModelName()));

        assertThat(createdChannelConfig.getModelConfiguration().getFrequencyPenalty())
                .isEqualTo((channelConfig.getModelConfiguration().getFrequencyPenalty()));

        assertThat(createdChannelConfig.getModelConfiguration().getPresencePenalty())
                .isEqualTo((channelConfig.getModelConfiguration().getPresencePenalty()));

        assertThat(createdChannelConfig.getModelConfiguration().getTemperature())
                .isEqualTo((channelConfig.getModelConfiguration().getTemperature()));

        assertThat(createdChannelConfig.getModelConfiguration().getLogitBias())
                .isEqualTo((channelConfig.getModelConfiguration().getLogitBias()));

        assertThat(createdChannelConfig.getModelConfiguration().getMaxTokenLimit())
                .isEqualTo((channelConfig.getModelConfiguration().getMaxTokenLimit()));

        assertThat(createdChannelConfig.getModelConfiguration().getMessageHistorySize())
                .isEqualTo((channelConfig.getModelConfiguration().getMessageHistorySize()));

        assertThat(createdChannelConfig.getModelConfiguration().getStopSequences())
                .isEqualTo((channelConfig.getModelConfiguration().getStopSequences()));

    }

    @Test
    public void retrieveChannelConfigById() {

        // Given
        String ownerDiscordId = "586678721356875";
        ChannelConfig channelConfig = repository.save(ChannelConfigFixture.sample()
                .id(null)
                .build());

        // When
        Optional<ChannelConfig> retrievedChannelConfigOptional = repository.findById(channelConfig.getId(),
                ownerDiscordId);

        // Then
        assertThat(retrievedChannelConfigOptional).isNotNull().isNotEmpty();

        ChannelConfig retrievedChannelConfig = retrievedChannelConfigOptional.get();
        assertThat(retrievedChannelConfig.getId()).isEqualTo(channelConfig.getId());
    }

    @Test
    public void emptyResultWhenUserCantSeeAsset() {

        // Given
        String requesterDiscordId = "123456";
        ChannelConfig channelConfig = repository.save(ChannelConfigFixture.sample()
                .id(null)
                .build());

        // When
        Optional<ChannelConfig> retrievedChannelConfigOptional = repository.findById(channelConfig.getId(),
                requesterDiscordId);

        // Then
        assertThat(retrievedChannelConfigOptional).isNotNull().isEmpty();
    }

    @Test
    public void emptyResultWhenAssetDoesntExist() {

        // Given
        String channelConfigId = "WRLDID";
        String requesterDiscordId = "123456";

        // When
        Optional<ChannelConfig> retrievedChannelConfigOptional = repository.findById(channelConfigId,
                requesterDiscordId);

        // Then
        assertThat(retrievedChannelConfigOptional).isNotNull().isEmpty();
    }

    @Test
    public void deleteChannelConfig() {

        // Given
        String ownerDiscordId = "586678721356875";
        ChannelConfig channelConfig = repository.save(ChannelConfigFixture.sample()
                .id(null)
                .build());

        // When
        repository.deleteById(channelConfig.getId());

        // Then
        assertThat(repository.findById(channelConfig.getId(), ownerDiscordId)).isNotNull().isEmpty();
    }

    @Test
    public void returnAllChannelConfigsWhenSearchingWithoutParameters() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .ownerDiscordId(ownerDiscordId)
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .ownerDiscordId("580485734")
                .usersAllowedToRead(Collections.singletonList(ownerDiscordId))
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .ownerDiscordId("580485734")
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder().build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void returnAllChannelConfigsWhenSearchingWithoutParametersAsc() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder().build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt3516k.getName());
        assertThat(channelConfigs.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void returnAllChannelConfigsWhenSearchingWithoutParametersDesc() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder()
                .direction("DESC")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt3516k.getName());
        assertThat(channelConfigs.get(2).getName()).isEqualTo(gpt4128k.getName());
    }

    @Test
    public void searchChannelConfigOrderByNameAsc() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder()
                .sortByField("name")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt3516k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt4128k.getName());
        assertThat(channelConfigs.get(2).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchChannelConfigOrderByNameDesc() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder()
                .sortByField("name")
                .direction("DESC")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt4128k.getName());
        assertThat(channelConfigs.get(2).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void searchChannelConfigOrderByAiModelAsc() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder()
                .sortByField("modelConfiguration.aiModel")
                .direction("ASC")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt3516k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt354k.getName());
        assertThat(channelConfigs.get(2).getName()).isEqualTo(gpt4128k.getName());
    }

    @Test
    public void searchChannelConfigOrderByAiModelDesc() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder()
                .sortByField("modelConfiguration.aiModel")
                .direction("DESC")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt354k.getName());
        assertThat(channelConfigs.get(2).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void searchChannelConfigOrderByModerationAsc() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .moderation("STRICT")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .moderation("PERMISSIVE")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .moderation("PERMISSIVE")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder()
                .sortByField("moderation")
                .direction("ASC")
                .page(1)
                .items(10)
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt3516k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt354k.getName());
        assertThat(channelConfigs.get(2).getName()).isEqualTo(gpt4128k.getName());
    }

    @Test
    public void searchChannelConfigOrderByModerationDesc() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder()
                .sortByField("modelConfiguration.aiModel")
                .direction("DESC")
                .page(1)
                .items(10)
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty();

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt354k.getName());
        assertThat(channelConfigs.get(2).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void searchChannelConfigFilterByAiModel() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder()
                .aiModel("gpt35-4k")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchChannelConfigFilterByName() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder()
                .name("Number 2")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void searchChannelConfigFilterByModeration() {

        // Given
        String ownerDiscordId = "586678721356875";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .moderation("STRICT")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .moderation("PERMISSIVE")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .moderation("PERMISSIVE")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithReadAccess query = SearchChannelConfigsWithReadAccess.builder()
                .moderation("PERMISSIVE")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithReadAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt3516k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void returnAllChannelConfigsWhenSearchingWithoutParametersShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .ownerDiscordId(ownerDiscordId)
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .ownerDiscordId("580485734")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .ownerDiscordId("580485734")
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder().build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void returnAllChannelConfigsWhenSearchingWithoutParametersAscShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .ownerDiscordId(ownerDiscordId)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder().build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void returnAllChannelConfigsWhenSearchingWithoutParametersDescShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .ownerDiscordId(ownerDiscordId)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.save(gpt4128k);
        jpaRepository.save(gpt3516k);
        jpaRepository.save(gpt354k);

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder()
                .direction("DESC")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt3516k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt4128k.getName());
    }

    @Test
    public void searchChannelConfigOrderByNameAscShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .ownerDiscordId(ownerDiscordId)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder()
                .sortByField("name")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt3516k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt4128k.getName());
    }

    @Test
    public void searchChannelConfigOrderByNameDescShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .ownerDiscordId(ownerDiscordId)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder()
                .sortByField("name")
                .direction("DESC")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt4128k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void searchChannelConfigOrderByAiModelAscShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .ownerDiscordId(ownerDiscordId)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder()
                .sortByField("modelConfiguration.aiModel")
                .direction("ASC")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt3516k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchChannelConfigOrderByAiModelDescShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .ownerDiscordId(ownerDiscordId)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder()
                .sortByField("modelConfiguration.aiModel")
                .direction("DESC")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void searchChannelConfigOrderByModerationAscShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .moderation("STRICT")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .moderation("PERMISSIVE")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .ownerDiscordId(ownerDiscordId)
                .moderation("PERMISSIVE")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder()
                .sortByField("moderation")
                .direction("ASC")
                .page(1)
                .items(10)
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt3516k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchChannelConfigOrderByModerationDescShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .ownerDiscordId(ownerDiscordId)
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder()
                .sortByField("modelConfiguration.aiModel")
                .direction("DESC")
                .page(1)
                .items(10)
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt354k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void searchChannelConfigFilterByAiModelShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder()
                .aiModel("gpt35-4k")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt354k.getName());
    }

    @Test
    public void searchChannelConfigFilterByNameShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k()
                        .aiModel("gpt35-4k")
                        .build())
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder()
                .name("Number 2")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(1);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt3516k.getName());
    }

    @Test
    public void searchChannelConfigFilterByModerationShowOnlyWithWriteAccess() {

        // Given
        String ownerDiscordId = "586678721358363";

        ChannelConfigEntity gpt4128k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 1")
                .moderation("STRICT")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt4128k().build())
                .build();

        ChannelConfigEntity gpt3516k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 2")
                .ownerDiscordId(ownerDiscordId)
                .moderation("PERMISSIVE")
                .modelConfiguration(ModelConfigurationEntityFixture.gpt3516k().build())
                .build();

        ChannelConfigEntity gpt354k = ChannelConfigEntityFixture.sample()
                .id(null)
                .name("Number 3")
                .usersAllowedToWrite(Collections.singletonList(ownerDiscordId))
                .moderation("PERMISSIVE")
                .build();

        jpaRepository.saveAll(Lists.list(gpt4128k, gpt3516k, gpt354k));

        SearchChannelConfigsWithWriteAccess query = SearchChannelConfigsWithWriteAccess.builder()
                .moderation("PERMISSIVE")
                .build();

        // When
        SearchChannelConfigsResult result = repository.searchChannelConfigsWithWriteAccess(query, ownerDiscordId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isNotEmpty().hasSize(2);

        List<GetChannelConfigResult> channelConfigs = result.getResults();
        assertThat(channelConfigs.get(0).getName()).isEqualTo(gpt3516k.getName());
        assertThat(channelConfigs.get(1).getName()).isEqualTo(gpt354k.getName());
    }
}