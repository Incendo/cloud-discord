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
import cloud.commandframework.arguments.aggregate.AggregateParser;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.types.range.Range;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
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
                                        .range(Range.intRange(1, 10))
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

    @Test
    void testAggregateParser() {
        // Arrange
        final AggregateParser<TestCommandSender, TestAggregateObject> parser = AggregateParser
                .<TestCommandSender>builder()
                .withComponent("integer", integerParser())
                .withComponent("string", stringParser())
                .withComponent("bool", booleanParser())
                .withDirectMapper(TestAggregateObject.class, (cmdCtx, ctx) -> ArgumentParseResult.success(new TestAggregateObject(
                        ctx.get("integer"),
                        ctx.get("string"),
                        ctx.get("bool")
                ))).build();
        this.commandManager.command(this.commandManager.commandBuilder("command").required("aggregate", parser));

        // Act
        final DiscordCommand<TestCommandSender> command =
                this.commandFactory.create(this.commandManager.commandTree().getNamedNode("command"));

        // Assert
        assertThat(command.name()).isEqualTo("command");
        assertThat(command.options()).hasSize(3);
        assertThat(command.options().get(0).type()).isEqualTo(DiscordOptionType.INTEGER);
        assertThat(command.options().get(0).name()).isEqualTo("integer");
        assertThat(command.options().get(1).type()).isEqualTo(DiscordOptionType.STRING);
        assertThat(command.options().get(1).name()).isEqualTo("string");
        assertThat(command.options().get(2).type()).isEqualTo(DiscordOptionType.BOOLEAN);
        assertThat(command.options().get(2).name()).isEqualTo("bool");
    }


    private static final class TestAggregateObject {

        private final int integer;
        private final String string;
        private final boolean bool;

        private TestAggregateObject(final int integer, final @NonNull String string, final boolean bool) {
            this.integer = integer;
            this.string = string;
            this.bool = bool;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o)  {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final TestAggregateObject that = (TestAggregateObject) o;
            return this.integer == that.integer && this.bool == that.bool && Objects.equals(this.string, that.string);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.integer, this.string, this.bool);
        }
    }
}
