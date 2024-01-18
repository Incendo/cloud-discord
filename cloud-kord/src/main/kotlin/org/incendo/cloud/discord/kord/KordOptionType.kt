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
package org.incendo.cloud.discord.kord

import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Entity
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import io.leangen.geantyref.TypeToken
import org.apiguardian.api.API
import org.incendo.cloud.discord.slash.DiscordOptionType

/**
 * Extension of [DiscordOptionType] for Kotlin-specific classes.
 *
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public object KordOptionType {

    public val USER: DiscordOptionType<User> = DiscordOptionType.of(
        "USER",
        6,
        TypeToken.get(User::class.java)
    )
    public val CHANNEL: DiscordOptionType<Channel> = DiscordOptionType.of(
        "CHANNEL",
        7,
        TypeToken.get(Channel::class.java)
    )
    public val ROLE: DiscordOptionType<Role> = DiscordOptionType.of(
        "ROLE",
        8,
        TypeToken.get(Role::class.java)
    )
    public val MENTIONABLE: DiscordOptionType<Entity> = DiscordOptionType.of(
        "MENTIONABLE",
        9,
        TypeToken.get(Entity::class.java)
    )
    public val ATTACHMENT: DiscordOptionType<Attachment> = DiscordOptionType.of(
        "ATTACHMENT",
        11,
        TypeToken.get(Attachment::class.java)
    )

    public val KORD_TYPES: Collection<DiscordOptionType<*>> = setOf(USER, CHANNEL, ROLE, MENTIONABLE, ATTACHMENT)
}
