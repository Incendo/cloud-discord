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

import cloud.commandframework.context.CommandContextFactory
import cloud.commandframework.context.StandardCommandContextFactory
import cloud.commandframework.util.StringUtils
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.suggestInteger
import dev.kord.core.behavior.interaction.suggestNumber
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.GroupCommand
import dev.kord.core.entity.interaction.IntegerOptionValue
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.core.entity.interaction.NumberOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.future.await
import org.apiguardian.api.API

/**
 * Kord event listener which handles command registration, execution and autocompletion.
 *
 * @param C command sender type
 * @since 1.0.0
 */
@API(status = API.Status.INTERNAL, since = "1.0.0")
internal class KordEventListener<C : Any>(private val commandManager: KordCommandManager<C>) {

    private val contextFactory: CommandContextFactory<C> = StandardCommandContextFactory(commandManager)

    internal fun registerEvents(kord: Kord) {
        kord.on<ReadyEvent> {
            listen()
        }
        kord.on<GuildCreateEvent> {
            listen()
        }
        kord.on<ChatInputCommandInteractionCreateEvent> {
            listen()
        }
        kord.on<AutoCompleteInteractionCreateEvent> {
            listen()
        }
    }

    private suspend fun ReadyEvent.listen() {
        if (commandManager.kordSettings[KordSetting.CLEAR_EXISTING]) {
            commandManager.commandFactory.deleteGlobalCommands(kord)
        }
        if (commandManager.kordSettings[KordSetting.AUTO_REGISTER_GLOBAL]) {
            commandManager.commandFactory.createGlobalCommands(kord)
        }
    }

    private suspend fun GuildCreateEvent.listen() {
        if (commandManager.kordSettings[KordSetting.CLEAR_EXISTING]) {
            commandManager.commandFactory.deleteGuildCommands(guild)
        }
        if (commandManager.kordSettings[KordSetting.AUTO_REGISTER_GUILD]) {
            commandManager.commandFactory.createGuildCommands(guild)
        }
    }

    private suspend fun ChatInputCommandInteractionCreateEvent.listen() {
        val command = interaction.command
        val fullCommand = command.buildCommand()

        val kordInteraction = KordInteraction(command, this)

        try {
            commandManager.commandExecutor().executeCommand(
                commandManager.senderMapper(kordInteraction),
                fullCommand,
            ) { context -> context[KordCommandManager.CONTEXT_INTERACTION] = kordInteraction }.await()
        } catch (_: Exception) {
            // Exceptions are handled by the exception controller.
        }
    }

    private suspend fun AutoCompleteInteractionCreateEvent.listen() {
        val command = interaction.command

        var fullCommand = command.buildCommand()
        if (this.interaction.focusedOption.value.isEmpty()) {
            fullCommand = "$fullCommand "
        }

        val kordInteraction = KordInteraction(command, this)

        val commandContext = contextFactory.create(
            true,
            commandManager.senderMapper(kordInteraction)
        )
        commandContext[KordCommandManager.CONTEXT_INTERACTION] = kordInteraction

        val type = command.options.values.first(OptionValue<*>::focused)

        val suggestions = commandManager.suggestionFactory().suggest(commandContext, fullCommand).await().let { suggestions ->
            suggestions.list()
                .asSequence()
                .map { suggestion ->
                    if (" " in suggestion.suggestion()) {
                        suggestion.withSuggestion(
                            StringUtils.trimBeforeLastSpace(
                                suggestion.suggestion(),
                                suggestions.commandInput()
                            )!!
                        )
                    } else {
                        suggestion
                    }
                }.filterNot { it.suggestion().isEmpty() }
        }

        when (type) {
            is IntegerOptionValue -> {
                interaction.suggestInteger {
                    suggestions.forEach {
                        choice(it.suggestion(), it.suggestion().toLong()) {
                        }
                    }
                }
            }
            is NumberOptionValue -> {
                interaction.suggestNumber {
                    suggestions.forEach {
                        choice(it.suggestion(), it.suggestion().toDouble()) {
                        }
                    }
                }
            }
            else -> {
                interaction.suggestString {
                    suggestions.forEach {
                        choice(it.suggestion(), it.suggestion()) {
                        }
                    }
                }
            }
        }
    }

    private fun InteractionCommand.buildCommand(): String = buildString {
        append(rootName)
        when (this@buildCommand) {
            is GroupCommand -> {
                append(" ").append(groupName).append(" ").append(name)
            }
            is SubCommand -> {
                append(" ").append(name)
            }
            else -> {}
        }

        options.forEach { (name, value) ->
            append(" ")
            when (value.value) {
                is Snowflake -> {
                    append(name)
                }
                else -> {
                    append(value.value)
                }
            }
        }

        if (toString().endsWith(" ")) {
            return substring(0, length - 1)
        }
    }
}
