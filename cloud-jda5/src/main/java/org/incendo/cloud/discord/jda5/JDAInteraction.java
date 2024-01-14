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
package org.incendo.cloud.discord.jda5;

import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.incendo.cloud.discord.immutables.StagedImmutableBuilder;

@StagedImmutableBuilder
@Value.Immutable
@API(status = API.Status.STABLE, since = "1.0.0")
public interface JDAInteraction {

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    static ImmutableJDAInteraction.@NonNull UserBuildStage builder() {
        return ImmutableJDAInteraction.builder();
    }

    /**
     * Returns the user that triggered the interaction.
     *
     * @return the user
     */
    @NonNull User user();

    /**
     * Returns the guild that triggered the interaction, if the interaction took place in a guild.
     *
     * @return the guild, or {@code null}
     */
    @Nullable Guild guild();

    /**
     * Returns the interaction event that triggered the command, if relevant.
     *
     * @return the interaction event, or {@code null}
     */
    @Nullable GenericCommandInteractionEvent interactionEvent();

    /**
     * Returns the reply callback, if relevant.
     *
     * @return the reply callback, or {@code null}
     */
    @Nullable IReplyCallback replyCallback();

    /**
     * Returns the raw JDA option mappings.
     *
     * @return option mappings
     */
    @NonNull List<@NonNull OptionMapping> optionMappings();

    /**
     * Returns the option mapping with the given {@code key}, if it exists.
     *
     * @param key mapping key
     * @return the mapping
     */
    default @NonNull Optional<@NonNull OptionMapping> getOptionMapping(final @NonNull String key) {
        return this.optionMappings().stream().filter(mapping -> mapping.getName().equalsIgnoreCase(key)).findFirst();
    }


    /**
     * Maps between {@link JDAInteraction} and {@link C}.
     *
     * @param <C> command sender type
     * @since 1.0.0
     */
    @FunctionalInterface
    @API(status = API.Status.STABLE, since = "1.0.0")
    interface InteractionMapper<C> {

        /**
         * Returns a mapper that maps the interaction to itself.
         *
         * @return identity mapper
         */
        static @NonNull InteractionMapper<JDAInteraction> identity() {
            return interaction -> interaction;
        }

        /**
         * Maps the interaction to the custom sender.
         *
         * @param interaction interaction to map
         * @return the mapped sender
         */
        @NonNull C map(@NonNull JDAInteraction interaction);
    }
}
