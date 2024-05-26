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

import dev.kord.common.DiscordBitSet
import dev.kord.common.entity.Permissions.Builder
import dev.kord.core.Kord
import dev.kord.core.behavior.createApplicationCommands
import dev.kord.core.entity.Guild
import dev.kord.rest.builder.interaction.BaseChoiceBuilder
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.NumericOptionBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.SubCommandBuilder
import dev.kord.rest.builder.interaction.attachment
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.channel
import dev.kord.rest.builder.interaction.group
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.mentionable
import dev.kord.rest.builder.interaction.number
import dev.kord.rest.builder.interaction.role
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.subCommand
import dev.kord.rest.builder.interaction.user
import org.apiguardian.api.API
import org.incendo.cloud.CommandTree
import org.incendo.cloud.discord.slash.CommandScope
import org.incendo.cloud.discord.slash.CommandScopePredicate
import org.incendo.cloud.discord.slash.DiscordCommand
import org.incendo.cloud.discord.slash.DiscordCommandFactory
import org.incendo.cloud.discord.slash.DiscordOption
import org.incendo.cloud.discord.slash.DiscordOption.SubCommand
import org.incendo.cloud.discord.slash.DiscordOption.Variable
import org.incendo.cloud.discord.slash.DiscordOptionType
import org.incendo.cloud.discord.slash.DiscordPermission
import org.incendo.cloud.discord.slash.NodeProcessor
import org.incendo.cloud.discord.slash.OptionRegistry
import org.incendo.cloud.discord.slash.StandardDiscordCommandFactory
import org.incendo.cloud.discord.slash.StandardOptionRegistry
import org.incendo.cloud.internal.CommandNode

