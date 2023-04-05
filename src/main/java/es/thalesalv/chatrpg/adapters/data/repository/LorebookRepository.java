package es.thalesalv.chatrpg.adapters.data.repository;

import es.thalesalv.chatrpg.adapters.data.entity.LorebookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LorebookRepository extends JpaRepository<LorebookEntity, String> {
}