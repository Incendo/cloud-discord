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
import cloud.commandframework.jda.repository.JDAChannelRepository;
import java.util.Set;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.discord.legacy.parser.DiscordChannelParser;
import org.incendo.cloud.discord.legacy.parser.DiscordParserMode;
import org.incendo.cloud.discord.legacy.repository.DiscordRepository;

/**
 * Parser {@link MessageChannel}
 *
 * @param <C> command sender type
 */
@SuppressWarnings("unused")
@API(status = API.Status.STABLE, since = "2.0.0")
public final class ChannelParser<C> extends DiscordChannelParser<C, Guild, MessageChannel> {

    /**
     * Creates a new channel parser.
     *
     * @param <C> command sender type
     * @param modes parser modes to use
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, MessageChannel> channelParser(final @NonNull Set<DiscordParserMode> modes) {
        return ParserDescriptor.of(new ChannelParser<>(modes), MessageChannel.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #channelParser} as the parser.
     *
     * @param <C> the command sender type
     * @param modes parser modes to use
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, MessageChannel> channelComponent(
            final @NonNull Set<DiscordParserMode> modes
    ) {
        return CommandComponent.<C, MessageChannel>builder().parser(channelParser(modes));
    }

    /**
     * Construct a new channel parser.
     *
     * @param modes parser modes to use
     */
    public ChannelParser(final @NonNull Set<DiscordParserMode> modes) {
        super(modes);
    }

    @Override
    protected @Nullable ArgumentParseResult<MessageChannel> preProcess(final @NonNull CommandContext<C> context) {
        if (!context.contains("MessageReceivedEvent")) {
            return ArgumentParseResult.failure(new IllegalStateException(
                    "MessageReceivedEvent was not in the command context."
            ));
        }

        final MessageReceivedEvent event = context.get("MessageReceivedEvent");
        if (!event.isFromGuild()) {
            return ArgumentParseResult.failure(new IllegalArgumentException("Channel arguments can only be parsed in guilds"));
        }

        return null;
    }

    @Override
    protected @NonNull DiscordRepository<Guild, MessageChannel> repository(final @NonNull CommandContext<C> context) {
        final MessageReceivedEvent event = context.get("MessageReceivedEvent");
        return new JDAChannelRepository(event.getGuild());
    }
}
