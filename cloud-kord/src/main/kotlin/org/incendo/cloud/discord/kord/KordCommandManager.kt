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

import cloud.commandframework.CommandManager
import cloud.commandframework.exceptions.CommandExecutionException
import cloud.commandframework.exceptions.InvalidCommandSenderException
import cloud.commandframework.exceptions.InvalidSyntaxException
import cloud.commandframework.exceptions.NoPermissionException
import cloud.commandframework.exceptions.NoSuchCommandException
import cloud.commandframework.exceptions.handling.ExceptionContext
import cloud.commandframework.exceptions.handling.ExceptionController
import cloud.commandframework.execution.ExecutionCoordinator
import cloud.commandframework.internal.CommandRegistrationHandler
import cloud.commandframework.keys.CloudKey
import cloud.commandframework.setting.Configurable
import dev.kord.core.Kord
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildInteraction
import kotlinx.coroutines.runBlocking
import org.apiguardian.api.API

/**
 * Command manager for Kord.
 *
 * @param C command sender type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public class KordCommandManager<C : Any>(
    executionCoordinator: ExecutionCoordinator<C>,
    public val senderMapper: (KordInteraction) -> C
) : CommandManager<C>(
    executionCoordinator,
    CommandRegistrationHandler.nullCommandRegistrationHandler()
) {

    public companion object {
        /**
         * Stores the interaction. This should be accessed using [cloud.commandframework.context.CommandContext.interaction].
         */
        public val CONTEXT_INTERACTION: CloudKey<KordInteraction> = CloudKey.of(
            "cloud:interaction",
            KordInteraction::class.java
        )
    }

    /**
     * Kord-specific settings.
     */
    public val kordSettings: Configurable<KordSetting> = Configurable.enumConfigurable(KordSetting::class.java)

    /**
     * Factory that creates Kord commands from Cloud commands.
     */
    public var commandFactory: KordCommandFactory<C> = StandardKordCommandFactory<C>(this.commandTree())

    /**
     * Predicate used to evaluate sender permissions.
     */
    public var permissionPredicate: (C, String) -> Boolean = { _, _ -> true }

    init {
        kordSettings.set(KordSetting.AUTO_REGISTER_GLOBAL, true)
        kordSettings.set(KordSetting.AUTO_REGISTER_GUILD, true)
        kordSettings.set(KordSetting.CLEAR_EXISTING, true)

        parserRegistry()
            .registerParser(KordParser.userParser())
            .registerParser(KordParser.roleParser())
            .registerParser(KordParser.channelParser())
            .registerParser(KordParser.mentionableParser())
            .registerParser(KordParser.attachmentParser())

        // Common parameter injections.
        parameterInjectorRegistry().registerInjector(KordInteraction::class.java) { ctx, _ ->
            ctx.interaction
        }
        parameterInjectorRegistry().registerInjector(User::class.java) { ctx, _ ->
            ctx.interaction.interactionEvent.interaction.user
        }
        parameterInjectorRegistry().registerInjector(Member::class.java) { ctx, _ ->
            (ctx.interaction.interactionEvent.interaction as GuildInteraction).user
        }
        parameterInjectorRegistry().registerInjector(Kord::class.java) { ctx, _ ->
            ctx.interaction.interactionEvent.kord
        }

        registerDefaultExceptionHandlers()
    }

    /**
     * Installs the event listener that handles command registration, execution and autocompletion.
     */
    public fun installListener(kord: Kord) {
        KordEventListener(this).registerEvents(kord)
    }

    override fun hasPermission(sender: C, permission: String): Boolean = permissionPredicate(sender, permission)

    private fun registerDefaultExceptionHandlers() {
        exceptionController().registerSuspending<Throwable> {
            it.context().interaction.respondEphemeral {
                content = it.exception().message
            }
        }
        exceptionController().registerSuspending<CommandExecutionException> {
            it.context().interaction.respondEphemeral {
                content = "Invalid Command Argument: ${it.exception().cause?.message}"
            }
        }
        exceptionController().registerSuspending<NoSuchCommandException> {
            it.context().interaction.respondEphemeral {
                content = "Unknown command"
            }
        }
        exceptionController().registerSuspending<NoPermissionException> {
            it.context().interaction.respondEphemeral {
                content = "Insufficient permissions"
            }
        }
        exceptionController().registerSuspending<InvalidCommandSenderException> {
            it.context().interaction.respondEphemeral {
                content = it.exception().message
            }
        }
        exceptionController().registerSuspending<InvalidSyntaxException> {
            it.context().interaction.respondEphemeral {
                content = "Invalid Command Syntax. Correct command syntax is: /${it.exception().correctSyntax()}"
            }
        }
    }

    private inline fun <reified T : Throwable> ExceptionController<C>.registerSuspending(
        crossinline handler: suspend (ExceptionContext<C, T>) -> Unit
    ) {
        exceptionController().registerHandler(T::class.java) { exceptionContext ->
            runBlocking {
                handler(exceptionContext)
            }
        }
    }
}
