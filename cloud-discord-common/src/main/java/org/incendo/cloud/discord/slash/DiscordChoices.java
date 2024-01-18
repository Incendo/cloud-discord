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

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.discord.immutables.ImmutableImpl;

@ImmutableImpl
@Value.Immutable
@SuppressWarnings("varargs")
@API(status = API.Status.STABLE, since = "1.0.0 ")
public interface DiscordChoices<C, T> extends DiscordChoiceProvider<C, T> {

    /**
     * Creates a {@link DiscordChoices} instance from the given {@code choices}.
     *
     * @param <C>     command sender type
     * @param <T>     choice type
     * @param choices choices
     * @return the created instance
     */
    static <C, T> @NonNull DiscordChoices<C, T> choices(final @NonNull Collection<@NonNull DiscordOptionChoice<T>> choices) {
        return DiscordChoicesImpl.of(choices);
    }

    /**
     * Creates a {@link DiscordChoices} instance from the given {@code choices}.
     *
     * @param <C>     command sender type
     * @param <T>     choice type
     * @param choices choices
     * @return the created instance
     */
    @SafeVarargs
    static <C, T> @NonNull DiscordChoices<C, T> choices(final @NonNull DiscordOptionChoice<T> @NonNull... choices) {
        return DiscordChoicesImpl.of(Arrays.asList(choices));
    }

    /**
     * Creates a {@link DiscordChoices} instance from the given {@code choices}.
     *
     * @param <C> command sender type
     * @param choices choices
     * @return the created instance
     */
    static <C> @NonNull DiscordChoices<C, Integer> integers(final @NonNull Iterable<@NonNull Integer> choices) {
        return DiscordChoicesImpl.of(
                StreamSupport.stream(choices.spliterator(), false)
                        .map(integer -> DiscordOptionChoice.of(Integer.toString(integer), integer))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Creates a {@link DiscordChoices} instance from the given {@code choices}.
     *
     * @param <C> command sender type
     * @param choices choices
     * @return the created instance
     */
    static <C> @NonNull DiscordChoices<C, Integer> integers(final int @NonNull... choices) {
        return DiscordChoicesImpl.of(
                Arrays.stream(choices)
                        .mapToObj(integer -> DiscordOptionChoice.of(Integer.toString(integer), integer))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Creates a {@link DiscordChoices} instance from the given {@code choices}.
     *
     * @param <C> command sender type
     * @param choices choices
     * @return the created instance
     */
    static <C> @NonNull DiscordChoices<C, Double> doubles(final @NonNull Iterable<@NonNull Double> choices) {
        return DiscordChoicesImpl.of(
                StreamSupport.stream(choices.spliterator(), false)
                        .map(number -> DiscordOptionChoice.of(Double.toString(number), number))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Creates a {@link DiscordChoices} instance from the given {@code choices}.
     *
     * @param <C> command sender type
     * @param choices choices
     * @return the created instance
     */
    static <C> @NonNull DiscordChoices<C, Double> doubles(final double @NonNull... choices) {
        return DiscordChoicesImpl.of(
                Arrays.stream(choices)
                        .mapToObj(number -> DiscordOptionChoice.of(Double.toString(number), number))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Creates a {@link DiscordChoices} instance from the given {@code choices}.
     *
     * @param <C> command sender type
     * @param choices choices
     * @return the created instance
     */
    static <C> @NonNull DiscordChoices<C, String> strings(final @NonNull Iterable<@NonNull String> choices) {
        return DiscordChoicesImpl.of(
                StreamSupport.stream(choices.spliterator(), false)
                        .map(string -> DiscordOptionChoice.of(string, string))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Creates a {@link DiscordChoices} instance from the given {@code choices}.
     *
     * @param <C> command sender type
     * @param choices choices
     * @return the created instance
     */
    static <C> @NonNull DiscordChoices<C, String> strings(final @NonNull String @NonNull... choices) {
        return DiscordChoicesImpl.of(
                Arrays.stream(choices)
                        .map(string -> DiscordOptionChoice.of(string, string))
                        .collect(Collectors.toList())
        );
    }

    @Override
    @NonNull Collection<@NonNull DiscordOptionChoice<T>> choices();
}
