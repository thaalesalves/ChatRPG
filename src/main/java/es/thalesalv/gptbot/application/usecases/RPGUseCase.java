package es.thalesalv.gptbot.application.usecases;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.stanford.nlp.simple.Sentence;
import es.thalesalv.gptbot.adapters.data.ContextDatastore;
import es.thalesalv.gptbot.adapters.data.db.entity.CharacterProfileEntity;
import es.thalesalv.gptbot.adapters.data.db.repository.CharacterProfileRepository;
import es.thalesalv.gptbot.application.service.GptService;
import es.thalesalv.gptbot.application.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

@Component
@RequiredArgsConstructor
public class RPGUseCase {

    private final GptService gptService;
    private final ContextDatastore contextDatastore;
    private final CharacterProfileRepository characterProfileRepository;

    private static final String RPG_DM_INSTRUCTIONS = "I will remember to never act or speak on behalf of {0}. I will not repeat what {0} just said. I will only describe the world around {0}.";
    private static final String CHARACTER_DESCRIPTION = "{0}''s description is: {1}";
    private static final Logger LOGGER = LoggerFactory.getLogger(RPGUseCase.class);
    
    public void generateResponse(final SelfUser bot, final User player, final Mentions mentions, final MessageChannelUnion channel) {
        
        channel.sendTyping().complete();
        LOGGER.debug("Entered generation of response for RPG");
        final List<String> messages = new ArrayList<>();
        channel.getHistory()
            .retrievePast(contextDatastore.getCurrentChannel().getChatHistoryMemory()).complete()
            .stream()
            .map(m -> {
                if (m.getContentDisplay().matches(("@" + bot.getName()).trim() + "$")) {
                    channel.deleteMessageById(m.getId()).complete();
                }

                return m;
            })
            .filter(m -> !m.getContentDisplay().matches(("@" + bot.getName()).trim() + "$"))
            .forEach(m -> {
                messages.add(MessageFormat.format("{0}: {1}",
                    m.getAuthor().getName(),
                    m.getContentDisplay().replaceAll("(@|)" + bot.getName(), StringUtils.EMPTY).trim()));
            });
        
        Collections.reverse(messages);

        final CharacterProfileEntity characterProfile = characterProfileRepository.findByPlayerDiscordId(player.getId());
        if (characterProfile != null) {
            messages.replaceAll(message -> message.replaceAll(player.getAsTag(), characterProfile.getName())
                    .replaceAll("(@|)" + player.getName(), characterProfile.getName()));
        }

        mentions.getUsers().stream()
            .forEach(mention -> {
                final CharacterProfileEntity mentionedProfile = characterProfileRepository.findByPlayerDiscordId(mention.getId());
                if (mentionedProfile != null) {
                    messages.replaceAll(message -> message.replaceAll(mention.getAsTag(), mentionedProfile.getName())
                            .replaceAll("(@|)" + mention.getName(), mentionedProfile.getName()));
                }
            });

        final Sentence sentence = new Sentence(messages.stream().collect(Collectors.joining("\n")));
        final HashSet<String> namesMentioned = new HashSet<String>(sentence.mentions());
        final HashSet<CharacterProfileEntity> charactersMentioned = characterProfileRepository.findByNameIn(namesMentioned);
        charactersMentioned.stream().forEach(character -> {
            messages.add(0, MessageFormat.format(RPG_DM_INSTRUCTIONS, character.getName()));
            messages.add(0, MessageFormat.format(CHARACTER_DESCRIPTION, character.getName(), character.getDescription()));

            final User characterPlayer = channel.getJDA().retrieveUserById(character.getPlayerDiscordId()).complete();
            if (characterPlayer != null) {
                messages.replaceAll(message -> message.replaceAll(characterPlayer.getAsTag(), character.getName())
                        .replaceAll("(@|)" + characterPlayer.getName(), character.getName()));
            }

        });

        MessageUtils.formatPersonality(messages, contextDatastore.getCurrentChannel(), bot);
        final String chatifiedMessage = MessageUtils.chatifyMessages(bot, messages)
                .replace(bot.getName() + " (ID " + bot.getId() + ")", "Dungeon Master")
                .replace(bot.getName(), "Dungeon Master");

        gptService.callDaVinci(chatifiedMessage)
                .filter(r -> !r.getChoices().get(0).getText().isBlank())
                .map(response -> {
                    final String responseText = response.getChoices().get(0).getText();
                    channel.sendMessage(responseText.trim()).queue();
                    return response;
                }).subscribe();
    }
}