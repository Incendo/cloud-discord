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

import cloud.commandframework.context.CommandContext
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.DeferredEphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import org.apiguardian.api.API

@API(status = API.Status.STABLE, since = "1.0.0")
public data class KordInteraction(
    public val command: InteractionCommand,
    public val interactionEvent: InteractionCreateEvent
) {

    /**
     * Returns the command event, if this interaction is part of a command execution.
     */
    public val commandEvent: ChatInputCommandInteractionCreateEvent?
        get() = interactionEvent as? ChatInputCommandInteractionCreateEvent

    /**
     * Returns the suggestion event, if this interaction is part of a command completion.
     */
    public val suggestionEvent: AutoCompleteInteractionCreateEvent?
        get() = interactionEvent as? AutoCompleteInteractionCreateEvent

    /**
     * Responds to the interaction with an ephemeral message.
     */
    public suspend inline fun respondEphemeral(builder: InteractionResponseCreateBuilder.() -> Unit) {
        requireNotNull(commandEvent).interaction.respondEphemeral(builder)
    }

    /**
     * Responds to the interaction with a public message.
     */
    public suspend inline fun respondPublic(builder: InteractionResponseCreateBuilder.() -> Unit) {
        requireNotNull(commandEvent).interaction.respondPublic(builder)
    }

    /**
     * Responds to the interaction with an ephemeral message.
     */
    public suspend inline fun deferEphemeralResponse(): DeferredEphemeralMessageInteractionResponseBehavior =
        requireNotNull(commandEvent).interaction.deferEphemeralResponse()

    /**
     * Responds to the interaction with a public message.
     */
    public suspend inline fun deferPublicResponse(): DeferredPublicMessageInteractionResponseBehavior =
        requireNotNull(commandEvent).interaction.deferPublicResponse()
}

/**
 * Returns the [KordInteraction] for this context.
 */
public val CommandContext<*>.interaction: KordInteraction
    get() = get(KordCommandManager.CONTEXT_INTERACTION)
