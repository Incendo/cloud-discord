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
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.legacy.parser.DiscordMemberParser;
import org.incendo.cloud.discord.legacy.repository.DiscordRepository;

/**
 * Repository for JDA {@link Member members}.
 */
@API(status = API.Status.INTERNAL, since = "1.0.0")
public final class JDAMemberRepository implements DiscordRepository<Guild, Member> {

    private final Guild guild;

    /**
     * Creates a new member repository.
     *
     * @param guild guild to retrieve members from
     */
    public JDAMemberRepository(final @NonNull Guild guild) {
        this.guild = Objects.requireNonNull(guild, "guild");
    }

    @Override
    public @NonNull Member getById(final long id) {
        try {
            Member guildMember = this.guild.getMemberById(id);

            if (guildMember == null) { // fallback if member is not cached
                guildMember = this.guild.retrieveMemberById(id).complete();
            }

            if (guildMember == null) {
                throw new DiscordMemberParser.MemberNotFoundParseException(Long.toString(id));
            } else {
                return guildMember;
            }
        } catch (final CompletionException e) {
            if (e.getCause().getClass().equals(ErrorResponseException.class)
                    && ((ErrorResponseException) e.getCause()).getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw new DiscordMemberParser.MemberNotFoundParseException(Long.toString(id));
            }
            throw e;
        }
    }

    @Override
    public @NonNull Collection<? extends @NonNull Member> getByName(final @NonNull String name) {
        return this.guild.getMembers()
                .stream()
                .filter(member -> member.getEffectiveName().toLowerCase().startsWith(name))
                .collect(Collectors.toList());
    }
}
