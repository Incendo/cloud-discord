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
package org.incendo.cloud.discord.slash;

import cloud.commandframework.Command;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.incendo.cloud.discord.immutables.StagedImmutableBuilder;

/**
 * Represents a Discord option.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public interface DiscordOption<C> extends Named {

    /**
     * Returns the option name.
     *
     * <p>Must be between 1 and 32 characters.</p>
     *
     * @return the name
     */
    @Override
    @NonNull String name();

    /**
     * Returns the option description.
     *
     * <p>Must be between 1 and 100 characters.</p>
     *
     * @return the description
     */
    @NonNull String description();

    /**
     * Returns the type of the option.
     *
     * @return option type
     */
    @NonNull DiscordOptionType<?> type();

    /**
     * Returns the associated command.
     *
     * @return the command, or {@code null}
     */
    @Nullable Command<C> command();


    @SuppressWarnings("immutables:subtype")
    @StagedImmutableBuilder
    @Value.Immutable
    interface SubCommand<C> extends DiscordOption<C> {

        @Value.Derived
        @Override
        default @NonNull DiscordOptionType<?> type() {
            if (!this.options().isEmpty() && this.options().get(0) instanceof SubCommand) {
                return DiscordOptionType.SUB_COMMAND_GROUP;
            }
            return DiscordOptionType.SUB_COMMAND;
        }

        /**
         * Returns the child options.
         *
         * @return the options
         */
        @NonNull List<@NonNull DiscordOption<C>> options();
    }

    @SuppressWarnings("immutables:subtype")
    @StagedImmutableBuilder
    @Value.Immutable
    interface Variable<C> extends DiscordOption<C> {

        /**
         * Whether the option is required.
         *
         * @return whether the option is required
         */
        boolean required();

        /**
         * Whether the autocomplete interaction is enabled for this option.
         *
         * @return whether autocomplete is enabled
         */
        boolean autocomplete();

        /**
         * Returns the choices.
         *
         * <p>This is only relevant for STRING, INTEGER &amp; NUMBER options.</p>
         *
         * @return the choices
         */
        @NonNull List<@NonNull DiscordOptionChoice<?>> choices();

        /**
         * Returns the range, if applicable.
         *
         * @return the range, or {@code null}
         */
        default @Nullable Range range() {
            return null;
        }
    }
}
