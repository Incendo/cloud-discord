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
package org.incendo.cloud.discord.discord4j.example.commands;

import cloud.commandframework.Description;
import cloud.commandframework.keys.CloudKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.discord4j.Discord4JCommandManager;
import org.incendo.cloud.discord.discord4j.Discord4JInteraction;
import org.incendo.cloud.discord.discord4j.example.Example;
import org.incendo.cloud.discord.slash.CommandScope;
import reactor.core.publisher.Mono;

import static cloud.commandframework.arguments.standard.StringParser.greedyStringParser;
import static org.incendo.cloud.discord.discord4j.Discord4JCommandExecutionHandler.reactiveHandler;

/**
 * Example of a command that responds with the original input.
 */
public final class PingCommand implements Example {

    private static final CloudKey<String> COMPONENT_MESSAGE = CloudKey.of(
            "message",
            String.class
    );

    @Override
    public void register(final @NonNull Discord4JCommandManager<Discord4JInteraction> commandManager) {
        commandManager.command(
                commandManager.commandBuilder("ping", Description.of("A ping command"))
                        .apply(CommandScope.guilds()) // You may only ping in guilds!
                        .required(COMPONENT_MESSAGE, greedyStringParser(), Description.of("The message"))
                        .handler(reactiveHandler(context -> {
                            final Discord4JInteraction interaction = context.sender();
                            final String message = context.get(COMPONENT_MESSAGE);
                            return interaction.commandEvent()
                                    .map(event -> (Mono<?>) event.reply(message).withEphemeral(true))
                                    .orElseGet(Mono::empty);
                        }))
        );
    }
}
