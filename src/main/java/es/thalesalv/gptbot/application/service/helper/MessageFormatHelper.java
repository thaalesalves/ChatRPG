package es.thalesalv.gptbot.application.service.helper;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import es.thalesalv.gptbot.adapters.data.db.entity.LorebookEntry;
import es.thalesalv.gptbot.adapters.data.db.entity.LorebookRegex;
import es.thalesalv.gptbot.adapters.data.db.repository.LorebookRegexRepository;
import es.thalesalv.gptbot.adapters.data.db.repository.LorebookRepository;
import es.thalesalv.gptbot.application.config.MessageEventData;
import es.thalesalv.gptbot.domain.model.openai.gpt.ChatGptMessage;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;

@Component
@RequiredArgsConstructor
public class MessageFormatHelper {

    private static final String ROLE_SYSTEM = "system";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final String ROLE_USER = "user";
    private static final String DUNGEON_MASTER = "Dungeon Master";
    private static final String CHARACTER_DESCRIPTION = "{0} description: {1}";
    private static final String RPG_DM_INSTRUCTIONS = "I will remember to never act or speak on behalf of {0}. I will not repeat what {0} just said. I will only describe the world around {0}.";

    private final LorebookRepository lorebookRepository;
    private final LorebookRegexRepository lorebookRegexRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageFormatHelper.class);

    /**
     * Extracts player characters entries from database given the player's Discord user ID
     * @param entriesFound List of entries found in the messages until now
     * @param messages List of messages in the channel
     * @param player Player user
     * @param mentions Mentioned users (their characters are extracted too)
     */
    public void handlePlayerCharacterEntries(final Set<LorebookEntry> entriesFound, final List<String> messages, final User player, final Mentions mentions) {

        LOGGER.debug("Entered player character entry handling");
        lorebookRepository.findByPlayerDiscordId(player.getId())
                .ifPresent(entry -> {
                    entriesFound.add(entry);
                    messages.replaceAll(m -> m.replaceAll(player.getAsTag(), entry.getName())
                            .replaceAll("(@|)" + player.getName(), entry.getName()));
                });

        mentions.getUsers().stream()
                .forEach(mention -> lorebookRepository.findByPlayerDiscordId(mention.getId())
                        .ifPresent(entry -> {
                            entriesFound.add(entry);
                            messages.replaceAll(m -> m.replaceAll(mention.getAsTag(), entry.getName())
                                    .replaceAll("(@|)" + mention.getName(), entry.getName()));
                        }));
    }

    /**
     * Extracts lore entries from the conversation when they're mentioned by name
     * @param messages List of messages in the channel
     * @param entriesFound List of entries found in the messages until now
     */
    public void handleEntriesMentioned(final List<String> messageList, final Set<LorebookEntry> entriesFound) {

        LOGGER.debug("Entered mentioned entries handling");
        final String messages = messageList.stream().collect(Collectors.joining("\n"));
        List<LorebookRegex> charRegex = lorebookRegexRepository.findAll();
        charRegex.forEach(e -> {
            Pattern p = Pattern.compile(e.getRegex());
            Matcher matcher = p.matcher(messages);
            if (matcher.find()) {
                lorebookRepository.findById(e.getLorebookEntry().getId()).ifPresent(entriesFound::add);
            }
        });
    }

    public void processEntriesFound(final Set<LorebookEntry> entriesFound, final List<String> messages, final JDA jda) {

        entriesFound.stream().forEach(entry -> {
            if (StringUtils.isNotBlank(entry.getPlayerDiscordId())) {
                messages.add(0, MessageFormat.format(RPG_DM_INSTRUCTIONS, entry.getName()));
            }

            messages.add(0, MessageFormat.format(CHARACTER_DESCRIPTION, entry.getName(), entry.getDescription()));
            Optional.ofNullable(entry.getPlayerDiscordId()).ifPresent(id -> {
                final User p = jda.retrieveUserById(id).complete();
                messages.replaceAll(m -> m.replaceAll(p.getAsTag(), entry.getName())
                        .replaceAll("(@|)" + p.getName(), entry.getName()));
            });
        });
    }

    public List<ChatGptMessage> formatMessagesForChatGpt(final Set<LorebookEntry> entriesFound, final List<String> messages, final MessageEventData eventData) {

        final SelfUser bot = eventData.getBot();
        final String personality = eventData.getPersona().getPersonality().replace("{0}", bot.getName());
        final List<ChatGptMessage> chatGptMessages = messages.stream()
                .filter(msg -> !msg.trim().equals((bot.getName() + " said:").trim()))
                .map(msg -> {
                    String role = determineRole(msg, bot);
                    msg = formatBotName(msg, bot);
                    return ChatGptMessage.builder()
                            .role(role)
                            .content(msg)
                            .build();
                })
                .collect(Collectors.toList());

        chatGptMessages.add(0, ChatGptMessage.builder()
                .role(ROLE_SYSTEM)
                .content(MessageFormat.format(personality, bot.getName())
                        .replace("@" + bot.getName(), StringUtils.EMPTY).trim())
                .build());
            
        return chatGptMessages;
    }

    private String formatBotName(String msg, SelfUser bot) {

        return msg.replace(bot.getName() + " said: ", StringUtils.EMPTY)
                .replaceAll("Dungeon Master says: ", StringUtils.EMPTY);
    }

    private String determineRole(String message, SelfUser bot) {

        final boolean isChat = message.matches("^(.*) (says|said|quoted|replied).*((.|\n)*)");
        if (message.startsWith(bot.getName()) || message.startsWith(DUNGEON_MASTER)) {
            return ROLE_ASSISTANT;
        } else if (isChat && !message.startsWith("I will remember to never")) {
            return ROLE_USER;
        }

        return ROLE_SYSTEM;
    }
}
