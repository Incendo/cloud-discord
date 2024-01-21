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
package org.incendo.cloud.discord.discord4j;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import java.util.Optional;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.discord.immutables.StagedImmutableBuilder;

@StagedImmutableBuilder
@Value.Immutable
@API(status = API.Status.STABLE, since = "1.0.0")
public interface Discord4JInteraction {

    /**
     * Returns a new builder.
     *
     * @return the builder
     */
    static ImmutableDiscord4JInteraction.@NonNull CommandInteractionBuildStage builder() {
        return ImmutableDiscord4JInteraction.builder();
    }

    /**
     * Returns the command interaction.
     *
     * @return command interaction
     */
    @NonNull ApplicationCommandInteraction commandInteraction();

    /**
     * Returns the interaction event.
     *
     * @return interaction event
     */
    @NonNull InteractionCreateEvent interactionEvent();

    /**
     * Returns the command event. This will be empty during suggestion generation.
     *
     * @return command event
     */
    default @NonNull Optional<@NonNull ChatInputInteractionEvent> commandEvent() {
       if (this.interactionEvent() instanceof ChatInputInteractionEvent) {
           return Optional.of((ChatInputInteractionEvent) this.interactionEvent());
       }
       return Optional.empty();
    }

    @FunctionalInterface
    @API(status = API.Status.STABLE, since = "1.0.0")
    interface InteractionMapper<C> {

        /**
         * Returns a mapper that maps the interaction to itself.
         *
         * @return identity mapper
         */
        static @NonNull InteractionMapper<Discord4JInteraction> identity() {
            return interaction -> interaction;
        }

        /**
         * Maps the interaction to the custom sender.
         *
         * @param interaction interaction to map
         * @return the mapped sender
         */
        @NonNull C map(@NonNull Discord4JInteraction interaction);
    }
}
