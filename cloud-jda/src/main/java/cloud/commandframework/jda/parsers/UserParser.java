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
import cloud.commandframework.jda.repository.JDAUserRepository;
import java.util.Set;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.discord.legacy.parser.DiscordParserMode;
import org.incendo.cloud.discord.legacy.parser.DiscordUserParser;
import org.incendo.cloud.discord.legacy.repository.DiscordRepository;

/**
 * Parser for {@link User}.
 *
 * @param <C> command sender type
 * @since 2.0.0
 */
@SuppressWarnings("unused")
@API(status = API.Status.STABLE, since = "2.0.0")
public final class UserParser<C> extends DiscordUserParser<C, Guild, User> {

    /**
     * Creates a new server parser.
     *
     * @param <C> command sender type
     * @param modes parser modes to use
     * @param isolationLevel isolation level to allow
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, User> userParser(
            final @NonNull Set<DiscordParserMode> modes,
            final @NonNull Isolation isolationLevel
    ) {
        return ParserDescriptor.of(new UserParser<>(modes, isolationLevel), User.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #userParser} as the parser.
     *
     * @param <C> the command sender type
     * @param modes parser modes to use
     * @param isolationLevel isolation level to allow
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, User> userComponent(
            final @NonNull Set<DiscordParserMode> modes,
            final @NonNull Isolation isolationLevel
    ) {
        return CommandComponent.<C, User>builder().parser(userParser(modes, isolationLevel));
    }

    /**
     * Construct a new user parser.
     *
     * @param modes parser modes to use
     * @param isolationLevel isolation level to allow
     */
    public UserParser(
            final @NonNull Set<DiscordParserMode> modes,
            final @NonNull Isolation isolationLevel
    ) {
        super(modes, isolationLevel);
    }

    @Override
    protected @NonNull DiscordRepository<Guild, User> repository(final @NonNull CommandContext<C> context) {
        final MessageReceivedEvent event = context.get("MessageReceivedEvent");
        return new JDAUserRepository(event.getGuild(), this.isolation());
    }

    @Override
    protected @Nullable ArgumentParseResult<User> preProcess(@NonNull final CommandContext<C> context) {
        if (!context.contains("MessageReceivedEvent")) {
            return ArgumentParseResult.failure(new IllegalStateException(
                    "MessageReceivedEvent was not in the command context."
            ));
        }
        return null;
    }
}
