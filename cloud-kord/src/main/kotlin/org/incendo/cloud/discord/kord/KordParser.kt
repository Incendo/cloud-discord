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
import dev.kord.core.entity.interaction.InteractionCommand
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.apiguardian.api.API
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.discord.slash.NullableParser
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ParserDescriptor
import java.util.concurrent.CompletableFuture

/**
 * A parser which wraps a Kord option value.
 *
 * @param C command sender type
 * @param T kord type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public data class KordParser<C : Any, T : Any> internal constructor(
    private val extract: suspend (
        name: String,
        command: InteractionCommand
    ) -> ArgumentParseResult<T>?
) : NullableParser<C, T>() {

    public companion object {

        /**
         * Returns a parser which extracts a [User].
         *
         * The parsed user may not be accessed from the context during suggestion generation.
         */
        public fun <C : Any> userParser(): ParserDescriptor<C, User> = createParser<C, User> { name, command ->
            command.users[name]?.let { ArgumentParseResult.success(it) }
        }

        /**
         * Returns a parser which extracts a [Channel].
         *
         * The parsed channel may not be accessed from the context during suggestion generation.
         */
        public fun <C : Any> channelParser(): ParserDescriptor<C, Channel> = createParser<C, Channel> { name, command ->
            command.channels[name]?.let { ArgumentParseResult.success(it) }
        }

        /**
         * Returns a parser which extracts a [Role].
         *
         * The parsed role may not be accessed from the context during suggestion generation.
         */
        public fun <C : Any> roleParser(): ParserDescriptor<C, Role> = createParser<C, Role> { name, command ->
            command.roles[name]?.let { ArgumentParseResult.success(it) }
        }

        /**
         * Returns a parser which extracts an [Entity].
         *
         * The parsed mentionable may not be accessed from the context during suggestion generation.
         */
        public fun <C : Any> mentionableParser(): ParserDescriptor<C, Entity> =
            createParser<C, Entity> { name, command -> command.users[name]?.let { ArgumentParseResult.success(it) } }

        /**
         * Returns a parser which extracts an [Attachment].
         *
         * The parsed attachment may not be accessed from the context during suggestion generation.
         */
        public fun <C : Any> attachmentParser(): ParserDescriptor<C, Attachment> =
            createParser<C, Attachment> { name, command -> command.attachments[name]?.let { ArgumentParseResult.success(it) } }

        private inline fun <C : Any, reified T : Any> createParser(
            noinline extract: suspend (name: String, command: InteractionCommand) -> ArgumentParseResult<T>?
        ): ParserDescriptor<C, T> = ParserDescriptor.of(KordParser(extract), T::class.java)
    }

    override fun parseNullable(
        commandContext: CommandContext<C>,
        commandInput: CommandInput
    ): CompletableFuture<ArgumentParseResult<T>?> = GlobalScope.future {
        extract(commandInput.readString(), commandContext.interaction.command)
    }
}
