package es.thalesalv.chatrpg.application.translator;

import org.springframework.stereotype.Component;

import es.thalesalv.chatrpg.adapters.data.db.entity.ChannelConfig;
import es.thalesalv.chatrpg.adapters.data.db.entity.ModelSettings;
import es.thalesalv.chatrpg.adapters.data.db.entity.Persona;
import es.thalesalv.chatrpg.domain.model.openai.gpt.Gpt3Request;

@Component
public class Gpt3RequestTranslator {

    public Gpt3Request buildRequest(final String prompt, final ChannelConfig channelConfig) {

        final Persona persona = channelConfig.getPersona();
        final ModelSettings modelSettings = channelConfig.getModelSettings();
        final String formattedPrompt = persona.getPersonality().replaceAll("\\{0\\}", persona.getName())
                + "\n" + prompt;

        return Gpt3Request.builder()
            .prompt(formattedPrompt)
            .model(modelSettings.getModelName())
            .maxTokens(modelSettings.getMaxTokens())
            .temperature(modelSettings.getTemperature())
            .presencePenalty(modelSettings.getPresencePenalty())
            .frequencyPenalty(modelSettings.getFrequencyPenalty())
            .logitBias(modelSettings.getLogitBias())
            .build();
    }
}
