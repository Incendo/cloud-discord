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
import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.parser.ParserDescriptor;

/**
 * Registry that stores {@link DiscordOptionType}-{@link ParserDescriptor} mappings.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public interface OptionRegistry<C> {

    /**
     * Registers a mapping between the given {@code optionType} and the given {@code parser}.
     *
     * @param optionType discord option type
     * @param parser     cloud parser
     * @return {@code this}
     */
    @This @NonNull OptionRegistry<C> registerMapping(
            @NonNull DiscordOptionType<?> optionType,
            @NonNull ParserDescriptor<C, ?> parser
    );

    /**
     * Returns the option type that best corresponds to the given {@code valueType}.
     *
     * @param valueType type to get the option for
     * @return the best matching option, using {@link DiscordOptionType#STRING} as the fallback
     */
    @NonNull DiscordOptionType<?> getOption(@NonNull TypeToken<?> valueType);

    /**
     * Returns an unmodifiable view of the recognized option types.
     *
     * @return option types
     */
    @NonNull Collection<@NonNull DiscordOptionType<?>> optionTypes();

    /**
     * Returns the option type with the given {@code value}, if it exists.
     *
     * @param value value to get the option by
     * @return the option type, or {@code null}
     */
    default @Nullable DiscordOptionType<?> getByValue(final int value) {
        return this.optionTypes()
                .stream()
                .filter(option -> option.value() == value)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the option type with the given {@code name}, if it exists.
     *
     * @param name name to get the option by
     * @return the option type, or {@code null}
     */
    default @Nullable DiscordOptionType<?> getByName(final @NonNull String name) {
        return this.optionTypes()
                .stream()
                .filter(option -> option.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
