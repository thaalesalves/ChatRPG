package es.thalesalv.chatrpg.core.domain.channelconfig;

import java.time.OffsetDateTime;

import es.thalesalv.chatrpg.core.domain.PermissionsFixture;
import es.thalesalv.chatrpg.core.domain.Visibility;

public class ChannelConfigFixture {

    public static ChannelConfig.Builder sample() {

        ChannelConfig.Builder builder = ChannelConfig.builder();
        builder.id("CHCONFID");
        builder.name("Name");
        builder.worldId("WRLDID");
        builder.personaId("PRSNID");
        builder.moderation(Moderation.STRICT);
        builder.visibility(Visibility.fromString("PRIVATE"));
        builder.modelConfiguration(ModelConfigurationFixture.gpt3516k().build());
        builder.permissions(PermissionsFixture.samplePermissions().build());
        builder.creatorDiscordId("CRTID");
        builder.creationDate(OffsetDateTime.now());
        builder.lastUpdateDate(OffsetDateTime.now());

        return builder;
    }
}