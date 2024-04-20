package es.thalesalv.chatrpg.core.application.query.world;

import es.thalesalv.chatrpg.common.annotation.UseCaseHandler;
import es.thalesalv.chatrpg.common.usecases.AbstractUseCaseHandler;
import es.thalesalv.chatrpg.core.domain.world.WorldLorebookEntryRepository;
import lombok.RequiredArgsConstructor;

@UseCaseHandler
@RequiredArgsConstructor
public class SearchWorldLorebookEntriesHandler extends AbstractUseCaseHandler<SearchWorldLorebookEntries, SearchWorldLorebookEntriesResult> {

    private final WorldLorebookEntryRepository repository;

    @Override
    public SearchWorldLorebookEntriesResult execute(SearchWorldLorebookEntries query) {

        return repository.searchWorldLorebookEntriesByWorldId(query);
    }
}
