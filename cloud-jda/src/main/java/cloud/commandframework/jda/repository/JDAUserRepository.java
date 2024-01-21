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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.legacy.parser.DiscordUserParser;
import org.incendo.cloud.discord.legacy.repository.DiscordRepository;

/**
 * Repository for JDA {@link User users}.
 */
@API(status = API.Status.INTERNAL, since = "1.0.0")
public final class JDAUserRepository implements DiscordRepository<Guild, User> {

    private final Guild guild;
    private final DiscordUserParser.Isolation isolation;

    /**
     * Creates a new User repository.
     *
     * @param guild     guild to retrieve users from
     * @param isolation isolation
     */
    public JDAUserRepository(
            final @NonNull Guild guild,
            final DiscordUserParser.@NonNull Isolation isolation
    ) {
        this.guild = Objects.requireNonNull(guild, "guild");
        this.isolation = Objects.requireNonNull(isolation, "isolation");
    }

    @Override
    public @NonNull User getById(final long id) {
        User user = null;

        if (this.isolation == DiscordUserParser.Isolation.GLOBAL) {
            User globalUser = this.guild.getJDA().getUserById(id);

            if (globalUser == null) { // fallback if User is not cached
                globalUser = this.guild.getJDA().retrieveUserById(id).complete();
            }

            user = globalUser;
        } else {
            Member member = this.guild.getMemberById(id);

            try {
                if (member == null) { // fallback if member is not cached
                    member = this.guild.retrieveMemberById(id).complete();
                }

                user = member.getUser();
            } catch (final CompletionException e) {
                if (e.getCause().getClass().equals(ErrorResponseException.class)
                        && ((ErrorResponseException) e.getCause()).getErrorResponse() == ErrorResponse.UNKNOWN_USER) {
                    //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                    throw new DiscordUserParser.UserNotFoundParseException(Long.toString(id));
                }
                throw e;
            }
        }

        if (user == null) {
            throw new DiscordUserParser.UserNotFoundParseException(Long.toString(id));
        }
        return user;
    }

    @Override
    public @NonNull Collection<? extends @NonNull User> getByName(final @NonNull String name) {
        if (this.isolation == DiscordUserParser.Isolation.GLOBAL) {
            return this.guild.getJDA().getUsersByName(name, true);
        }

        return this.guild.getMembers()
                .stream()
                .filter(member -> member.getEffectiveName().toLowerCase().startsWith(name))
                .map(Member::getUser)
                .collect(Collectors.toList());
    }
}
