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
package org.incendo.cloud.discord.jda6;

import io.leangen.geantyref.TypeToken;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.slash.DiscordOptionType;

/**
 * Extension of {@link DiscordOptionType} for JDA-specific classes.
 *
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public final class JDAOptionType {

    public static final @NonNull DiscordOptionType<User> USER = DiscordOptionType.of(
            "USER",
            6,
            TypeToken.get(User.class)
    );
    public static final @NonNull DiscordOptionType<Channel> CHANNEL = DiscordOptionType.of(
            "CHANNEL",
            7,
            TypeToken.get(Channel.class)
    );
    public static final @NonNull DiscordOptionType<Role> ROLE = DiscordOptionType.of(
            "ROLE",
            8,
            TypeToken.get(Role.class)
    );
    public static final @NonNull DiscordOptionType<IMentionable> MENTIONABLE = DiscordOptionType.of(
            "MENTIONABLE",
            9,
            TypeToken.get(IMentionable.class)
    );
    public static final @NonNull DiscordOptionType<Message.Attachment> ATTACHMENT = DiscordOptionType.of(
            "ATTACHMENT",
            11,
            TypeToken.get(Message.Attachment.class)
    );

    public static final Collection<@NonNull DiscordOptionType<?>> JDA_TYPES = Collections.unmodifiableCollection(
            Arrays.asList(USER, CHANNEL, ROLE, MENTIONABLE, ATTACHMENT)
    );

    private JDAOptionType() {
    }
}
