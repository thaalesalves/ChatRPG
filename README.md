# ChatRPG: a virtual Dungeon Master for RPG adventures
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=chatrpg-cs_chatrpg-be&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=chatrpg-cs_chatrpg-be) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=chatrpg-cs_chatrpg-be&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=chatrpg-cs_chatrpg-be) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=chatrpg-cs_chatrpg-be&metric=coverage)](https://sonarcloud.io/summary/new_code?id=chatrpg-cs_chatrpg-be) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=chatrpg-cs_chatrpg-be&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=chatrpg-cs_chatrpg-be) [![Java CI](https://github.com/thaalesalves/ChatRPG/actions/workflows/automated-cicd.yaml/badge.svg?branch=master)](https://github.com/thaalesalves/ChatRPG/actions/workflows/automated-cicd.yaml)

ChatRPG is a Discord bot powered by JDA that is connected to Azure's OpenAI GPT API. It is cabaple of generating text using base or finetuned models, and was made to act primarily as a chatbot and as an RPG dungeon master. Developed by veteran AI Dungeon and NovelAI players and contributors, aimed to make text-based RPGs more flexible and easier to use on Discord.

## Technologies used
* Java 21
* Maven
* Spring Boot
* Spring WebFlux
* Spring Data
* Spring Security
* JDA
* PostgreSQL
* HuggingFace Tokenizer

## What does it do?
ChatRPG is mainly focused on RPG DM'ing, but it can also be used as a normal chatbot. The Persona feature allows users to customize behavior and personality for the bot, making it talk or act in a specific way. Channel configs can be set to three game mods, which are `RPG`, `Author` and `Chat`, allowing usage of the bot as an RPG game master, an author of a play or story or a regular chatbot.

## Is it free?
Yes and no. The code is free for usage, and so is Discord's API. But as it relies on Azure's API to power the AI, you need to have a subscription on their side and pay according to usage and model chosen. Do take that into consideration before running ChatRPG or choosing a model to run it.

## Does it work with other GPT models?
For now, only OpenAI models of the GPT-3.5 and GPT-4 families are supported. ChatRPG does not support Eleuther AI's, GooseAI's, Meta's or AI21's models as of now, nor does it support usage of OpenAI's GPT-2 models locally.

## Building from source
To run ChatRPG, you can clone the code and execute him locally. To do so, you'll need to have JDK 21, a PostgreSQL database (ChatRPG comes with a `docker-compose.yaml` for convenience) and Maven. You will also need a Discord Developer account set up with an app created with the proper permissions, as well as an Azure account.

### Discord app permissions
To have ChatRPG work, your Discord app needs to have both the `Server Members` and `Message Content` intents allowed in the `Bot` menu of the Discord Developer dashboard. When inviting the bot to your server, make sure to add the `bot` and `applications.commands` scopes added in the `OAuth > URL Generator` menu of the Discord Developer dashboard. The bot also needs to have permission to read, send and delete messages in the channels specified in its personas, because when a comment is problematic, it will try to delete it. For OAuth2 authentication to work with the ChatRPG API, you will also need to set up integration with Discord and generate a login link, which relies on a redirect URI being created (by the default, ChatRPG uses `localhost:8080/auth/code`, and unless you change the port and/or the host ChatRPG is running on, you should just use that URL as well).

### API keys
You need to have both yout Discord API Key and your Azure API key in hand to set up ChatRPG.

### Building
1. Clone the repo
2. Add both keys to the bot's `application.yaml`
    - The recommended way of doing this is setting the keys to the `DISCORD_BOT_API_TOKEN` and `OPENAI_API_TOKEN` environment variables respectively, in which case you won't need to modify those values in the YAML; and everything is also safer that way
3. Enable Developer Mode on your Discord client and right click > copy ID of the channels you want the bot to have access to
4. Set up the database and either rewrite the values in the YAML or add the values to their respective environment variable
    - Same advice as for the API keys: instead of rewriting the config file, add the proper environment variables
5. Compile the code with `mvn clean install`
6. Run the bot
    - Through the IDE of your choice
    - Through the console with Maven by running `mvn spring-boot:run` on the bot's root folder
    - Through the console by running the JAR file directly with `java -jar chatrpg-0.0.1-SNAPSHOT.jar`

## Features
ChatRPG was made with RPG DM'ing in mind, so we're striving to add commands and features that make that experience richer.

* Slash commands to manage the bot.
* Custom personas with their own model settings.
* Custom worlds for the bot to generate adventures in.
* Channel configurations to set up model definitions and moderation used.
* Lorebook with regex capabilities to improve the AI's context on the adventure.
* Lorebook entries that can be set as player characters so the AI knows who's who and refrains from speaking on behalf of players.
* Moderation filters powered by OpenAI's API to avoid problematic and abusive topics.
* Compatibility with GPT-3.5 and GPT-4.
* Main use case is as an RPG dungeon master.
* Can also be used as the author of a story.
* Can also be used as a normal chatbot if the right intent is used.
