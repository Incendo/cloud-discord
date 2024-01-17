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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.repository.JDAMemberRepository;
import java.util.Set;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.discord.legacy.parser.DiscordMemberParser;
import org.incendo.cloud.discord.legacy.parser.DiscordParserMode;
import org.incendo.cloud.discord.legacy.repository.DiscordRepository;

/**
 * Parser for {@link Member}.
 *
 * @param <C> command sender type
 * @since 2.0.0
 */
@SuppressWarnings("unused")
@API(status = API.Status.STABLE, since = "2.0.0")
public final class MemberParser<C> extends DiscordMemberParser<C, Guild, Member> {

    /**
     * Creates a new member parser.
     *
     * @param <C> command sender type
     * @param modes parser modes to use
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Member> memberParser(final @NonNull Set<DiscordParserMode> modes) {
        return ParserDescriptor.of(new MemberParser<>(modes), Member.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #memberParser} as the parser.
     *
     * @param <C> the command sender type
     * @param modes parser modes to use
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Member> roleComponent(final @NonNull Set<DiscordParserMode> modes) {
        return CommandComponent.<C, Member>builder().parser(memberParser(modes));
    }

    /**
     * Constructs a new parser for {@link Member}.
     *
     * @param modes parsing modes to use when parsing
     * @throws IllegalArgumentException if no parsing modes were provided
     */
    public MemberParser(final @NonNull Set<DiscordParserMode> modes) {
        super(modes);
    }

    @Override
    protected @NonNull DiscordRepository<Guild, Member> repository(final @NonNull CommandContext<C> context) {
        final MessageReceivedEvent event = context.get("MessageReceivedEvent");
        return new JDAMemberRepository(event.getGuild());
    }

    @Override
    protected @Nullable ArgumentParseResult<Member> preProcess(final @NonNull CommandContext<C> context) {
        if (!context.contains("MessageReceivedEvent")) {
            return ArgumentParseResult.failure(new IllegalStateException(
                    "MessageReceivedEvent was not in the command context."
            ));
        }

        final MessageReceivedEvent event = context.get("MessageReceivedEvent");
        if (!event.isFromGuild()) {
            return ArgumentParseResult.failure(new CommandNotFromGuildException());
        }

        return null;
    }
}
