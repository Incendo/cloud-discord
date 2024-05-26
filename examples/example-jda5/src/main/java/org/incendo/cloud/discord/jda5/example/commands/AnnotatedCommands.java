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
package org.incendo.cloud.discord.jda5.example.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.dv8tion.jda.api.entities.IMentionable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.annotation.specifier.Completions;
import org.incendo.cloud.annotation.specifier.Range;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.discord.immutables.ImmutableImpl;
import org.incendo.cloud.discord.jda5.JDA5CommandManager;
import org.incendo.cloud.discord.jda5.JDAInteraction;
import org.incendo.cloud.discord.jda5.annotation.ReplySetting;
import org.incendo.cloud.discord.jda5.annotation.ReplySettingBuilderModifier;
import org.incendo.cloud.discord.jda5.example.Example;
import org.incendo.cloud.discord.slash.annotation.CommandScopeBuilderModifier;
import org.incendo.cloud.suggestion.Suggestion;

/**
 * Example showcasing how to use cloud-annotations with cloud-jda5.
 */
public final class AnnotatedCommands implements Example {

    private final CatRepository catRepository = new CatRepositoryImpl();

    @Override
    public void register(final @NonNull JDA5CommandManager<JDAInteraction> commandManager) {
        final AnnotationParser<JDAInteraction> annotationParser = new AnnotationParser<>(
                commandManager,
                JDAInteraction.class
        );

        // Adds support for the JDA-specific annotations.
        ReplySettingBuilderModifier.install(annotationParser);
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
     * @param breed       breed of the cat
     */
    @ReplySetting(defer = true, ephemeral = true)
    @Command("cat add <name> <age> <breed>")
    public void addCat(
            final @NonNull JDAInteraction interaction,
            @Completions("Cat,Benny,Meowy") final @NonNull String name,
            @Range(min = "0", max = "20") final int age,
            final @NonNull CatBreed breed
    ) {
        this.catRepository.addCat(CatImpl.of(name, age, breed));
        // We need to reply through the hook because we used a deferred reply.
        interaction.interactionEvent()
                .getHook()
                .sendMessageFormat("Added the cat named %s with age %d", name, age)
                .queue();
    }

    /**
     * Removes the cat from the cat registry.
     *
     * @param interaction command trigger
     * @param name        name of the cat to remove
     */
    @Command("cat remove <name>")
    public void removeCat(
            final @NonNull JDAInteraction interaction,
            @Argument(suggestions = "cats") final @NonNull String name
    ) {
        this.catRepository.removeCat(name);
        interaction.replyCallback()
                .replyFormat("Removed the cat named %s", name)
                .setEphemeral(true)
                .queue();
    }

    /**
     * Lists the cats in the repository.
     *
     * @param interaction command trigger
     */
    @Command("cat list")
    public void listCats(final @NonNull JDAInteraction interaction) {
        final String cats = this.catRepository.cats()
                .stream()
                .map(Cat::name)
                .collect(Collectors.joining(", "));
        interaction.replyCallback()
                .replyFormat("Cats: %s", cats)
                .setEphemeral(true)
                .queue();
    }

    /**
     * Makes a cat meow at someone.
     *
     * @param interaction command trigger
     * @param cat         cat that should meow
     * @param target      meow target
     */
    @Command("cat meow <target> <cat>")
    public void meow(
            final @NonNull JDAInteraction interaction,
            @Argument(suggestions = "cats") final @NonNull String cat,
            final @NonNull IMentionable target
    ) {
        interaction.replyCallback().reply("The meowing is happening...").setEphemeral(true).queue();
        interaction.interactionEvent()
                .getMessageChannel()
                .sendMessage(cat + " meows at " + target.getAsMention())
                .queue();
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
                .map(Suggestion::suggestion);
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

        /**
         * Returns the cat breed.
         *
         * @return the breed
         */
        @NonNull CatBreed breed();
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

    public enum CatBreed {
        RAGDOLL,
        SPYHNX,
        BURMESE
    }
}
