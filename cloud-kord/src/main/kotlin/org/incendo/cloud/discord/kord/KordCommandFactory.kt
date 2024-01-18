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

import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import org.apiguardian.api.API
import org.incendo.cloud.discord.slash.CommandScopePredicate

/**
 * Factory that creates commands.
 *
 * @param C command sender type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public interface KordCommandFactory<C> {

    /**
     * Predicate that determines whether a command is applicable in a given scope.
     */
    public var commandScopePredicate: CommandScopePredicate<C>

    /**
     * Creates the commands for the given [guild].
     */
    public suspend fun createGuildCommands(guild: Guild)

    /**
     * Deletes the commands from the given [guild].
     */
    public suspend fun deleteGuildCommands(guild: Guild)

    /**
     * Creates global commands using the given [kord] instance.
     */
    public suspend fun createGlobalCommands(kord: Kord)

    /**
     * Deletes global commands from the given [kord] instance.
     */
    public suspend fun deleteGlobalCommands(kord: Kord)
}
