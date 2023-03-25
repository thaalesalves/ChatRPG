package es.thalesalv.chatrpg.application.service.commands.lorebook;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.thalesalv.chatrpg.adapters.data.db.entity.LorebookEntity;
import es.thalesalv.chatrpg.adapters.data.db.entity.LorebookEntryEntity;
import es.thalesalv.chatrpg.adapters.data.db.entity.LorebookRegexEntity;
import es.thalesalv.chatrpg.adapters.data.db.repository.ChannelRepository;
import es.thalesalv.chatrpg.adapters.data.db.repository.LorebookEntryRepository;
import es.thalesalv.chatrpg.adapters.data.db.repository.LorebookRegexRepository;
import es.thalesalv.chatrpg.application.ContextDatastore;
import es.thalesalv.chatrpg.application.mapper.chconfig.ChannelEntityToDTO;
import es.thalesalv.chatrpg.application.mapper.worlds.LorebookDTOToEntity;
import es.thalesalv.chatrpg.application.mapper.worlds.LorebookEntryEntityToDTO;
import es.thalesalv.chatrpg.application.service.ModerationService;
import es.thalesalv.chatrpg.application.service.commands.DiscordCommand;
import es.thalesalv.chatrpg.domain.exception.LorebookEntryNotFoundException;
import es.thalesalv.chatrpg.domain.exception.MissingRequiredSlashCommandOptionException;
import es.thalesalv.chatrpg.domain.model.openai.dto.EventData;
import es.thalesalv.chatrpg.domain.model.openai.dto.LorebookEntry;
import es.thalesalv.chatrpg.domain.model.openai.dto.World;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateLorebookCommandService implements DiscordCommand {

    private final LorebookEntryEntityToDTO lorebookEntryToDTO;
    private final ModerationService moderationService;
    private final ContextDatastore contextDatastore;
    private final ObjectMapper objectMapper;
    private final LorebookEntryRepository lorebookRepository;
    private final LorebookRegexRepository lorebookRegexRepository;
    private final ChannelRepository channelRepository;
    private final ChannelEntityToDTO channelEntityMapper;
    private final LorebookDTOToEntity lorebookDTOToEntity;

    private static final int DELETE_EPHEMERAL_20_SECONDS = 20;

    private static final String ERROR_UPDATE = "There was an error parsing your request. Please try again.";
    private static final String ENTRY_UPDATED = "Lore entry with name {0} was updated.\n```json\n{1}```";
    private static final String MISSING_ID_MESSAGE = "The UUID of the entry is required for an update action. Please try again with the entry id.";
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateLorebookCommandService.class);

    @Override
    public void handle(final SlashCommandInteractionEvent event) {

        try {
            LOGGER.debug("Received slash command for lore entry update");
            channelRepository.findByChannelId(event.getChannel().getId()).stream()
                    .findFirst()
                    .map(channelEntityMapper::apply)
                    .ifPresent(channel -> {
                        final String entryId = event.getOption("lorebook-entry-id").getAsString();
                        contextDatastore.setEventData(EventData.builder()
                                .lorebookEntryId(entryId).botChannelDefinitions(channel).build());

                        final var entry = lorebookRegexRepository.findByLorebookEntry(LorebookEntryEntity.builder().id(entryId).build())
                                .orElseThrow(LorebookEntryNotFoundException::new);

                        final Modal modalEntry = buildEntryUpdateModal(entry);
                        event.replyModal(modalEntry).queue();
                        return;
                    });

            event.reply("This command cannot be issued from this channel.").setEphemeral(true).complete();
        } catch (MissingRequiredSlashCommandOptionException e) {
            LOGGER.info("User tried to use update command without ID");
            event.reply(MISSING_ID_MESSAGE).setEphemeral(true).complete();
        } catch (LorebookEntryNotFoundException e) {
            LOGGER.info("User tried to update an entry that does not exist");
            event.reply("The entry queried does not exist.").setEphemeral(true).complete();
        } catch (Exception e) {
            LOGGER.error("Exception caught while updating lorebook entry", e);
            event.reply(ERROR_UPDATE).setEphemeral(true).complete();
        }
    }

    @Override
    public void handle(final ModalInteractionEvent event) {

        try {
            LOGGER.debug("Received data from lore entry update modal");
            event.deferReply();
            final EventData eventData = contextDatastore.getEventData();
            final World world = eventData.getBotChannelDefinitions().getChannelConfig().getWorld();

            final String entryId = eventData.getLorebookEntryId();
            final String updatedEntryName = event.getValue("lorebook-entry-name").getAsString();
            final String updatedEntryRegex = event.getValue("lorebook-entry-regex").getAsString();
            final String updatedEntryDescription = event.getValue("lorebook-entry-desc").getAsString();
            final String playerId = retrieveDiscordPlayerId(event.getValue("lorebook-entry-player"),
                    event.getUser().getId());

            final LorebookRegexEntity updatedEntry = updateEntry(updatedEntryDescription, entryId,
                    updatedEntryName, playerId, updatedEntryRegex, world);

            final LorebookEntry entry = lorebookEntryToDTO.apply(updatedEntry);
            final String loreEntryJson = objectMapper.setSerializationInclusion(Include.NON_EMPTY)
                    .writerWithDefaultPrettyPrinter().writeValueAsString(entry);

            moderationService.moderate(loreEntryJson, eventData, event).subscribe(response -> {
                event.reply(MessageFormat.format(ENTRY_UPDATED,
                updatedEntry.getLorebookEntry().getName(), loreEntryJson))
                        .setEphemeral(true).queue(reply -> {
                            reply.deleteOriginal().queueAfter(DELETE_EPHEMERAL_20_SECONDS, TimeUnit.SECONDS);
                        });
            });
        } catch (JsonProcessingException e) {
            LOGGER.error("Error parsing entry data into JSON", e);
            event.reply(ERROR_UPDATE).setEphemeral(true).queue(reply -> {
                reply.deleteOriginal().queueAfter(DELETE_EPHEMERAL_20_SECONDS, TimeUnit.SECONDS);
            });
        }
    }

    private LorebookRegexEntity updateEntry(final String description, final String entryId, final String name,
            final String playerId, final String entryRegex, final World world) {

        final LorebookEntryEntity lorebookEntry = LorebookEntryEntity.builder()
                .description(description)
                .id(entryId)
                .name(name)
                .playerDiscordId(playerId)
                .build();

        final LorebookEntity lorebook = lorebookDTOToEntity.apply(world.getLorebook());
        return lorebookRegexRepository.findByLorebookEntry(lorebookEntry)
                .map(re -> {
                    final LorebookRegexEntity lorebookRegex = LorebookRegexEntity.builder()
                            .id(re.getId())
                            .regex(Optional.ofNullable(entryRegex)
                                    .filter(StringUtils::isNotBlank)
                                    .orElse(name))
                            .lorebookEntry(lorebookEntry)
                            .lorebook(lorebook)
                            .build();

                    lorebookRepository.save(lorebookEntry);
                    lorebookRegexRepository.save(lorebookRegex);
                    return lorebookRegex;
                }).get();
    }

    private String retrieveDiscordPlayerId(final ModalMapping modalMapping, final String id) {

        return Optional.of(modalMapping.getAsString())
                .filter(a -> a.equals("y"))
                .map(a -> id)
                .orElse(null);
    }

    private Modal buildEntryUpdateModal(final LorebookRegexEntity lorebookRegex) {

        LOGGER.debug("Building entry update modal");
        final TextInput lorebookEntryName = TextInput
                .create("lorebook-entry-name", "Name", TextInputStyle.SHORT)
                .setValue(lorebookRegex.getLorebookEntry().getName())
                .setRequired(true)
                .build();

        final String regex = Optional.ofNullable(lorebookRegex.getRegex())
                .filter(StringUtils::isNotBlank)
                .orElse(lorebookRegex.getLorebookEntry().getName());

        final TextInput lorebookEntryRegex = TextInput
                .create("lorebook-entry-regex", "Regular expression (optional)", TextInputStyle.SHORT)
                .setValue(regex)
                .setRequired(false)
                .build();

        final TextInput lorebookEntryDescription = TextInput
                .create("lorebook-entry-desc", "Description", TextInputStyle.PARAGRAPH)
                .setValue(lorebookRegex.getLorebookEntry().getDescription())
                .setRequired(true)
                .build();

        String isPlayerCharacter = StringUtils.isBlank(lorebookRegex.getLorebookEntry()
                .getPlayerDiscordId()) ? "n" : "y";

        final TextInput lorebookEntryPlayer = TextInput
                .create("lorebook-entry-player", "Is this a player character?", TextInputStyle.SHORT)
                .setValue(isPlayerCharacter)
                .setMaxLength(1)
                .setRequired(true)
                .build();

        return Modal.create("update-lorebook-entry-data","Lorebook Entry Update")
                .addComponents(ActionRow.of(lorebookEntryName), ActionRow.of(lorebookEntryRegex),
                        ActionRow.of(lorebookEntryDescription), ActionRow.of(lorebookEntryPlayer)).build();
    }
}
