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
package org.incendo.cloud.discord.discord4j.example.commands;

import discord4j.common.util.Snowflake;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.annotation.specifier.Completions;
import org.incendo.cloud.annotation.specifier.Range;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.discord.discord4j.Discord4JCommandManager;
import org.incendo.cloud.discord.discord4j.Discord4JInteraction;
import org.incendo.cloud.discord.discord4j.example.Example;
import org.incendo.cloud.discord.immutables.ImmutableImpl;
import org.incendo.cloud.discord.slash.annotations.CommandScopeBuilderModifier;
import org.incendo.cloud.suggestion.Suggestion;

/**
 * Example showcasing how to use cloud-annotations with cloud-Discord4J.
 */
public final class AnnotatedCommands implements Example {

    private final CatRepository catRepository = new CatRepositoryImpl();

    @Override
    public void register(final @NonNull Discord4JCommandManager<Discord4JInteraction> commandManager) {
        final AnnotationParser<Discord4JInteraction> annotationParser = new AnnotationParser<>(
                commandManager,
                Discord4JInteraction.class
        );

        // Adds support for the Discord4J-specific annotations.
        CommandScopeBuilderModifier.install(annotationParser);

        // Parses @Command, @Parser, @Suggestions & @ExceptionHandler...
        annotationParser.parse(this);
    }

    /**
     * Adds a cat to the cat registry.
     *
     * @param interaction command trigger
     * @param name        name of the cat to add
     * @param age         age of the cat
     * @return future that completes when the interaction is done
     */
    @Command("cat add <name> <age>")
    public @NonNull CompletableFuture<Void> addCat(
            final @NonNull Discord4JInteraction interaction,
            @Completions("Cat,Benny,Meowy") final @NonNull String name,
            @Range(min = "0", max = "20") final int age
    ) {
        this.catRepository.addCat(CatImpl.of(name, age));
        return interaction.commandEvent()
                .get()
                .reply(String.format("Added the cat named %s with age %d", name, age))
                .withEphemeral(true)
                .toFuture();
    }

    /**
     * Removes the cat from the cat registry.
     *
     * @param interaction command trigger
     * @param name        name of the cat to remove
     * @return future that completes when the interaction is done
     */
    @Command("cat remove <name>")
    public @NonNull CompletableFuture<Void> removeCat(
            final @NonNull Discord4JInteraction interaction,
            @Argument(suggestions = "cats") final @NonNull String name
    ) {
        this.catRepository.removeCat(name);
        return interaction.commandEvent()
                .get()
                .reply(String.format("Removed the cat named %s", name))
                .withEphemeral(true)
                .toFuture();
    }

    /**
     * Lists the cats in the repository.
     *
     * @param interaction command trigger
     * @return future that completes when the interaction is done
     */
    @Command("cat list")
    public @NonNull CompletableFuture<Void> listCats(final @NonNull Discord4JInteraction interaction) {
        final String cats = this.catRepository.cats()
                .stream()
                .map(Cat::name)
                .collect(Collectors.joining(", "));
        return interaction.commandEvent()
                .get()
                .reply(String.format("Cats: %s", cats))
                .withEphemeral(true)
                .toFuture();
    }

    /**
     * Makes a cat meow at someone.
     *
     * @param interaction command trigger
     * @param cat         cat that should meow
     * @param target      meow target
     * @return future that completes when the interaction is done
     */
    @Command("cat meow <target> <cat>")
    public @NonNull CompletableFuture<Void> meow(
            final @NonNull Discord4JInteraction interaction,
            @Argument(suggestions = "cats") final @NonNull String cat,
            final @NonNull Snowflake target
    ) {
        return interaction.commandEvent()
                .get()
                .reply(cat + " meows at <@" + target.asLong() + ">")
                .toFuture();
    }

    /**
     * Returns suggestions based on the cats in the cat registry.
     *
     * @return the suggestions
     */
    @Suggestions("cats")
    public @NonNull Stream<@NonNull Suggestion> catNames() {
        return this.catRepository.cats()
                .stream()
                .map(Cat::name)
                .map(Suggestion::simple);
    }


    @ImmutableImpl
    @Value.Immutable
    interface Cat {

        /**
         * Returns the name of the cat.
         *
         * @return cat name
         */
        @NonNull String name();

        /**
         * Returns the age of the cat.
         *
         * @return cat age
         */
        int age();
    }


    private interface CatRepository {

        /**
         * Adds the cat.
         *
         * @param cat cat to add
         */
        void addCat(@NonNull Cat cat);

        /**
         * Removes the cat with the given {@code name}.
         *
         * @param name name of the cat to remove
         */
        void removeCat(@NonNull String name);

        /**
         * Returns an immutable view of the cats.
         *
         * @return the cats
         */
        @NonNull Collection<@NonNull Cat> cats();
    }

    private static final class CatRepositoryImpl implements CatRepository {

        private final Map<String, Cat> catMap = new HashMap<>();

        @Override
        public void addCat(final @NonNull Cat cat) {
            this.catMap.put(cat.name(), cat);
        }

        @Override
        public void removeCat(final @NonNull String name) {
            this.catMap.remove(name);
        }

        @Override
        public @NonNull Collection<@NonNull Cat> cats() {
            return Collections.unmodifiableCollection(this.catMap.values());
        }
    }
}
