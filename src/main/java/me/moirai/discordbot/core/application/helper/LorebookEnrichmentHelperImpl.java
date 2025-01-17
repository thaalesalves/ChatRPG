package me.moirai.discordbot.core.application.helper;

import static me.moirai.discordbot.common.util.DefaultStringProcessors.formatRpgDirective;
import static me.moirai.discordbot.common.util.DefaultStringProcessors.replaceTemplateWithValueIgnoreCase;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.LF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.micrometer.common.util.StringUtils;
import me.moirai.discordbot.common.annotation.Helper;
import me.moirai.discordbot.common.exception.AssetNotFoundException;
import me.moirai.discordbot.common.util.StringProcessor;
import me.moirai.discordbot.core.application.usecase.discord.DiscordMessageData;
import me.moirai.discordbot.core.application.usecase.discord.DiscordUserDetails;
import me.moirai.discordbot.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.discordbot.core.domain.adventure.AdventureService;
import me.moirai.discordbot.core.domain.port.TokenizerPort;
import me.moirai.discordbot.infrastructure.outbound.adapter.request.ModelConfigurationRequest;

@Helper
public class LorebookEnrichmentHelperImpl implements LorebookEnrichmentHelper {

    private static final String ENTRY_DESCRIPTION = "[ Description of %s: %s ]";
    private static final String RETRIEVED_MESSAGES = "retrievedMessages";
    private static final String LOREBOOK = "lorebook";

    private final TokenizerPort tokenizerPort;
    private final AdventureService adventureService;
    private final ChatMessageHelper chatMessageService;

    public LorebookEnrichmentHelperImpl(
            TokenizerPort tokenizerPort,
            AdventureService adventureService,
            ChatMessageHelper chatMessageService) {

        this.tokenizerPort = tokenizerPort;
        this.adventureService = adventureService;
        this.chatMessageService = chatMessageService;
    }

    @Override
    public Map<String, Object> enrichContextWithLorebookForRpg(List<DiscordMessageData> rawMessageHistory,
            String adventureId, ModelConfigurationRequest modelConfiguration) {

        int totalTokens = modelConfiguration.getAiModel().getHardTokenLimit();
        int reservedTokensForLorebook = (int) Math.floor(totalTokens * 0.30);

        List<AdventureLorebookEntry> entriesFound = findLorebookEntries(adventureId, rawMessageHistory);
        List<DiscordMessageData> formattedHistory = enrichMessagesWithLorebook(rawMessageHistory, entriesFound);
        List<String> formattedEntries = formatEntriesForContext(entriesFound, reservedTokensForLorebook);

        Map<String, Object> context = new HashMap<>();
        context.put(RETRIEVED_MESSAGES, new ArrayList<>(formattedHistory));

        String stringifiedLorebook = stringifyList(formattedEntries);
        if (StringUtils.isNotBlank(stringifiedLorebook)) {
            context.put(LOREBOOK, stringifiedLorebook);
        }

        return chatMessageService.addMessagesToContext(context, reservedTokensForLorebook);
    }

    @Override
    public Map<String, Object> enrichContextWithLorebook(List<DiscordMessageData> rawMessageHistory, String adventureId,
            ModelConfigurationRequest modelConfiguration) {

        int totalTokens = modelConfiguration.getAiModel().getHardTokenLimit();
        int reservedTokensForLorebook = (int) Math.floor(totalTokens * 0.30);

        List<String> messageHistory = rawMessageHistory.stream()
                .map(DiscordMessageData::getContent)
                .collect(Collectors.toCollection(ArrayList::new));

        String stringifiedStory = stringifyList(messageHistory);

        Map<String, Object> context = new HashMap<>();
        context.put(RETRIEVED_MESSAGES, new ArrayList<>(rawMessageHistory));

        List<AdventureLorebookEntry> entriesFound = adventureService.findAllLorebookEntriesByRegex(stringifiedStory,
                adventureId);
        Map<String, Object> enrichedContext = addEntriesFoundToContext(entriesFound, context,
                reservedTokensForLorebook);

        return chatMessageService.addMessagesToContext(enrichedContext, reservedTokensForLorebook);
    }

