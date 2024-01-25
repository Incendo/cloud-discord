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

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Entity
import org.incendo.cloud.annotation.specifier.Completions
import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.discord.kord.KordCommandManager
import org.incendo.cloud.discord.kord.KordInteraction
import org.incendo.cloud.discord.kord.example.Example
import org.incendo.cloud.discord.slash.annotation.CommandScopeBuilderModifier
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
import org.incendo.cloud.suggestion.Suggestion

public data class AnnotatedCommands(
    private val catRepository: CatRepository = CatRepositoryImpl()
) : Example {

    override fun register(commandManager: KordCommandManager<KordInteraction>) {
        val annotationParser = AnnotationParser(
            commandManager,
            KordInteraction::class.java
        )

        // Adds support for suspending functions.
        annotationParser.installCoroutineSupport()

        // Adds support for @CommandScope
        CommandScopeBuilderModifier.install(annotationParser)

        // Parses @Command, @Parser, @Suggestions & @ExceptionHandler...
        annotationParser.parse(this)
    }

    @Command("cat add <name> <age>")
    public suspend fun addCat(
        interaction: KordInteraction,
        @Completions("Cat,Benny,Meowy") name: String,
        @Range(min = "0", max = "20") age: Int
    ) {
        catRepository.addCat(Cat(name, age))
        interaction.respondEphemeral {
            content = "Added the cat named $name with age $age"
        }
    }

    @Command("cat remove <name>")
    public suspend fun removeCat(interaction: KordInteraction, @Argument(suggestions = "cats") name: String) {
        catRepository.removeCat(name)
        interaction.respondEphemeral {
            content = "Removed the cat named $name"
        }
    }

    @Command("cat list")
    public suspend fun listCats(interaction: KordInteraction) {
        val cats = catRepository.cats.asSequence().map(Cat::name).joinToString(", ")
        interaction.respondEphemeral {
            content = "Cats: $cats"
        }
    }

    @Command("cat meow <target> <cat>")
    public suspend fun meow(interaction: KordInteraction, @Argument(suggestions = "cats") cat: String, target: Entity) {
        interaction.deferPublicResponse().respond {
            content = "$cat meows at <@${target.id}>"
        }
    }

    @Suggestions("cats")
    public fun catNames(): Sequence<Suggestion> = catRepository.cats.asSequence().map(Cat::name).map(Suggestion::simple)

    public data class Cat(
        public val name: String,
        public val age: Int
    )

    public interface CatRepository {

        /**
         * Adds the [cat].
         */
        public fun addCat(cat: Cat)

        /**
         * Removes the cat with the given [name].
         */
        public fun removeCat(name: String)

        /**
         * An immutable view of the cats.
         */
        public val cats: Iterable<Cat>
    }

    private data class CatRepositoryImpl(private val catMap: MutableMap<String, Cat> = mutableMapOf()) : CatRepository {

        override val cats: Iterable<Cat>
            get() = catMap.values

        override fun addCat(cat: Cat) {
            catMap[cat.name] = cat
        }

        override fun removeCat(name: String) {
            catMap -= name
        }
    }
}
