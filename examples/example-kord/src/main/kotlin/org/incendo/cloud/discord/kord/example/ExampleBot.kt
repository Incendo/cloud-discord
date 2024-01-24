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

import dev.kord.core.Kord
import io.github.oshai.kotlinlogging.KotlinLogging
import org.incendo.cloud.discord.kord.KordCommandManager
import org.incendo.cloud.execution.ExecutionCoordinator
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Example kord bot.
 */
public class ExampleBot(public val configuration: BotConfiguration) {

    /**
     * Starts the bot.
     */
    public suspend fun start() {
        logger.info { "Starting the example bot..." }
        val commandManager = KordCommandManager(ExecutionCoordinator.simpleCoordinator()) {
            it
        }

        Examples(commandManager).registerExamples()

        logger.info { "Logging into Kord..." }
        val kord = Kord(configuration.token)

        logger.info { "Installing the event listener..." }
        commandManager.installListener(kord)

        kord.login()
    }
}

/**
 * Main method.
 */
public suspend fun main() {
    ExampleBot(PropertiesBotConfiguration(File("./bot.properties"))).start()
}
