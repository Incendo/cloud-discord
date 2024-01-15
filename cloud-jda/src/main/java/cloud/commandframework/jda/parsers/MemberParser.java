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
package cloud.commandframework.jda.parsers;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parser for {@link Member}.
 *
 * @param <C> command sender type
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public final class MemberParser<C> implements ArgumentParser<C, Member> {

    private final Set<ParserMode> modes;

    /**
     * Constructs a new parser for {@link Member}.
     *
     * @param modes parsing modes to use when parsing
     * @throws IllegalArgumentException if no parsing modes were provided
     */
    public MemberParser(final @NonNull Set<ParserMode> modes) {
        if (modes.isEmpty()) {
            throw new IllegalArgumentException("At least one parsing mode is required");
        }
        this.modes = modes;
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Member> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (!commandContext.contains("MessageReceivedEvent")) {
            return ArgumentParseResult.failure(new IllegalStateException(
                    "MessageReceivedEvent was not in the command context."
            ));
        }

        final MessageReceivedEvent event = commandContext.get("MessageReceivedEvent");

        if (!event.isFromGuild()) {
            return ArgumentParseResult.failure(new CommandNotFromGuildException());
        }

        Exception exception = null;
        final String input = commandInput.readString();

        if (this.modes.contains(ParserMode.MENTION)) {
            if (input.startsWith("<@") && input.endsWith(">")) {
                final String id;
                if (input.startsWith("<@!")) {
                    id = input.substring(3, input.length() - 1);
                } else {
                    id = input.substring(2, input.length() - 1);
                }

                try {
                    return this.memberFromId(event, input, Long.parseLong(id));
                } catch (final MemberNotFoundParseException | NumberFormatException e) {
                    exception = e;
                }
            } else {
                exception = new IllegalArgumentException(
                        String.format("Input '%s' is not a member mention.", input)
                );
            }
        }

        if (this.modes.contains(ParserMode.ID)) {
            try {
                return this.memberFromId(event, input, Long.parseLong(input));
            } catch (final MemberNotFoundParseException | NumberFormatException e) {
                exception = e;
            }
        }

        if (this.modes.contains(ParserMode.NAME)) {
            final List<Member> members;

            if (event.getAuthor().getName().equalsIgnoreCase(input)) {
                members = Collections.singletonList(event.getMember());
            } else {
                members = event.getGuild().getMembers()
                        .stream()
                        .filter(member -> member.getEffectiveName().toLowerCase().startsWith(input))
                        .collect(Collectors.toList());
            }

            if (members.isEmpty()) {
                exception = new MemberNotFoundParseException(input);
            } else if (members.size() > 1) {
                exception = new TooManyMembersFoundParseException(input);
            } else {
                return ArgumentParseResult.success(members.get(0));
            }
        }

        assert exception != null;
        return ArgumentParseResult.failure(exception);
    }

    private @NonNull ArgumentParseResult<Member> memberFromId(
            final @NonNull MessageReceivedEvent event,
            final @NonNull String input,
            final @NonNull Long id
    )
            throws MemberNotFoundParseException, NumberFormatException {
        final Guild guild = event.getGuild();

        final Member member;
        if (event.getAuthor().getIdLong() == id) {
            member = event.getMember();
        } else {
            Member guildMember = guild.getMemberById(id);

            if (guildMember == null) { // fallback if member is not cached
                guildMember = guild.retrieveMemberById(id).complete();
            }
            member = guildMember;
        }

        if (member == null) {
            throw new MemberNotFoundParseException(input);
        } else {
            return ArgumentParseResult.success(member);
        }
    }


    public enum ParserMode {
        MENTION,
        ID,
        NAME
    }


    public static class MemberParseException extends IllegalArgumentException {

        private final String input;

        /**
         * Constructs a new member parse exception.
         *
         * @param input string input
         */
        public MemberParseException(final @NonNull String input) {
            this.input = input;
        }

        /**
         * Returns the user's input.
         *
         * @return user's input
         */
        public final @NonNull String input() {
            return this.input;
        }
    }


    public static final class TooManyMembersFoundParseException extends MemberParseException {

        /**
         * Constructs a new member parse exception.
         *
         * @param input string input
         */
        public TooManyMembersFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public String getMessage() {
            return String.format("Too many users found for '%s'.", this.input());
        }
    }


    public static final class MemberNotFoundParseException extends MemberParseException {

        /**
         * Constructs a new member parse exception.
         *
         * @param input string input
         */
        public MemberNotFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public String getMessage() {
            return String.format("User not found for '%s'.", this.input());
        }
    }


    public static final class CommandNotFromGuildException extends IllegalArgumentException {

        /**
         * Constructs a new command not from guild exception.
         */
        public CommandNotFromGuildException() {
            super("Command must be executed in a guild.");
        }
    }
}