@API(status = API.Status.STABLE, since = "1.0.0")
internal class StandardKordCommandFactory<C : Any>(
    private val commandTree: CommandTree<C>,
    private val optionRegistry: OptionRegistry<C> = StandardOptionRegistry(),
    private val discordCommandFactory: DiscordCommandFactory<C> = StandardDiscordCommandFactory(optionRegistry),
    private val nodeProcessor: NodeProcessor<C> = NodeProcessor(commandTree),
    override var commandScopePredicate: CommandScopePredicate<C> = CommandScopePredicate.alwaysTrue()
) : KordCommandFactory<C> {

    init {
        optionRegistry
            .registerMapping(KordOptionType.USER, KordParser.userParser())
            .registerMapping(KordOptionType.CHANNEL, KordParser.channelParser())
            .registerMapping(KordOptionType.ROLE, KordParser.roleParser())
            .registerMapping(KordOptionType.MENTIONABLE, KordParser.mentionableParser())
            .registerMapping(KordOptionType.ATTACHMENT, KordParser.attachmentParser())
    }

    override suspend fun createGuildCommands(guild: Guild) {
        guild.createApplicationCommands {
            createCommands(CommandScope.guilds(-1, guild.id.value.toLong()))
        }
    }

    override suspend fun deleteGuildCommands(guild: Guild) {
        guild.getApplicationCommands().collect {
            it.delete()
        }
    }

    override suspend fun createGlobalCommands(kord: Kord) {
        kord.createGlobalApplicationCommands {
            createCommands(CommandScope.global())
        }
    }

    override suspend fun deleteGlobalCommands(kord: Kord) {
        kord.getGlobalApplicationCommands().collect {
            it.delete()
        }
    }

    private fun MultiApplicationCommandBuilder.createCommands(scope: CommandScope<C>) {
        nodeProcessor.prepareTree()

        commandTree.rootNodes().forEach { rootNode ->
            val rootScope = rootNode.nodeMeta().get(NodeProcessor.NODE_META_SCOPE) as CommandScope<C>
            if (!rootScope.overlaps(scope)) {
                return@forEach
            }

            if (!commandScopePredicate.test(rootNode, scope)) {
                return@forEach
            }

            val discordCommand = discordCommandFactory.create(rootNode)
            input(discordCommand.name(), discordCommand.description()) {
                createCommand(discordCommand)

                // It's the best we've got
                val accessMap = rootNode.nodeMeta().getOrNull(CommandNode.META_KEY_ACCESS)
                val senderType = rootNode.command().senderType().map { v -> v.type }.orElse(null)

                accessMap?.get(senderType)
                    ?.let { it as? DiscordPermission }
                    ?.permissionString()
                    ?.let { DiscordBitSet(it) }
                    ?.let(::Builder)
                    ?.let(Builder::build)
                    ?.apply(this@input::defaultMemberPermissions::set)
            }
        }
    }

    private fun ChatInputCreateBuilder.createCommand(discordCommand: DiscordCommand<C>) {
        discordCommand.options().forEach { option ->
            createOption(option)
        }
    }

    private fun ChatInputCreateBuilder.createOption(discordOption: DiscordOption<C>) {
        if (discordOption is SubCommand<C>) {
            createSubCommand(discordOption)
        } else if (discordOption is Variable<C>) {
            createVariable(discordOption)
        }
    }

    private fun ChatInputCreateBuilder.createSubCommand(subCommand: SubCommand<C>) {
        if (subCommand.type() == DiscordOptionType.SUB_COMMAND) {
            subCommand(subCommand.name(), subCommand.description()) {
                configureSubCommand(subCommand)
            }
        } else {
            group(subCommand.name(), subCommand.description()) {
                subCommand.options().forEach { child ->
                    require(child is SubCommand<C>) {
                        "Cannot add variable option ${child.name()} as a child if group ${subCommand.name()}"
                    }
                    subCommand(child.name(), child.description()) {
                        configureSubCommand(child)
                    }
                }
            }
        }
    }

    private fun SubCommandBuilder.configureSubCommand(subCommand: SubCommand<C>) {
        subCommand.options().forEach { child ->
            require(child is Variable<C>) {
                "Cannot add subcommand ${child.name()} as a child of subcommand ${subCommand.name()}"
            }
            createVariable(child)
        }
    }

    private fun BaseInputChatBuilder.createVariable(variable: Variable<C>) {
        when (variable.type()) {
            DiscordOptionType.INTEGER -> integer(variable.name(), variable.description()) {
                configureNumericVariable(variable)
            }

            DiscordOptionType.NUMBER -> number(variable.name(), variable.description()) {
                configureNumericVariable(variable)
            }

            DiscordOptionType.STRING -> string(variable.name(), variable.description()) {
                configureChoiceVariable(variable)
            }

            DiscordOptionType.BOOLEAN -> boolean(variable.name(), variable.description()) {
                configureVariable(variable)
            }

            KordOptionType.ROLE -> role(variable.name(), variable.description()) {
                configureVariable(variable)
            }

            KordOptionType.CHANNEL -> channel(variable.name(), variable.description()) {
                configureVariable(variable)
            }

            KordOptionType.USER -> user(variable.name(), variable.description()) {
                configureVariable(variable)
            }

            KordOptionType.MENTIONABLE -> mentionable(variable.name(), variable.description()) {
                configureVariable(variable)
            }

            KordOptionType.ATTACHMENT -> attachment(variable.name(), variable.description()) {
                configureVariable(variable)
            }
        }
    }

    private inline fun <reified T : Number> NumericOptionBuilder<T>.configureNumericVariable(variable: Variable<C>) {
        variable.range()?.let {
            if (T::class == Long::class) {
                minValue = it.min().toLong() as T
                maxValue = it.max().toLong() as T
            } else {
                minValue = it.min().toDouble() as T
                maxValue = it.max().toDouble() as T
            }
        }
        configureChoiceVariable(variable)
    }

    private inline fun <reified T : Any> BaseChoiceBuilder<T>.configureChoiceVariable(variable: Variable<C>) {
        if (variable.autocomplete()) {
            autocomplete = true
        } else if (variable.choices().isNotEmpty()) {
            variable.choices().forEach { choice ->
                if (T::class == Long::class) {
                    choice(choice.name(), (choice.value() as Number).toLong() as T)
                } else if (T::class == Double::class) {
                    choice(choice.name(), (choice.value() as Number).toDouble() as T)
                } else {
                    choice(choice.name(), choice.value() as T)
                }
            }
        }
        configureVariable(variable)
    }

    private fun OptionsBuilder.configureVariable(variable: Variable<C>) {
        required = variable.required()
    }
}
