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

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@FunctionalInterface
@API(status = API.Status.STABLE, since = "1.0.0")
public interface Discord4JCommandExecutionHandler<C> extends CommandExecutionHandler.FutureCommandExecutionHandler<C> {

    /**
     * Returns a new execution handler that wraps the given {@code function}.
     *
     * @param <C>      command sender type
     * @param function function that consumes the {@link CommandContext} and returns a {@link Publisher} that publishes the
     *                 result of the interaction
     * @return the command execution handler
     */
    static <C> @NonNull CommandExecutionHandler<C> reactiveHandler(
            final @NonNull Function<@NonNull CommandContext<C>, @NonNull Publisher<?>> function
    ) {
        return new Discord4JCommandExecutionHandler<C>() {

            @Override
            public @NonNull Publisher<?> executeReactively(final @NonNull CommandContext<C> commandContext) {
                return function.apply(commandContext);
            }
        };
    }

    @Override
    default CompletableFuture<@Nullable Void> executeFuture(final @NonNull CommandContext<C> commandContext) {
        return Mono.from(this.executeReactively(commandContext)).then().toFuture();
    }

    /**
     * Executes the command and returns a publisher that publishes the result.
     *
     * @param commandContext command context
     * @return the publisher
     */
    @NonNull Publisher<?> executeReactively(@NonNull CommandContext<C> commandContext);
}
