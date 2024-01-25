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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CommandTree;
import org.incendo.cloud.internal.CommandNode;

/**
 * Processes {@link CommandNode nodes} and prepares them for mapping to Discord commands.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@API(status = API.Status.INTERNAL, since = "1.0.0")
public final class NodeProcessor<C> {

    public static final String NODE_META_SCOPE = "scope";

    private final CommandTree<C> commandTree;

    /**
     * Creates a new node processor.
     *
     * @param commandTree tree that should be processed
     */
    public NodeProcessor(final @NonNull CommandTree<C> commandTree) {
        this.commandTree = Objects.requireNonNull(commandTree, "commandTree");
    }

    /**
     * Prepares the command tree.
     */
    public void prepareTree() {
        this.commandTree.getLeavesRaw(this.commandTree.rootNode()).forEach(this::propagateRequirements);
    }

    @SuppressWarnings("unchecked")
    private void propagateRequirements(final @NonNull CommandNode<C> leafNode) {
        CommandScope<C> parentScope = (CommandScope<C>) leafNode.command().commandMeta().getOrDefault(
                CommandScope.META_COMMAND_SCOPE,
                CommandScope.global()
        );
        leafNode.nodeMeta().put(NODE_META_SCOPE, parentScope);

        List<CommandNode<C>> chain = this.getChain(leafNode);
        Collections.reverse(chain);
        chain = chain.subList(1, chain.size());

        for (final CommandNode<C> commandNode : chain) {
            final CommandScope<C> existingScope = (CommandScope<C>) commandNode.nodeMeta().get(NODE_META_SCOPE);

            CommandScope<C> scope;
            if (existingScope != null) {
                // Global scope overrides everything :)
                if (existingScope instanceof CommandScope.Global) {
                    scope = existingScope;
                } else if (existingScope instanceof CommandScope.Guilds) {
                    if (parentScope instanceof CommandScope.Guilds) {
                        scope = ((CommandScope.Guilds<C>) existingScope)
                                .withGuild(((CommandScope.Guilds<C>) parentScope).guilds());
                    } else {
                        scope = existingScope;
                    }
                } else {
                    scope = parentScope;
                }
            } else {
                scope = parentScope;
            }

            commandNode.nodeMeta().put(NODE_META_SCOPE, scope);
        }
    }

    private @NonNull List<@NonNull CommandNode<C>> getChain(final @Nullable CommandNode<C> end) {
        final List<CommandNode<C>> chain = new LinkedList<>();
        CommandNode<C> tail = end;
        while (tail != null) {
            chain.add(tail);
            tail = tail.parent();
        }
        Collections.reverse(chain);
        return chain;
    }
}
