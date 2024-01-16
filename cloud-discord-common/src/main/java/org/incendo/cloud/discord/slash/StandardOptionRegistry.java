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

import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.standard.BooleanParser;
import cloud.commandframework.arguments.standard.DoubleParser;
import cloud.commandframework.arguments.standard.IntegerParser;
import cloud.commandframework.arguments.standard.StringParser;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * Standard implementation of {@link OptionRegistry}.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@API(status = API.Status.INTERNAL, since = "1.0.0")
public final class StandardOptionRegistry<C> implements OptionRegistry<C> {

    private final Map<DiscordOptionType<?>, ParserDescriptor<C, ?>> parserMap = new HashMap<>();
    private final Map<Class<?>, DiscordOptionType<?>> optionMap = new HashMap<>();

    /**
     * Creates a new standard option registry.
     */
    public StandardOptionRegistry() {
        this.registerMapping(DiscordOptionType.STRING, StringParser.stringParser())
                .registerMapping(DiscordOptionType.INTEGER, IntegerParser.integerParser())
                .registerMapping(DiscordOptionType.BOOLEAN, BooleanParser.booleanParser())
                .registerMapping(DiscordOptionType.NUMBER, DoubleParser.doubleParser());
    }

    @Override
    public @This @NonNull OptionRegistry<C> registerMapping(
            final @NonNull DiscordOptionType<?> optionType,
            final @NonNull ParserDescriptor<C, ?> parser
    ) {
        Objects.requireNonNull(optionType, "optionType");
        Objects.requireNonNull(parser, "parser");

        this.parserMap.put(optionType, parser);
        this.optionMap.put(GenericTypeReflector.erase(parser.valueType().getType()), optionType);
        return this;
    }

    @Override
    public @NonNull DiscordOptionType<?> getOption(final @NonNull TypeToken<?> valueType) {
        Objects.requireNonNull(valueType, "valueType");

        return this.optionMap.getOrDefault(
                GenericTypeReflector.erase(GenericTypeReflector.box(valueType.getType())),
                DiscordOptionType.STRING
        );
    }

    @Override
    public @NonNull Collection<@NonNull DiscordOptionType<?>> optionTypes() {
        return Collections.unmodifiableCollection(this.parserMap.keySet());
    }
}
