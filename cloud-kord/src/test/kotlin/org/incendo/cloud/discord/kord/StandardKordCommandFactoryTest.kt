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

import com.google.common.truth.Truth.assertThat
import dev.kord.rest.builder.interaction.GlobalMultiApplicationCommandBuilder
import org.incendo.cloud.discord.slash.CommandScope
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StandardKordCommandFactoryTest {

    private lateinit var commandManager: KordCommandManager<KordInteraction>
    private lateinit var commandFactory: StandardKordCommandFactory<KordInteraction>

    @BeforeEach
    fun setup() {
        commandManager = KordCommandManager(ExecutionCoordinator.simpleCoordinator()) { it }
        @Suppress("UNCHECKED_CAST")
        commandFactory = commandManager.commandFactory as StandardKordCommandFactory<KordInteraction>
    }

    @Test
    fun testCreateCommandWithArgument() {
        // Arrange
        commandManager.command(
            commandManager.commandBuilder("command")
                .required("string", stringParser())
        )
        val builder = GlobalMultiApplicationCommandBuilder()

        // Act
        with(commandFactory) {
            builder.createCommands(CommandScope.global())
        }

        // Assert
        assertThat(builder.commands.map { it.name }).containsExactly("command")
    }

    @Test
    fun testCreateCommandWithoutArgument() {
        // Arrange
        commandManager.command(commandManager.commandBuilder("command"))
        val builder = GlobalMultiApplicationCommandBuilder()

        // Act
        with(commandFactory) {
            builder.createCommands(CommandScope.global())
        }

        // Assert
        assertThat(builder.commands.map { it.name }).containsExactly("command")
    }
}
