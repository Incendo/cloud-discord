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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.key.CloudKey;

@API(status = API.Status.STABLE, since = "1.0.0")
public interface CommandScope<C> extends Command.Builder.Applicable<C> {

    CloudKey<CommandScope<?>> META_COMMAND_SCOPE = CloudKey.of(
            "cloud:command_scope",
            new TypeToken<CommandScope<?>>() {
            }
    );

    /**
     * Returns the global command scope.
     *
     * @param <C> command sender type
     * @return global scope
     */
    @SuppressWarnings("unchecked")
    static <C> @NonNull CommandScope<C> global() {
        return (CommandScope<C>) Global.GLOBAL;
    }

    /**
     * Returns a command scope for the given {@code guilds}.
     *
     * @param <C> command sender type
     * @param guilds the guilds
     * @return guild scope
     */
    static <C> @NonNull CommandScope<C> guilds(final long @NonNull... guilds) {
        return new Guilds<>(guilds);
    }

    /**
     * Returns a command scope that makes the command be active in all guilds.
     *
     * @param <C> command sender type
     * @return guild scope
     */
    static <C> @NonNull CommandScope<C> guilds() {
        return new Guilds<>(-1);
    }

    /**
     * Checks if there's any overlap between {@code this} scope and the given {@code scope}.
     *
     * @param scope other scope
     * @return {@code true} if the scopes overlap, else {@code false}
     */
    boolean overlaps(@NonNull CommandScope<C> scope);

    @Override
    default Command.@NonNull Builder<C> applyToCommandBuilder(Command.@NonNull Builder<C> builder) {
        return builder.meta(META_COMMAND_SCOPE, this);
    }

    /**
     * Makes the command globally available.
     *
     * @param <C> command sender type
     * @since 1.0.0
     */
    @API(status = API.Status.STABLE, since = "1.0.0")
    final class Global<C> implements CommandScope<C> {

        private static final Global<?> GLOBAL = new Global<>();

        private Global() {
        }

        @Override
        public boolean overlaps(final @NonNull CommandScope<C> scope) {
            return scope instanceof Global;
        }
    }

    /**
     * Makes the command available in specific guilds.
     *
     * @param <C> command sender type
     * @since 1.0.0
     */
    @API(status = API.Status.STABLE, since = "1.0.0")
    final class Guilds<C> implements CommandScope<C> {

        private final Set<Long> guilds;

        private Guilds(final @NonNull Set<@NonNull Long> guilds) {
            this.guilds = Collections.unmodifiableSet(guilds);
        }

        private Guilds(final long @NonNull... guilds) {
            this(Arrays.stream(guilds).boxed().collect(Collectors.toSet()));
        }

        private Guilds(final long guildId) {
            this.guilds = Collections.singleton(guildId);
        }

        /**
         * Returns an unmodifiable view of the guilds.
         *
         * @return the guilds
         */
        public @NonNull Set<@NonNull Long> guilds() {
            return this.guilds;
        }

        /**
         * Returns a new {@link Guilds} instance with the given {@code guildId} added.
         *
         * @param guildId guild to add
         * @return the new instance
         */
        public @NonNull Guilds<C> withGuild(final long guildId) {
            final Set<Long> guilds = new HashSet<>(this.guilds);
            guilds.add(guildId);
            return new Guilds<>(guilds);
        }

        /**
         * Returns a new {@link Guilds} instance with the given {@code guilds} added.
         *
         * @param guilds new guilds to add
         * @return the new instance
         */
        public @NonNull Guilds<C> withGuild(final Set<@NonNull Long> guilds) {
            final Set<Long> newGuilds = new HashSet<>(this.guilds);
            newGuilds.addAll(guilds);
            return new Guilds<>(newGuilds);
        }

        @Override
        public boolean overlaps(final @NonNull CommandScope<C> scope) {
            if (!(scope instanceof Guilds)) {
                return false;
            }
            final Guilds<C> guilds = (Guilds<C>) scope;
            return this.guilds.stream().anyMatch(guildId -> guilds.guilds().contains(guildId));
        }

        @Override
        public @NonNull String toString() {
            return "Guilds{"
                    + "guilds=" + this.guilds
                    + '}';
        }
    }
}
