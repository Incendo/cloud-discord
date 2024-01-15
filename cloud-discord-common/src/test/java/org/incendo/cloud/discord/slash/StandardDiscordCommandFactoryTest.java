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
package org.incendo.cloud.discord.slash;

import cloud.commandframework.CommandManager;
import cloud.commandframework.Description;
import org.incendo.cloud.discord.util.TestCommandManager;
import org.incendo.cloud.discord.util.TestCommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.arguments.standard.BooleanParser.booleanParser;
import static cloud.commandframework.arguments.standard.IntegerParser.integerParser;
import static cloud.commandframework.arguments.standard.StringParser.stringParser;
import static com.google.common.truth.Truth.assertThat;

class StandardDiscordCommandFactoryTest {

    private DiscordCommandFactory<TestCommandSender> commandFactory;
    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        final OptionRegistry<TestCommandSender> optionRegistry = new StandardOptionRegistry<>();
        this.commandFactory = new StandardDiscordCommandFactory<>(optionRegistry);
        this.commandManager = new TestCommandManager();
    }

    @Test
    void testCommandCreation() {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("command", Description.of("Command Description"))
                        .literal("foo")
                        .required("integer", integerParser(1, 10), Description.of("Integer Argument"))
                        .optional("boolean", booleanParser())
        );
        this.commandManager.command(
                this.commandManager.commandBuilder("command")
                        .literal("bar")
                        .required("string", stringParser(), DiscordChoices.strings("cat", "dog"))
        );

        // Act
        final DiscordCommand<TestCommandSender> command =
                this.commandFactory.create(this.commandManager.commandTree().getNamedNode("command"));

        // Assert
        assertThat(command.name()).isEqualTo("command");
        assertThat(command.description()).isEqualTo("Command Description");
        assertThat(command.options()).containsExactly(
                ImmutableSubCommand.<TestCommandSender>builder()
                        .name("bar")
                        .description("bar")
                        .addOption(
                                ImmutableVariable.<TestCommandSender>builder()
                                        .name("string")
                                        .description("string")
                                        .type(DiscordOptionType.STRING)
                                        .required(true)
                                        .autocomplete(false)
                                        .addChoices(
                                                DiscordOptionChoice.of("cat", "cat"),
                                                DiscordOptionChoice.of("dog", "dog")
                                        ).build()
                        ).build(),
                ImmutableSubCommand.<TestCommandSender>builder()
                        .name("foo")
                        .description("foo")
                        .addOption(
                                ImmutableVariable.<TestCommandSender>builder()
                                        .name("integer")
                                        .description("Integer Argument")
                                        .type(DiscordOptionType.INTEGER)
                                        .required(true)
                                        .autocomplete(true)
                                        .range(Range.of(1, 10))
                                        .build()
                        ).addOption(
                                ImmutableVariable.<TestCommandSender>builder()
                                        .name("boolean")
                                        .description("boolean")
                                        .type(DiscordOptionType.BOOLEAN)
                                        .required(false)
                                        .autocomplete(false)
                                        .build()
                        ).build()
        );
    }

    @Test
    void testSubCommandGroup() {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("command")
                        .literal("group")
                        .literal("foo")
        );
        this.commandManager.command(
                this.commandManager.commandBuilder("command")
                        .literal("group")
                        .literal("bar")
        );

        // Act
        final DiscordCommand<TestCommandSender> command =
                this.commandFactory.create(this.commandManager.commandTree().getNamedNode("command"));

        // Assert
        assertThat(command.name()).isEqualTo("command");
        assertThat(command.options()).hasSize(1);
        assertThat(command.options().get(0).type()).isEqualTo(DiscordOptionType.SUB_COMMAND_GROUP);
        assertThat(command.options().get(0).name()).isEqualTo("group");
        assertThat(((DiscordOption.SubCommand<?>) command.options().get(0)).options()).containsExactly(
                ImmutableSubCommand.builder()
                        .name("foo")
                        .description("foo")
                        .build(),
                ImmutableSubCommand.builder()
                        .name("bar")
                        .description("bar")
                        .build()
        );
    }
}
