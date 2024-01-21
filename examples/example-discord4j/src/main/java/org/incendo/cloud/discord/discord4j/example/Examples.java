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
package org.incendo.cloud.discord.discord4j.example;

import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.discord4j.Discord4JCommandManager;
import org.incendo.cloud.discord.discord4j.Discord4JInteraction;
import org.incendo.cloud.discord.discord4j.example.commands.AggregateCommand;
import org.incendo.cloud.discord.discord4j.example.commands.AnnotatedCommands;
import org.incendo.cloud.discord.discord4j.example.commands.PingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that registers the examples.
 *
 * <p>You can find the active examples in {@link #examples}.</p>
 */
public final class Examples {

    private static final Logger LOGGER = LoggerFactory.getLogger(Examples.class);

    private final Discord4JCommandManager<Discord4JInteraction> commandManager;
    private final List<? extends Example> examples = Arrays.asList(
            new AggregateCommand(),
            new AnnotatedCommands(),
            new PingCommand()
    );

    /**
     * Creates a new example instance.
     *
     * @param commandManager command manager
     */
    public Examples(final @NonNull Discord4JCommandManager<Discord4JInteraction> commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Registers the example commands.
     */
    public void registerExamples() {
        LOGGER.info("Registering examples:");
        for (final Example example : this.examples) {
            LOGGER.info("- Registering example: {}", example.getClass().getSimpleName());
            example.register(this.commandManager);
        }
    }
}
