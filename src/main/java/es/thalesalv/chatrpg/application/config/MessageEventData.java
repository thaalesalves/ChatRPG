package es.thalesalv.chatrpg.application.config;

import es.thalesalv.chatrpg.adapters.data.db.entity.ChannelConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

@Getter
@Setter
@Builder
public class MessageEventData {

    private Guild guild;
    private SelfUser bot;
    private User messageAuthor;
    private Message responseMessage;
    private Message message;
    private MessageChannelUnion channel;
    private ChannelConfig channelConfig;
}