    private List<AdventureLorebookEntry> findLorebookEntries(String adventureId,
            List<DiscordMessageData> rawMessageHistory) {

        List<AdventureLorebookEntry> entriesInHistory = findLorebookEntriesInHistory(adventureId, rawMessageHistory);
        List<AdventureLorebookEntry> entriesByMention = findLorebookEntriesByMention(adventureId, rawMessageHistory);
        List<AdventureLorebookEntry> entriesByAuthor = findLorebookEntriesByAuthor(adventureId, rawMessageHistory);

        Set<String> entryIdsNotDuplicated = new HashSet<>();
        return Stream.of(entriesInHistory, entriesByMention, entriesByAuthor)
                .flatMap(Collection::stream)
                .filter(entry -> entryIdsNotDuplicated.add(entry.getId()))
                .toList();
    }

    private List<AdventureLorebookEntry> findLorebookEntriesInHistory(
            String adventureId, List<DiscordMessageData> rawMessageHistory) {

        List<String> messageHistory = rawMessageHistory.stream()
                .map(DiscordMessageData::getContent)
                .toList();

        return adventureService.findAllLorebookEntriesByRegex(stringifyList(messageHistory), adventureId);
    }

    private List<AdventureLorebookEntry> findLorebookEntriesByMention(String adventureId,
            List<DiscordMessageData> rawMessageHistory) {

        return rawMessageHistory.stream()
                .flatMap(message -> message.getMentionedUsers().stream())
                .map(user -> adventureService.findLorebookEntryByPlayerDiscordId(user.getId(), adventureId))
                .toList();
    }

    private List<AdventureLorebookEntry> findLorebookEntriesByAuthor(String adventureId,
            List<DiscordMessageData> rawMessageHistory) {

        return rawMessageHistory.stream()
                .map(message -> {
                    try {
                        return adventureService.findLorebookEntryByPlayerDiscordId(message.getAuthor().getId(),
                                adventureId);
                    } catch (AssetNotFoundException exception) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        AdventureLorebookEntry::getId,
                        entry -> entry,
                        (existing, replacement) -> existing))
                .values()
                .stream()
                .toList();
    }

    private List<DiscordMessageData> enrichMessagesWithLorebook(List<DiscordMessageData> rawMessageHistory,
            List<AdventureLorebookEntry> lorebook) {

        List<DiscordMessageData> messagesFormattedForMentions = formatMessagesWithMentions(
                rawMessageHistory, lorebook);

        return formatMessagesWithAuthor(messagesFormattedForMentions, lorebook);
    }

    private List<DiscordMessageData> formatMessagesWithMentions(
            List<DiscordMessageData> messageHistory, List<AdventureLorebookEntry> lorebook) {

        return messageHistory.stream()
                .flatMap(message -> {
                    List<DiscordMessageData> messages = new ArrayList<>();

                    if (isEmpty(message.getMentionedUsers())) {
                        messages.add(message);
                    }

                    for (DiscordUserDetails mentionedUser : message.getMentionedUsers()) {
                        messages.add(lorebook.stream()
                                .filter(entry -> mentionedUser.getId().equals(entry.getPlayerDiscordId()))
                                .findFirst()
                                .map(entry -> {
                                    StringProcessor processor = new StringProcessor();
                                    processor.addRule(replaceTemplateWithValueIgnoreCase(
                                            entry.getName(), mentionedUser.getNickname()));

                                    processor.addRule(replaceTemplateWithValueIgnoreCase(
                                            entry.getName(), mentionedUser.getMention()));

                                    processor.addRule(replaceTemplateWithValueIgnoreCase(
                                            entry.getName(), mentionedUser.getUsername()));

                                    return DiscordMessageData.builder()
                                            .id(message.getId())
                                            .author(message.getAuthor())
                                            .content(processor.process(message.getContent()))
                                            .build();
                                })
                                .orElse(message));
                    }

                    return messages.stream();
                })
                .toList();
    }

