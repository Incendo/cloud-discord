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

import cloud.commandframework.Description
import cloud.commandframework.arguments.aggregate.AggregateParser
import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.standard.IntegerParser.integerParser
import cloud.commandframework.kotlin.coroutines.extension.suspendingHandler
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.kotlin.extension.name
import cloud.commandframework.kotlin.extension.parser
import cloud.commandframework.kotlin.extension.suggestionProvider
import cloud.commandframework.kotlin.extension.textDescription
import cloud.commandframework.kotlin.extension.withComponent
import dev.kord.core.entity.User
import org.incendo.cloud.discord.kord.KordCommandManager
import org.incendo.cloud.discord.kord.KordInteraction
import org.incendo.cloud.discord.kord.KordParser.Companion.userParser
import org.incendo.cloud.discord.kord.example.Example
import org.incendo.cloud.discord.kord.interaction
import org.incendo.cloud.discord.slash.DiscordChoices

/**
 * Example showcasing aggregate parsers.
 */
public class AggregateCommand : Example {

    override fun register(commandManager: KordCommandManager<KordInteraction>) {
        val hugParser = AggregateParser.builder<KordInteraction>()
            .withComponent {
                parser = userParser()
                name = "recipient"
                textDescription = "The recipient of the hugs"
            }
            .withComponent {
                parser = integerParser(1, 20)
                name = "number"
                textDescription = "The number of hugs"
                suggestionProvider = DiscordChoices.integers(1..20)
            }
            .withDirectMapper(Hug::class.java) { _, ctx ->
                ArgumentParseResult.success(
                    Hug(ctx.get("recipient"), ctx.get("number"))
                )
            }.build()

        commandManager.buildAndRegister("hug", Description.of("Hug someone")) {
            required("hug", hugParser)

            suspendingHandler { context ->
                val hug = context.get<Hug>("hug")

                context.interaction.respondPublic {
                    content = "You hug ${hug.recipient.mention} ${hug.number} time(s)!"
                }
            }
        }
    }

    public data class Hug(
        /**
         * The recipient of the hugs.
         */
        public val recipient: User,
        /**
         * The number of hugs.
         */
        public val number: Int
    )
}
