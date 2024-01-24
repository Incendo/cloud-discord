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
package org.incendo.cloud.discord.kord.example.commands

import dev.kord.common.entity.Permission
import org.incendo.cloud.description.Description
import org.incendo.cloud.discord.kord.KordCommandManager
import org.incendo.cloud.discord.kord.KordInteraction
import org.incendo.cloud.discord.kord.commandScope
import org.incendo.cloud.discord.kord.example.Example
import org.incendo.cloud.discord.kord.interaction
import org.incendo.cloud.discord.kord.permissions
import org.incendo.cloud.discord.slash.CommandScope
import org.incendo.cloud.key.CloudKey
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.kotlin.extension.textDescription
import org.incendo.cloud.parser.standard.StringParser.greedyStringParser

/**
 * Example of a command that responds with the original input.
 */
public class PingCommand : Example {

    private companion object {
        private val COMPONENT_MESSAGE = CloudKey.of(
            "message",
            String::class.java
        )
    }

    override fun register(commandManager: KordCommandManager<KordInteraction>) {
        commandManager.buildAndRegister("ping", Description.of("A ping command")) {
            commandScope(CommandScope.guilds()) // You may only ping in guilds!

            permissions(Permission.Administrator)

            required(COMPONENT_MESSAGE, greedyStringParser()) {
                textDescription = "Hello world!"
            }

            suspendingHandler { context ->
                val message = context.get(COMPONENT_MESSAGE)

                context.interaction.respondEphemeral {
                    content = message
                }
            }
        }
    }
}
