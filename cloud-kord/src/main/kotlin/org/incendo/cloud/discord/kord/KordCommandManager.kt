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
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildInteraction
import kotlinx.coroutines.runBlocking
import org.apiguardian.api.API
import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.internal.CommandRegistrationHandler
import org.incendo.cloud.key.CloudKey
import org.incendo.cloud.setting.Configurable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
        private val LOGGER: Logger = LoggerFactory.getLogger(KordCommandManager::class.java)

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
        registerDefaultExceptionHandlers(
            {
                runBlocking {
                    it.first().interaction.respondEphemeral {
                        content = it.first().formatCaption(it.second(), it.third())
                    }
                }
            },
            {
                LOGGER.error(it.first(), it.second())
            }
        )
    }
}
