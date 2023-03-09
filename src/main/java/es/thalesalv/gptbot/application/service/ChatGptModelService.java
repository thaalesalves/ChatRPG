package es.thalesalv.gptbot.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import es.thalesalv.gptbot.adapters.data.db.entity.LorebookEntry;
import es.thalesalv.gptbot.adapters.rest.OpenAIApiService;
import es.thalesalv.gptbot.application.config.MessageEventData;
import es.thalesalv.gptbot.application.errorhandling.CommonErrorHandler;
import es.thalesalv.gptbot.application.service.helper.MessageFormatHelper;
import es.thalesalv.gptbot.application.service.interfaces.GptModelService;
import es.thalesalv.gptbot.application.translator.ChatGptRequestTranslator;
import es.thalesalv.gptbot.domain.exception.ModelResponseBlankException;
import es.thalesalv.gptbot.domain.model.openai.gpt.ChatGptMessage;
import es.thalesalv.gptbot.domain.model.openai.gpt.ChatGptRequest;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.User;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatGptModelService implements GptModelService {

    private final MessageFormatHelper lorebookEntryExtractionHelper;
    private final CommonErrorHandler commonErrorHandler;
    private final ChatGptRequestTranslator chatGptRequestTranslator;
    private final OpenAIApiService openAiService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatGptModelService.class);

    @Override
    public Mono<String> generate(final String prompt, final List<String> messages, final MessageEventData eventData) {

        LOGGER.debug("Called inference for ChatGPT. Persona -> {}", eventData.getPersona());
        final Mentions mentions = eventData.getMessage().getMentions();
        final User author = eventData.getMessageAuthor();
        final Set<LorebookEntry> entriesFound = new HashSet<>();

        lorebookEntryExtractionHelper.handleEntriesMentioned(messages, entriesFound);
        if (eventData.getPersona().getIntent().equals("dungeonMaster")) {
            lorebookEntryExtractionHelper.handlePlayerCharacterEntries(entriesFound, messages, author, mentions);
            lorebookEntryExtractionHelper.processEntriesFoundForRpg(entriesFound, messages, author.getJDA());
        } else {
            lorebookEntryExtractionHelper.processEntriesFoundForChat(entriesFound, messages, author.getJDA());
        }

        final List<ChatGptMessage> chatGptMessages = lorebookEntryExtractionHelper.formatMessagesForChatGpt(entriesFound, messages, eventData);
        final ChatGptRequest request = chatGptRequestTranslator.buildRequest(messages, eventData.getPersona(), chatGptMessages);
        return openAiService.callGptChatApi(request, eventData).map(response -> {
            final String responseText = response.getChoices().get(0).getMessage().getContent();
            if (StringUtils.isBlank(responseText)) {
                throw new ModelResponseBlankException();
            }

            return responseText.trim();
        })
        .doOnError(ModelResponseBlankException.class::isInstance, e -> commonErrorHandler.handleEmptyResponse(eventData));
    }
}
