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

import io.leangen.geantyref.TypeToken;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.discord.immutables.ImmutableImpl;

/**
 * Represents a Discord option type.
 *
 * @param <T> option type
 * @since 1.0.0
 */
@Value.Immutable
@ImmutableImpl
@API(status = API.Status.STABLE, since = "1.0.0")
public interface DiscordOptionType<@NonNull T> extends Named {

    @NonNull DiscordOptionType<DiscordOption<?>> SUB_COMMAND = of("SUB_COMMAND", 1, new TypeToken<DiscordOption<?>>() {
    });
    @NonNull DiscordOptionType<List<DiscordOption<?>>> SUB_COMMAND_GROUP = of("SUB_COMMAND_GROUP", 2,
            new TypeToken<List<DiscordOption<?>>>() {
            }
    );
    @NonNull DiscordOptionType<String> STRING = of("STRING", 3, TypeToken.get(String.class));
    @NonNull DiscordOptionType<Integer> INTEGER = of("INTEGER", 4, TypeToken.get(Integer.class));
    @NonNull DiscordOptionType<Boolean> BOOLEAN = of("BOOLEAN", 5, TypeToken.get(Boolean.class));
    @NonNull DiscordOptionType<Double> NUMBER = of("NUMBER", 10, TypeToken.get(Double.class));
    // Non-generic types must be implemented in the platform modules.

    @NonNull Collection<@NonNull DiscordOptionType<?>> AUTOCOMPLETE = Collections.unmodifiableCollection(
            Arrays.asList(STRING, INTEGER, NUMBER)
    );

    /**
     * Creates a new option instance.
     *
     * @param <T>   type represented by the option
     * @param name  option name
     * @param value option value
     * @param type  option type
     * @return the option
     */
    static <T> @NonNull DiscordOptionType<T> of(
            final @NonNull String name,
            final int value,
            final TypeToken<T> type
    ) {
        return DiscordOptionTypeImpl.of(name, value, type);
    }

    /**
     * Returns the name of the option type.
     *
     * @return option type name
     */
    @Override
    @NonNull String name();

    /**
     * Returns the option value.
     *
     * @return option value
     */
    int value();

    /**
     * Returns the option type.
     *
     * @return option type
     */
    @NonNull TypeToken<T> type();
}
