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

import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;

/**
 * Hack that enables Discord parsers to return nullable values when parsing.
 *
 * <p>The parser will return a successful response wrapping the {@link #SENTINEL_VALUE}. This value cannot be extracted from
 * the command context, as that will lead to a {@link ClassCastException} at runtime.</p>
 *
 * @param <C> command sender type
 * @param <T> parser type
 * @since 1.0.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@API(status = API.Status.INTERNAL, since = "1.0.0")
public abstract class NullableParser<C, T> implements ArgumentParser.FutureArgumentParser<C, T> {

    public static final Object SENTINEL_VALUE = new Object();

    /**
     * Attempts to parse the object, returning {@code null} if parsing was successful but did not map to a currently
     * existing value.
     *
     * @param commandContext command context
     * @param commandInput   command input
     * @return future that completes with a result or {@code null}
     */
    public abstract @NonNull CompletableFuture<@Nullable ArgumentParseResult<T>> parseNullable(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull CommandInput commandInput
    );

    @Override
    public final @NonNull CompletableFuture parseFuture(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        return this.parseNullable(commandContext, commandInput).thenApply(result -> {
            if (result != null) {
                return result;
            }
            return ArgumentParseResult.success(SENTINEL_VALUE);
        });
    }
}
