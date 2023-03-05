package es.thalesalv.gptbot.domain.model.openai.gpt;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatGptRequest {

    @JsonProperty("model")
    private String model;

    @JsonProperty("messages")
    private List<ChatGptMessage> messages;

    @JsonProperty("stop")
    private String stop;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("n")
    private Integer n;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;
}