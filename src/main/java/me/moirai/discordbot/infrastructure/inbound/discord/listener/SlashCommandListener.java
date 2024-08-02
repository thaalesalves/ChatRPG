package me.moirai.discordbot.infrastructure.inbound.discord.listener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import me.moirai.discordbot.common.usecases.UseCaseRunner;
import me.moirai.discordbot.core.application.usecase.discord.slashcommands.GenerateOutput;
import me.moirai.discordbot.core.application.usecase.discord.slashcommands.RetryGeneration;
import me.moirai.discordbot.core.application.usecase.discord.slashcommands.StartCommand;
import me.moirai.discordbot.core.application.usecase.discord.slashcommands.TokenizeInput;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

@Component
public class SlashCommandListener extends ListenerAdapter {

    private static final String OUTPUT_GENERATED = "Output generated.";

    private final UseCaseRunner useCaseRunner;

    public SlashCommandListener(UseCaseRunner useCaseRunner) {
        this.useCaseRunner = useCaseRunner;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        String command = event.getFullCommandName();
        TextChannel textChannel = event.getChannel().asTextChannel();
        Guild guild = event.getGuild();
        Member bot = guild.retrieveMember(event.getJDA().getSelfUser()).complete();
        User author = event.getMember().getUser();

        if (!author.isBot()) {
            switch (command) {
                case "retry" -> {
                    InteractionHook interactionHook = sendNotification(event, "Generating new output...");
                    String botUsername = bot.getUser().getName();
                    String botNickname = StringUtils.isNotBlank(bot.getNickname()) ? bot.getNickname() : botUsername;

                    RetryGeneration useCase = RetryGeneration.builder()
                            .botId(bot.getId())
                            .botNickname(botNickname)
                            .botUsername(botUsername)
                            .guildId(guild.getId())
                            .channelId(textChannel.getId())
                            .build();

                    useCaseRunner.run(useCase)
                            .doOnError(error -> updateNotification(interactionHook, error.getMessage()))
                            .subscribe(__ -> updateNotification(interactionHook, OUTPUT_GENERATED));
                }
                case "go" -> {
                    InteractionHook interactionHook = sendNotification(event, "Generating output...");
                    String botUsername = bot.getUser().getName();
                    String botNickname = StringUtils.isNotBlank(bot.getNickname()) ? bot.getNickname() : botUsername;

                    GenerateOutput useCase = GenerateOutput.builder()
                            .botId(bot.getId())
                            .botNickname(botNickname)
                            .botUsername(botUsername)
                            .guildId(guild.getId())
                            .channelId(textChannel.getId())
                            .build();

                    useCaseRunner.run(useCase)
                            .doOnError(error -> updateNotification(interactionHook, error.getMessage()))
                            .subscribe(__ -> updateNotification(interactionHook, OUTPUT_GENERATED));
                }
                case "tokenize" -> {
                    InteractionHook interactionHook = sendNotification(event, "Tokenizing input...");
                    String inputToBeTokenized = event.getOption("input").getAsString();

                    String tokenizationResult = useCaseRunner.run(TokenizeInput.build(inputToBeTokenized));

                    updateNotification(interactionHook, tokenizationResult);
                }
                case "start" -> {
                    InteractionHook interactionHook = sendNotification(event, "Starting adventure...");
                    String botUsername = bot.getUser().getName();
                    String botNickname = StringUtils.isNotBlank(bot.getNickname()) ? bot.getNickname() : botUsername;

                    StartCommand useCase = StartCommand.builder()
                            .botId(bot.getId())
                            .botNickname(botNickname)
                            .botUsername(botUsername)
                            .guildId(guild.getId())
                            .channelId(textChannel.getId())
                            .build();

                    useCaseRunner.run(useCase)
                            .doOnError(error -> updateNotification(interactionHook, error.getMessage()))
                            .subscribe(__ -> updateNotification(interactionHook, "Adventure started! Enjoy!"));
                }
                case "say" -> {
                    TextInput content = TextInput.create("content", "Content", TextInputStyle.PARAGRAPH)
                            .setPlaceholder("Message to be sent as the bot")
                            .setMinLength(1)
                            .setMaxLength(2000)
                            .build();

                    Modal modal = Modal.create("sayAsBot", "Say as bot")
                            .addComponents(ActionRow.of(content))
                            .build();

                    event.replyModal(modal).complete();
                }
            }
        }
    }

    private Message updateNotification(InteractionHook interactionHook, String newContent) {
        return interactionHook.editOriginal(newContent).complete();
    }

    private InteractionHook sendNotification(SlashCommandInteractionEvent event, String message) {
        return event.reply(message).setEphemeral(true).complete();
    }
}
