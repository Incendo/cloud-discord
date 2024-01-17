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
package org.incendo.cloud.discord.jda5.example.commands;

import cloud.commandframework.Description;
import cloud.commandframework.keys.CloudKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.jda5.JDA5CommandManager;
import org.incendo.cloud.discord.jda5.JDAInteraction;
import org.incendo.cloud.discord.jda5.ReplySetting;
import org.incendo.cloud.discord.jda5.example.Example;
import org.incendo.cloud.discord.slash.CommandScope;

import static cloud.commandframework.arguments.standard.StringParser.greedyStringParser;

/**
 * Example of a command that responds with the original input.
 */
public final class PingCommand implements Example {

    private static final CloudKey<String> COMPONENT_MESSAGE = CloudKey.of(
            "message",
            String.class
    );

    @Override
    public void register(final @NonNull JDA5CommandManager<JDAInteraction> commandManager) {
        commandManager.command(
                commandManager.commandBuilder("ping", Description.of("A ping command"))
                        .apply(ReplySetting.defer(true)) // Defer the response & make the response ephemeral.
                        .apply(CommandScope.guilds()) // You may only ping in guilds!
                        .required(COMPONENT_MESSAGE, greedyStringParser(), Description.of("The message"))
                        .handler(context -> {
                            final JDAInteraction interaction = context.sender();
                            final String message = context.get(COMPONENT_MESSAGE);

                            // When using ephemeral messages we must edit the original message
                            // using the hook from the interaction event.
                            interaction.interactionEvent().getHook().sendMessage(message).queue();
                        })
        );
    }
}
