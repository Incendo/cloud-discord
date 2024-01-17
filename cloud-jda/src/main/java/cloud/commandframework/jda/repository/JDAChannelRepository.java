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
package cloud.commandframework.jda.repository;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.legacy.parser.DiscordChannelParser;
import org.incendo.cloud.discord.legacy.repository.DiscordRepository;

/**
 * Repository for JDA {@link MessageChannel message channels}.
 */
@API(status = API.Status.INTERNAL, since = "1.0.0")
public final class JDAChannelRepository implements DiscordRepository<Guild, MessageChannel> {

    private final Guild guild;

    /**
     * Creates a new channel repository.
     *
     * @param guild guild to retrieve channels from
     */
    public JDAChannelRepository(final @NonNull Guild guild) {
        this.guild = Objects.requireNonNull(guild, "guild");
    }

    @Override
    public @NonNull MessageChannel getById(final long id) {
        try {
            final MessageChannel channel = this.guild.getTextChannelById(id);

            if (channel == null) {
                throw new DiscordChannelParser.ChannelNotFoundParseException(Long.toString(id));
            }

            return channel;
        } catch (final CompletionException e) {
            if (e.getCause().getClass().equals(ErrorResponseException.class)
                    && ((ErrorResponseException) e.getCause()).getErrorResponse() == ErrorResponse.UNKNOWN_CHANNEL) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw new DiscordChannelParser.ChannelNotFoundParseException(Long.toString(id));
            }
            throw e;
        }
    }

    @Override
    public @NonNull Collection<@NonNull TextChannel> getByName(final @NonNull String name) {
        return this.guild.getTextChannelsByName(name, true);
    }
}
