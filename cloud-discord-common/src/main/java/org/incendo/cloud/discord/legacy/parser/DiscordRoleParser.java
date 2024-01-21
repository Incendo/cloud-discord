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
package org.incendo.cloud.discord.legacy.parser;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.legacy.repository.DiscordRepository;

/**
 * Parser for Discord roles.
 *
 * @param <C> command sender type
 * @param <G> guild type
 * @param <T> role type
 * @since 1.0.0
 */
@API(status = API.Status.INTERNAL, since = "1.0.0")
public abstract class DiscordRoleParser<C, G, T> extends MentionableDiscordParser<C, G, T> {

    protected DiscordRoleParser(final @NonNull Set<@NonNull DiscordParserMode> modes) {
        super(modes);
    }

    @Override
    public final @NonNull ArgumentParseResult<@NonNull T> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final ArgumentParseResult<T> preProcessed = this.preProcess(commandContext);
        if (preProcessed != null) {
            return preProcessed;
        }

        final String input = commandInput.readString();
        final DiscordRepository<G, T> repository = this.repository(commandContext);

        Exception exception = null;

        if (this.modes().contains(DiscordParserMode.MENTION)) {
            if (input.startsWith("<@") && input.endsWith(">")) {
                final String id = input.substring(3, input.length() - 1);

                try {
                    final T result = repository.getById(id);
                    if (result != null) {
                        return ArgumentParseResult.success(result);
                    }
                } catch (final RoleNotFoundParseException | NumberFormatException e) {
                    exception = e;
                }
            } else {
                exception = new IllegalArgumentException(String.format("Input '%s' is not a role mention.", input));
            }
        }

        if (this.modes().contains(DiscordParserMode.ID)) {
            try {
                final T result = repository.getById(input);
                if (result != null) {
                    return ArgumentParseResult.success(result);
                }
            } catch (final RoleNotFoundParseException | NumberFormatException e) {
                exception = e;
            }
        }

        if (this.modes().contains(DiscordParserMode.NAME)) {
            final Collection<? extends T> users = repository.getByName(input);

            if (users.isEmpty()) {
                exception = new RoleNotFoundParseException(input);
            } else if (users.size() > 1) {
                exception = new TooManyRolesFoundParseException(input);
            } else {
                return ArgumentParseResult.success(users.stream().findFirst().get());
            }
        }

        return ArgumentParseResult.failure(Objects.requireNonNull(exception, "exception"));
    }


    public static class RoleParseException extends IllegalArgumentException {

        private static final long serialVersionUID = -2451548379508062135L;
        private final String input;

        /**
         * Construct a new role parse exception
         *
         * @param input String input
         */
        public RoleParseException(final @NonNull String input) {
            this.input = input;
        }

        /**
         * Get the users input
         *
         * @return users input
         */
        public final @NonNull String input() {
            return this.input;
        }
    }


    public static final class TooManyRolesFoundParseException extends RoleParseException {

        private static final long serialVersionUID = -8604082973199995006L;

        /**
         * Construct a new role parse exception
         *
         * @param input String input
         */
        public TooManyRolesFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("Too many roles found for '%s'.", input());
        }
    }


    public static final class RoleNotFoundParseException extends RoleParseException {

        private static final long serialVersionUID = 7931804739792920510L;

        /**
         * Construct a new role parse exception
         *
         * @param input String input
         */
        public RoleNotFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("Role not found for '%s'.", input());
        }
    }
}
