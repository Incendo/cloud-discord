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
package org.incendo.cloud.discord.kord.example

import io.github.oshai.kotlinlogging.KotlinLogging
import org.incendo.cloud.discord.kord.KordCommandManager
import org.incendo.cloud.discord.kord.KordInteraction
import org.incendo.cloud.discord.kord.example.commands.AggregateCommand
import org.incendo.cloud.discord.kord.example.commands.AnnotatedCommands
import org.incendo.cloud.discord.kord.example.commands.PingCommand

private val logger = KotlinLogging.logger {}

/**
 * Class that registers the examples.
 *
 * You can find the active examples in [examples].
 */
public class Examples(private val commandManager: KordCommandManager<KordInteraction>) {

    private val examples: List<Example> = listOf(
        AggregateCommand(),
        AnnotatedCommands(),
        PingCommand()
    )

    /**
     * Registers the example commands.
     */
    public fun registerExamples() {
        logger.info { "Registering examples:" }
        examples.forEach { example ->
            logger.info { "- Registering example: ${example::class.simpleName}" }
            example.register(commandManager)
        }
    }
}
