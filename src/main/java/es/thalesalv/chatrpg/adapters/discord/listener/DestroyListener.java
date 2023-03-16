package es.thalesalv.chatrpg.adapters.discord.listener;

import es.thalesalv.chatrpg.adapters.beans.JDAConfigurationBean;
import es.thalesalv.chatrpg.application.config.BotConfig;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@RequiredArgsConstructor
public class DestroyListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
    private final JDAConfigurationBean jdaConfig;
    private final BotConfig botConfig;

    @PreDestroy
    public void beforeDestroy() {
        try {
            jdaConfig.jda().getChannelById(TextChannel.class, botConfig.getStatusChannelId()).sendMessage(jdaConfig.jda().getSelfUser().getName() + " is ready to chat!").complete();
        } catch (Exception e) {
            LOGGER.error("Error during destroy: ", e);
        }
    }
}