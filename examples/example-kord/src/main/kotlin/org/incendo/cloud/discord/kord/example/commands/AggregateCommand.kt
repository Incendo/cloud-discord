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

import dev.kord.core.entity.User
import org.incendo.cloud.description.Description
import org.incendo.cloud.discord.kord.KordCommandManager
import org.incendo.cloud.discord.kord.KordInteraction
import org.incendo.cloud.discord.kord.KordParser.Companion.userParser
import org.incendo.cloud.discord.kord.example.Example
import org.incendo.cloud.discord.kord.interaction
import org.incendo.cloud.discord.slash.DiscordChoices
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.kotlin.extension.name
import org.incendo.cloud.kotlin.extension.parser
import org.incendo.cloud.kotlin.extension.suggestionProvider
import org.incendo.cloud.kotlin.extension.textDescription
import org.incendo.cloud.kotlin.extension.withComponent
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.aggregate.AggregateParser
import org.incendo.cloud.parser.standard.IntegerParser.integerParser

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
