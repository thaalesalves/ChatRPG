package es.thalesalv.chatrpg.application.translator.airequest;

import org.springframework.stereotype.Component;

import es.thalesalv.chatrpg.domain.enums.AIModelEnum;
import es.thalesalv.chatrpg.domain.model.openai.completion.TextCompletionRequest;
import es.thalesalv.chatrpg.domain.model.openai.dto.ChannelConfig;
import es.thalesalv.chatrpg.domain.model.openai.dto.ModelSettings;
import es.thalesalv.chatrpg.domain.model.openai.dto.Persona;

@Component
public class TextCompletionRequestTranslator {

    public TextCompletionRequest buildRequest(final String prompt, final ChannelConfig channelConfig) {

        final Persona persona = channelConfig.getPersona();
        final ModelSettings modelSettings = channelConfig.getSettings().getModelSettings();
        final String modelName = AIModelEnum.findByInternalName(modelSettings.getModelName()).getModelName();
        final String formattedPrompt = persona.getPersonality().replaceAll("\\{0\\}", persona.getName())
                + "\n" + prompt;

        return TextCompletionRequest.builder()
            .prompt(formattedPrompt)
            .model(modelName)
            .maxTokens(modelSettings.getMaxTokens())
            .temperature(modelSettings.getTemperature())
            .presencePenalty(modelSettings.getPresencePenalty())
            .frequencyPenalty(modelSettings.getFrequencyPenalty())
            .logitBias(modelSettings.getLogitBias())
            .build();
    }
}
