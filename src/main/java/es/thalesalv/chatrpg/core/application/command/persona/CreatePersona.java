package es.thalesalv.chatrpg.core.application.command.persona;

import java.util.List;

import es.thalesalv.chatrpg.common.usecases.UseCase;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
public final class CreatePersona extends UseCase<CreatePersonaResult> {

    private final String name;
    private final String personality;
    private final String creatorDiscordId;
    private final String nudgeRole;
    private final String nudgeContent;
    private final String bumpRole;
    private final String bumpContent;
    private final String visibility;
    private final Integer bumpFrequency;
    private final List<String> writerUsers;
    private final List<String> readerUsers;
}