    private List<DiscordMessageData> formatMessagesWithAuthor(
            List<DiscordMessageData> rawMessageHistory, List<AdventureLorebookEntry> lorebook) {

        return rawMessageHistory.stream()
                .map(message -> lorebook.stream()
                        .filter(entry -> message.getAuthor().getId().equals(entry.getPlayerDiscordId()))
                        .findFirst()
                        .map(entry -> {
                            DiscordUserDetails author = DiscordUserDetails.builder()
                                    .id(message.getAuthor().getId())
                                    .mention(message.getAuthor().getMention())
                                    .username(message.getAuthor().getUsername())
                                    .nickname(entry.getName())
                                    .build();

                            StringProcessor processor = new StringProcessor();
                            processor.addRule(replaceTemplateWithValueIgnoreCase(
                                    entry.getName(), message.getAuthor().getNickname()));

                            processor.addRule(replaceTemplateWithValueIgnoreCase(
                                    entry.getName(), message.getAuthor().getMention()));

                            processor.addRule(replaceTemplateWithValueIgnoreCase(
                                    entry.getName(), message.getAuthor().getUsername()));

                            String messageContent = processor.process(message.getContent());

                            return DiscordMessageData.builder()
                                    .id(message.getId())
                                    .author(author)
                                    .content(formatRpgDirective(entry.getName()).apply(messageContent))
                                    .build();
                        })
                        .orElse(message))
                .toList();
    }

    private List<String> formatEntriesForContext(List<AdventureLorebookEntry> entries, int reservedTokensForLorebook) {

        List<String> lorebook = new ArrayList<>();

        entries.stream()
                .takeWhile(entryData -> {
                    String entry = String.format(ENTRY_DESCRIPTION, entryData.getName(), entryData.getDescription());
                    String stringifiedLorebook = stringifyList(lorebook);

                    int tokensInEntry = tokenizerPort.getTokenCountFrom(entry);
                    int tokensInContext = tokenizerPort.getTokenCountFrom(stringifiedLorebook);

                    int tokensLeftInContext = reservedTokensForLorebook - tokensInContext;

                    return tokensInEntry <= tokensLeftInContext;
                })
                .map(entryData -> String.format(ENTRY_DESCRIPTION, entryData.getName(), entryData.getDescription()))
                .forEach(lorebook::add);

        return lorebook;
    }

    private Map<String, Object> addEntriesFoundToContext(List<AdventureLorebookEntry> entries,
            Map<String, Object> context,
            int reservedTokensForLorebook) {

        List<String> lorebook = new ArrayList<>();

        entries.stream()
                .takeWhile(entryData -> {
                    String entry = String.format(ENTRY_DESCRIPTION, entryData.getName(), entryData.getDescription());
                    String stringifiedLorebook = stringifyList(lorebook);

                    int tokensInEntry = tokenizerPort.getTokenCountFrom(entry);
                    int tokensInContext = tokenizerPort.getTokenCountFrom(stringifiedLorebook);

                    int tokensLeftInContext = reservedTokensForLorebook - tokensInContext;

                    return tokensInEntry <= tokensLeftInContext;
                })
                .map(entryData -> String.format(ENTRY_DESCRIPTION, entryData.getName(), entryData.getDescription()))
                .forEach(lorebook::add);

        String stringifiedLorebook = stringifyList(lorebook);
        if (StringUtils.isNotBlank(stringifiedLorebook)) {
            context.put(LOREBOOK, stringifiedLorebook);
        }

        return context;
    }

    private String stringifyList(List<String> list) {

        return list.stream().collect(Collectors.joining(LF));
    }
}
