package es.thalesalv.gptbot.adapters.discord.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import es.thalesalv.gptbot.application.config.BotConfig;
import es.thalesalv.gptbot.application.config.MessageEventData;
import es.thalesalv.gptbot.application.service.interfaces.GptModelService;
import es.thalesalv.gptbot.application.service.usecases.BotUseCase;
import es.thalesalv.gptbot.application.translator.MessageEventDataTranslator;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Service
@RequiredArgsConstructor
public class MessageListener extends ListenerAdapter {

    private final BotConfig botConfig;
    private final ApplicationContext applicationContext;
    private final MessageEventDataTranslator messageEventDataTranslator;

    private static final String MODEL_SERVICE = "ModelService";
    private static final String USE_CASE = "UseCase";
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        LOGGER.debug("Message received -> {}", event);
        if (!event.getAuthor().isBot()) {
            botConfig.getPersonas().forEach(persona -> {
                final boolean isCurrentChannel = persona.getChannelIds().stream().anyMatch(id -> event.getChannel().getId().equals(id));
                if (isCurrentChannel) {
                	MessageEventData messageEventData = messageEventDataTranslator.translate(event);
                    final GptModelService model = (GptModelService) applicationContext.getBean(persona.getModelFamily() + MODEL_SERVICE);
                    final BotUseCase useCase = (BotUseCase) applicationContext.getBean(persona.getIntent() + USE_CASE);
                    useCase.generateResponse(persona, messageEventData, event.getMessage().getMentions(), model);
                }
            });
        }
    }
}
