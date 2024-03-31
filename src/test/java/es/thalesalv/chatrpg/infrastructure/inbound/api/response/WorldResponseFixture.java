package es.thalesalv.chatrpg.infrastructure.inbound.api.response;

import es.thalesalv.chatrpg.core.domain.world.World;
import es.thalesalv.chatrpg.core.domain.world.WorldFixture;

public class WorldResponseFixture {

    public static WorldResponse.Builder publicWorld() {

        World world = WorldFixture.publicWorld().build();

        return WorldResponse.builder()
                .id(world.getId())
                .name(world.getName())
                .description(world.getDescription())
                .adventureStart(world.getAdventureStart())
                .visibility(world.getVisibility().toString())
                .ownerDiscordId(world.getOwnerDiscordId())
                .creationDate(world.getCreationDate())
                .usersAllowedToRead(world.getReaderUsers())
                .usersAllowedToWrite(world.getWriterUsers());
    }

    public static WorldResponse.Builder privateWorld() {

        World world = WorldFixture.privateWorld().build();

        return WorldResponse.builder()
                .id(world.getId())
                .name(world.getName())
                .description(world.getDescription())
                .adventureStart(world.getAdventureStart())
                .visibility(world.getVisibility().toString())
                .ownerDiscordId(world.getOwnerDiscordId())
                .creationDate(world.getCreationDate())
                .usersAllowedToRead(world.getReaderUsers())
                .usersAllowedToWrite(world.getWriterUsers());
    }
}