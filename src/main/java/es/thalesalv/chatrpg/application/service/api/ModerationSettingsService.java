package es.thalesalv.chatrpg.application.service.api;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import es.thalesalv.chatrpg.adapters.data.repository.ModerationSettingsRepository;
import es.thalesalv.chatrpg.application.mapper.chconfig.ModerationSettingsDTOToEntity;
import es.thalesalv.chatrpg.application.mapper.chconfig.ModerationSettingsEntityToDTO;
import es.thalesalv.chatrpg.domain.exception.ModerationSettingsNotFoundException;
import es.thalesalv.chatrpg.domain.model.chconf.ModerationSettings;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModerationSettingsService {

    private final ModerationSettingsDTOToEntity moderationSettingsDTOToEntity;
    private final ModerationSettingsEntityToDTO moderationSettingsEntityToDTO;

    private final ModerationSettingsRepository moderationSettingsRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ModerationSettingsService.class);

    public List<ModerationSettings> retrieveAllModerationSettings() {

        LOGGER.debug("Retrieving moderation settings data from request");
        return moderationSettingsRepository.findAll()
                .stream()
                .map(moderationSettingsEntityToDTO)
                .toList();
    }

    public ModerationSettings retrieveModerationSettingsById(final String moderationSettingsId) {

        LOGGER.debug("Retrieving moderation settings by ID data from request");
        return moderationSettingsRepository.findById(moderationSettingsId)
                .map(moderationSettingsEntityToDTO)
                .orElseThrow(ModerationSettingsNotFoundException::new);
    }

    public ModerationSettings saveModerationSettings(final ModerationSettings moderationSettings) {

        LOGGER.debug("Saving moderation settings data from request");
        return Optional.of(moderationSettingsDTOToEntity.apply(moderationSettings))
                .map(moderationSettingsRepository::save)
                .map(moderationSettingsEntityToDTO)
                .orElseThrow();
    }

    public ModerationSettings updateModerationSettings(final String moderationSettingsId,
            final ModerationSettings moderationSettings) {

        LOGGER.debug("Updating moderation settings data from request");
        return Optional.of(moderationSettingsDTOToEntity.apply(moderationSettings))
                .map(c -> {
                    c.setId(moderationSettingsId);
                    return moderationSettingsRepository.save(c);
                })
                .map(moderationSettingsEntityToDTO)
                .orElseThrow();
    }

    public void deleteModerationSettings(final String moderationSettingsId) {

        LOGGER.debug("Deleting moderation settings data from request");
        moderationSettingsRepository.deleteById(moderationSettingsId);
    }
}
