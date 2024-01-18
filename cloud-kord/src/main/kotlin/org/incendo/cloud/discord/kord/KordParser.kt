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

import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.parser.ParserDescriptor
import cloud.commandframework.context.CommandContext
import cloud.commandframework.context.CommandInput
import cloud.commandframework.kotlin.coroutines.SuspendingArgumentParser
import cloud.commandframework.kotlin.coroutines.asParserDescriptor
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Entity
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.interaction.GuildInteraction
import dev.kord.core.entity.interaction.InteractionCommand
import kotlinx.coroutines.flow.firstOrNull
import org.apiguardian.api.API
import kotlin.NullPointerException

/**
 * A parser which wraps a Kord option value.
 *
 * @param C command sender type
 * @param T kord type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public fun interface KordParser<C : Any, T : Any> : SuspendingArgumentParser<C, T> {

    public companion object {

        /**
         * Returns a parser which extracts a [User].
         */
        public fun <C : Any> userParser(): ParserDescriptor<C, User> = createParser<C, User> { name, command, context ->
            command.users[name]?.let { ArgumentParseResult.success(it) }
                ?: context.ifSuggestion(name) {
                    ArgumentParseResult.success(context.interaction.interactionEvent.interaction.user)
                }
        }

        /**
         * Returns a parser which extracts a [Channel].
         */
        public fun <C : Any> channelParser(): ParserDescriptor<C, Channel> = createParser<C, Channel> { name, command, context ->
            command.channels[name]?.let { ArgumentParseResult.success(it) }
                ?: context.ifSuggestion(name) {
                    (context.interaction.interactionEvent as? GuildInteraction)?.channel?.asChannel()
                        ?.let { ArgumentParseResult.success(it) }
                }
        }

        /**
         * Returns a parser which extracts a [Role].
         */
        public fun <C : Any> roleParser(): ParserDescriptor<C, Role> = createParser<C, Role> { name, command, context ->
            command.roles[name]?.let { ArgumentParseResult.success(it) }
                ?: context.ifSuggestion(name) {
                    (context.interaction.interactionEvent as? GuildInteraction)?.user?.roles?.firstOrNull()
                        ?.let { ArgumentParseResult.success(it) }
                }
        }

        /**
         * Returns a parser which extracts an [Entity].
         */
        public fun <C : Any> mentionableParser(): ParserDescriptor<C, Entity> =
            createParser<C, Entity> { name, command, context ->
                command.users[name]?.let { ArgumentParseResult.success(it) }
                    ?: context.ifSuggestion(name) {
                        ArgumentParseResult.success(context.interaction.interactionEvent.interaction.user)
                    }
            }

        /**
         * Returns a parser which extracts an [Attachment].
         */
        public fun <C : Any> attachmentParser(): ParserDescriptor<C, Attachment> =
            createParser<C, Attachment> { name, command, _ ->
                command.attachments[name]?.let { ArgumentParseResult.success(it) }
                    ?: ArgumentParseResult.failure(NullPointerException(name))
            }

        private inline fun <C : Any, reified T : Any> createParser(parser: KordParser<C, T>): ParserDescriptor<C, T> =
            parser.asParserDescriptor<C, T>()

        // TODO(City): This is a terrible hack and should be removed.
        private suspend inline fun <C : Any, T : Any> CommandContext<C>.ifSuggestion(
            name: String,
            crossinline body: suspend () -> ArgumentParseResult<T>?
        ): ArgumentParseResult<T> {
            val result = if (isSuggestions) {
                body()
            } else {
                null
            }
            return result ?: ArgumentParseResult.failure(NullPointerException(name))
        }
    }

    /**
     * Returns the result of extracting the argument from the given mapping.
     */
    public suspend fun extract(name: String, command: InteractionCommand, context: CommandContext<C>): ArgumentParseResult<T>

    override suspend fun invoke(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<T> = extract(
        commandInput.readString(),
        commandContext.interaction.command,
        commandContext
    )
}
