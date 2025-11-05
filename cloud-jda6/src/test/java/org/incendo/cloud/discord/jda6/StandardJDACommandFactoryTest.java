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
package org.incendo.cloud.discord.jda6;

import java.util.Collection;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.discord.slash.CommandScope;
import org.incendo.cloud.discord.slash.DiscordChoices;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.parser.standard.BooleanParser.booleanParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

class StandardJDACommandFactoryTest {

    private JDA6CommandManager<JDAInteraction> commandManager;
    private JDACommandFactory<JDAInteraction> commandFactory;

    @BeforeEach
    void setup() {
        this.commandManager = new JDA6CommandManager<>(
                ExecutionCoordinator.simpleCoordinator(),
                JDAInteraction.InteractionMapper.identity()
        );
        this.commandFactory = this.commandManager.commandFactory();
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
        final Collection<CommandData> commands = this.commandFactory.createCommands(CommandScope.global());

        // Assert
        assertThat(commands).hasSize(1);
        final CommandData command = commands.stream().findFirst().get();
        assertThat(command.getName()).isEqualTo("command");
        assertThat(command.getType()).isEqualTo(Command.Type.SLASH);
        final SlashCommandData slashCommand = (SlashCommandData) command;

        final List<SubcommandData> subcommands = slashCommand.getSubcommands();
        assertThat(subcommands.get(0).getName()).isEqualTo("bar");
        assertThat(subcommands.get(0).getDescription()).isEqualTo("bar");
        assertThat(subcommands.get(1).getName()).isEqualTo("foo");
        assertThat(subcommands.get(1).getName()).isEqualTo("foo");

        final List<OptionData> barOptions = subcommands.get(0).getOptions();
        assertThat(barOptions).hasSize(1);
        assertThat(barOptions.get(0).getName()).isEqualTo("string");
        assertThat(barOptions.get(0).getType()).isEqualTo(OptionType.STRING);
        assertThat(barOptions.get(0).getDescription()).isEqualTo("string");
        assertThat(barOptions.get(0).getChoices()).containsExactly(
                new Command.Choice("cat", "cat"),
                new Command.Choice("dog", "dog")
        );
        assertThat(barOptions.get(0).isRequired()).isTrue();
        assertThat(barOptions.get(0).isAutoComplete()).isFalse();

        final List<OptionData> fooOptions = subcommands.get(1).getOptions();
        assertThat(fooOptions).hasSize(2);
        assertThat(fooOptions.get(0).getName()).isEqualTo("integer");
        assertThat(fooOptions.get(0).getType()).isEqualTo(OptionType.INTEGER);
        assertThat(fooOptions.get(0).getDescription()).isEqualTo("Integer Argument");
        assertThat(fooOptions.get(0).getMinValue()).isEqualTo(1);
        assertThat(fooOptions.get(0).getMaxValue()).isEqualTo(10);
        assertThat(fooOptions.get(0).isRequired()).isTrue();
        assertThat(fooOptions.get(0).isAutoComplete()).isTrue();
        assertThat(fooOptions.get(1).getName()).isEqualTo("boolean");
        assertThat(fooOptions.get(1).getType()).isEqualTo(OptionType.BOOLEAN);
        assertThat(fooOptions.get(1).getDescription()).isEqualTo("boolean");
        assertThat(fooOptions.get(1).isRequired()).isFalse();
        assertThat(fooOptions.get(1).isAutoComplete()).isFalse();
    }
}
