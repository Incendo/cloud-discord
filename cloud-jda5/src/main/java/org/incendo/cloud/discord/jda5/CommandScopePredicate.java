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

import cloud.commandframework.internal.CommandNode;
import java.util.function.BiPredicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.slash.CommandScope;

/**
 * Predicate that determines whether a command scope should receive a certain command.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "1.0.0")
public interface CommandScopePredicate<C> extends BiPredicate<CommandNode<C>, CommandScope<C>> {

    /**
     * Returns a predicate that always returns {@code true}.
     *
     * @param <C> command sender type
     * @return the predicate
     */
    static <C> @NonNull CommandScopePredicate<C> alwaysTrue() {
        return (node, scope) -> true;
    }

    /**
     * Returns whether the {@link CommandNode} should be registered as a command for the given {@link CommandScope}.
     *
     * @param node  command node that is being registered, this is always a root command node
     * @param scope scope that the command is being registered to
     * @return {@code true} if the scope should receive the command, {@code false} if not
     */
    @Override
    boolean test(@NonNull CommandNode<C> node, @NonNull CommandScope<C> scope);
}
