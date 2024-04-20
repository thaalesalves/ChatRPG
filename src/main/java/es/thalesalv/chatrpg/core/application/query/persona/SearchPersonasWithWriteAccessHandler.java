package es.thalesalv.chatrpg.core.application.query.persona;

import es.thalesalv.chatrpg.common.annotation.UseCaseHandler;
import es.thalesalv.chatrpg.common.usecases.AbstractUseCaseHandler;
import es.thalesalv.chatrpg.core.domain.persona.PersonaRepository;
import lombok.RequiredArgsConstructor;

@UseCaseHandler
@RequiredArgsConstructor
public class SearchPersonasWithWriteAccessHandler extends AbstractUseCaseHandler<SearchPersonasWithWriteAccess, SearchPersonasResult> {

    private final PersonaRepository repository;

    @Override
    public SearchPersonasResult execute(SearchPersonasWithWriteAccess query) {

        return repository.searchPersonasWithWriteAccess(query);
    }
}
