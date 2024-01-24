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

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.discord.immutables.ImmutableImpl;
import org.incendo.cloud.suggestion.Suggestion;

/**
 * Represents a Discord option choice.
 *
 * @param <T> option type
 * @since 1.0.0
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE, since = "1.0.0")
public interface DiscordOptionChoice<T> extends Named, Suggestion {

    /**
     * Creates a new choice.
     *
     * @param <T>   option type
     * @param name  choice name
     * @param value choice value
     * @return the choice
     */
    static <T> @NonNull DiscordOptionChoice<T> of(final @NonNull String name, final @NonNull T value) {
        return DiscordOptionChoiceImpl.of(name, value);
    }

    /**
     * Returns the choice name.
     *
     * @return the name
     */
    @Override
    @NonNull String name();

    /**
     * Returns the choice value.
     *
     * @return the value
     */
    @NonNull T value();

    @Override
    default @NonNull String suggestion() {
        return this.name();
    }

    @Override
    default @NonNull DiscordOptionChoice<T> withSuggestion(final @NonNull String suggestion) {
        return DiscordOptionChoiceImpl.copyOf(this).withSuggestion(suggestion);
    }
}
