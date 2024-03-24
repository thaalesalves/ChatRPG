package es.thalesalv.chatrpg.core.application.command.channelconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.thalesalv.chatrpg.core.domain.channelconfig.ChannelConfig;
import es.thalesalv.chatrpg.core.domain.channelconfig.ChannelConfigDomainServiceImpl;
import es.thalesalv.chatrpg.core.domain.channelconfig.ChannelConfigFixture;

@ExtendWith(MockitoExtension.class)
public class UpdateChannelConfigHandlerTest {

    @Mock
    private ChannelConfigDomainServiceImpl service;

    @InjectMocks
    private UpdateChannelConfigHandler handler;

    @Test
    public void updateChannelConfig() {

        // Given
        String id = "CHCONFID";

        UpdateChannelConfig command = UpdateChannelConfig.builder()
                .id(id)
                .name("Name")
                .worldId("WRLDID")
                .personaId("PRSNID")
                .moderation("STRICT")
                .visibility("PRIVATE")
                .creatorDiscordId("CRTID")
                .build();

        ChannelConfig expectedUpdatedChannelConfig = ChannelConfigFixture.sample().build();

        when(service.update(any(UpdateChannelConfig.class)))
                .thenReturn(expectedUpdatedChannelConfig);

        // When
        UpdateChannelConfigResult result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLastUpdatedDateTime()).isEqualTo(expectedUpdatedChannelConfig.getLastUpdateDate());
    }
}