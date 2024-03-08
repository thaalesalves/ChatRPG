package es.thalesalv.chatrpg.core.application.query.world;

import org.springframework.stereotype.Service;

import es.thalesalv.chatrpg.common.usecases.UseCaseHandler;
import es.thalesalv.chatrpg.core.domain.world.WorldRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchWorldsHandler extends UseCaseHandler<SearchWorlds, SearchWorldsResult> {

    private final WorldRepository repository;

    @Override
    public SearchWorldsResult execute(SearchWorlds query) {

        return repository.searchWorlds(query);
    }
}
