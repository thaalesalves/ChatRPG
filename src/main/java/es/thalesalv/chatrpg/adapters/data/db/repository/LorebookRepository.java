package es.thalesalv.chatrpg.adapters.data.db.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import es.thalesalv.chatrpg.adapters.data.db.entity.LorebookEntryEntity;

@Repository
public interface LorebookRepository extends CrudRepository<LorebookEntryEntity, String> {

    /**
     * Retrieves a character from the database by providing the player's Discord ID
     *
     * @param userId Player's Discord ID
     * @return Player's character profile
     */
    Optional<LorebookEntryEntity> findByPlayerDiscordId(String userId);

    /**
     * Retrieves all characters that match the list of names provided
     *
     * @param names List containing names to look up
     * @return Character profiles with those names
     */
    Set<LorebookEntryEntity> findByNameIn(Set<String> names);
}
