//
// MIT License
//
// Copyright (c) 2024 Incendo
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package org.incendo.cloud.discord.jda6.example;

import java.io.File;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.incendo.cloud.discord.slash.DiscordSetting;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example Discord bot using cloud-jda6.
 */
public final class ExampleBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleBot.class);

    /**
     * Launches the bot.
     *
     * @param args ignored args
     */
    public static void main(final @NonNull String@NonNull[] args) {
        new ExampleBot();
    }

    private final JDA6CommandManager<JDAInteraction> commandManager;
    private final BotConfiguration botConfiguration;
    private final JDA jda;

    private ExampleBot() {
        LOGGER.info("Loading configuration...");
        this.botConfiguration = new PropertiesBotConfiguration(new File("bot.properties"));

        LOGGER.info("Creating command manager...");
        this.commandManager = new JDA6CommandManager<>(
                ExecutionCoordinator.simpleCoordinator(),
                JDAInteraction.InteractionMapper.identity()
        );
        if (this.botConfiguration.ephemeralErrorMessages()) {
            LOGGER.info("Activating ephemeral error messages");
            this.commandManager.discordSettings().set(DiscordSetting.EPHEMERAL_ERROR_MESSAGES, true);
        }

        new Examples(this.commandManager).registerExamples();

        LOGGER.info("Starting JDA...");
        this.jda = JDABuilder.createDefault(this.botConfiguration.token())
                // You need to register the event listener to your JDA instance.
                .addEventListeners(this.commandManager.createListener())
                .build();
    }
}
