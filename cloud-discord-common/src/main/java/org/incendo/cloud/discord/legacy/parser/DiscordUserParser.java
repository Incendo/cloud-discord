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
 * Parser for Discord users.
 *
 * @param <C> command sender type
 * @param <G> guild type
 * @param <T> user type
 * @since 1.0.0
 */
@API(status = API.Status.INTERNAL, since = "1.0.0")
public abstract class DiscordUserParser<C, G, T> extends MentionableDiscordParser<C, G, T> {

    private final Isolation isolation;

    protected DiscordUserParser(
            final @NonNull Set<@NonNull DiscordParserMode> modes,
            final @NonNull Isolation isolation
    ) {
        super(modes);
        this.isolation = Objects.requireNonNull(isolation, "isolation");
    }

    /**
     * Returns the isolation.
     *
     * @return the isolation
     */
    public @NonNull Isolation isolation() {
        return this.isolation;
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
                final String id;
                if (input.startsWith("<@!")) {
                    id = input.substring(3, input.length() - 1);
                } else {
                    id = input.substring(2, input.length() - 1);
                }

                try {
                    final T result = repository.getById(id);
                    if (result != null) {
                        return ArgumentParseResult.success(result);
                    }
                } catch (final UserNotFoundParseException | NumberFormatException e) {
                    exception = e;
                }
            } else {
                exception = new IllegalArgumentException(String.format("Input '%s' is not a User mention.", input));
            }
        }

        if (this.modes().contains(DiscordParserMode.ID)) {
            try {
                final T result = repository.getById(input);
                if (result != null) {
                    return ArgumentParseResult.success(result);
                }
            } catch (final UserNotFoundParseException | NumberFormatException e) {
                exception = e;
            }
        }

        if (this.modes().contains(DiscordParserMode.NAME)) {
            final Collection<? extends T> users = repository.getByName(input);

            if (users.isEmpty()) {
                exception = new UserNotFoundParseException(input);
            } else if (users.size() > 1) {
                exception = new TooManyUsersFoundParseException(input);
            } else {
                return ArgumentParseResult.success(users.stream().findFirst().get());
            }
        }

        return ArgumentParseResult.failure(Objects.requireNonNull(exception, "exception"));
    }


    public enum Isolation {
        GLOBAL,
        GUILD
    }


    public static class UserParseException extends IllegalArgumentException {

        private final String input;

        /**
         * Construct a new user parse exception
         *
         * @param input String input
         */
        public UserParseException(final @NonNull String input) {
            this.input = input;
        }

        /**
         * Get the users input
         *
         * @return Users input
         */
        public final @NonNull String input() {
            return this.input;
        }
    }


    public static final class TooManyUsersFoundParseException extends UserParseException {


        /**
         * Construct a new user parse exception
         *
         * @param input String input
         */
        public TooManyUsersFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("Too many users found for '%s'.", input());
        }
    }


    public static final class UserNotFoundParseException extends UserParseException {


        /**
         * Construct a new user parse exception
         *
         * @param input String input
         */
        public UserNotFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("User not found for '%s'.", input());
        }
    }
}
