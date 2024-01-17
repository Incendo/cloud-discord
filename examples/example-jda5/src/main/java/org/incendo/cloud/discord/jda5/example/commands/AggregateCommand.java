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
import cloud.commandframework.arguments.aggregate.AggregateCommandParser;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import net.dv8tion.jda.api.entities.User;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.discord.immutables.ImmutableImpl;
import org.incendo.cloud.discord.jda5.JDA5CommandManager;
import org.incendo.cloud.discord.jda5.JDAInteraction;
import org.incendo.cloud.discord.jda5.JDAParser;
import org.incendo.cloud.discord.jda5.example.Example;

import static cloud.commandframework.arguments.standard.IntegerParser.integerParser;

/**
 * Example showcasing aggregate parsers.
 */
public final class AggregateCommand implements Example {

    @Override
    public void register(final @NonNull JDA5CommandManager<JDAInteraction> commandManager) {
        final AggregateCommandParser<JDAInteraction, Hug> hugParser = AggregateCommandParser.<JDAInteraction>builder()
                .withComponent("recipient", JDAParser.userParser())
                .withComponent("number", integerParser(1, 100))
                .withDirectMapper(Hug.class, (cmdCtx, ctx) -> ArgumentParseResult.success(
                        HugImpl.of(ctx.get("recipient"), ctx.get("number"))
                )).build();
        commandManager.command(
                commandManager.commandBuilder("hug", Description.of("Hug someone"))
                        .required("hug", hugParser)
                        .handler(context -> {
                            final JDAInteraction interaction = context.sender();
                            final Hug hug = context.get("hug");

                            interaction.replyCallback().reply("You hug " + hug.recipient().getAsMention()
                                    + " " + hug.number() + " time(s)!").queue();
                        })
        );
    }


    @ImmutableImpl
    @Value.Immutable
    interface Hug {

        /**
         * Returns the recipient of the hugs.
         *
         * @return hug recipient
         */
        @NonNull User recipient();

        /**
         * Returns the number of hugs.
         *
         * @return the number of hugs
         */
        int number();
    }
}
