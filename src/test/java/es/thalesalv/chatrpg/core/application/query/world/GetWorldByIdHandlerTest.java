package es.thalesalv.chatrpg.core.application.query.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.thalesalv.chatrpg.core.application.usecase.world.GetWorldByIdHandler;
import es.thalesalv.chatrpg.core.application.usecase.world.request.GetWorldById;
import es.thalesalv.chatrpg.core.application.usecase.world.result.GetWorldResult;
import es.thalesalv.chatrpg.core.domain.world.World;
import es.thalesalv.chatrpg.core.domain.world.WorldService;
import es.thalesalv.chatrpg.core.domain.world.WorldFixture;

@ExtendWith(MockitoExtension.class)
public class GetWorldByIdHandlerTest {

    @Mock
    private WorldService domainService;

    @InjectMocks
    private GetWorldByIdHandler handler;

    @Test
    public void errorWhenQueryIsNull() {

        // Given
        GetWorldById query = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void getWorldById() {

        // Given
        String requesterDiscordId = "586678721356875";
        String id = "HAUDHUAHD";
        World world = WorldFixture.privateWorld().id(id).build();
        GetWorldById query = GetWorldById.build(id, requesterDiscordId);

        when(domainService.getWorldById(any(GetWorldById.class))).thenReturn(world);

        // When
        GetWorldResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }
}
