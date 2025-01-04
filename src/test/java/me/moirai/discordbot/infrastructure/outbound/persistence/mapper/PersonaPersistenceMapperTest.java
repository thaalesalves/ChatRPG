package me.moirai.discordbot.infrastructure.outbound.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import me.moirai.discordbot.core.application.usecase.persona.result.GetPersonaResult;
import me.moirai.discordbot.core.application.usecase.persona.result.SearchPersonasResult;
import me.moirai.discordbot.core.domain.persona.Persona;
import me.moirai.discordbot.core.domain.persona.PersonaFixture;

@ExtendWith(MockitoExtension.class)
public class PersonaPersistenceMapperTest {

    @InjectMocks
    private PersonaPersistenceMapper mapper;

    @Test
    public void mapPersonaDomain_whenGetOperation_thenMapToGetResult() {

        // Given
        Persona persona = PersonaFixture.privatePersona().build();

        // When
        GetPersonaResult result = mapper.mapToResult(persona);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(persona.getId());
        assertThat(result.getName()).isEqualTo(persona.getName());
        assertThat(result.getPersonality()).isEqualTo(persona.getPersonality());
        assertThat(result.getVisibility()).isEqualTo(persona.getVisibility().name());
        assertThat(result.getUsersAllowedToRead()).hasSameElementsAs(persona.getUsersAllowedToRead());
        assertThat(result.getUsersAllowedToWrite()).hasSameElementsAs(persona.getUsersAllowedToWrite());
        assertThat(result.getCreationDate()).isEqualTo(persona.getCreationDate());
        assertThat(result.getLastUpdateDate()).isEqualTo(persona.getLastUpdateDate());
        assertThat(result.getOwnerDiscordId()).isEqualTo(persona.getOwnerDiscordId());
    }

    @Test
    public void mapPersonaDomain_whenSearchPersona_thenMapToServer() {

        // Given
        List<Persona> personas = IntStream.range(0, 20)
                .mapToObj(op -> PersonaFixture.privatePersona()
                        .id(String.valueOf(op + 1))
                        .build())
                .toList();

        Pageable pageable = Pageable.ofSize(10);
        Page<Persona> page = new PageImpl<>(personas, pageable, 20);

        // When
        SearchPersonasResult result = mapper.mapToResult(page);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getItems()).isEqualTo(20);
    }
}
