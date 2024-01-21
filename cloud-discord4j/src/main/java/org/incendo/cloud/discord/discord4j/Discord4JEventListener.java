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
package org.incendo.cloud.discord.discord4j;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandContextFactory;
import cloud.commandframework.context.StandardCommandContextFactory;
import cloud.commandframework.util.StringUtils;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionChoiceData;
import discord4j.rest.RestClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.slash.CommandScope;
import reactor.core.publisher.Mono;

@API(status = API.Status.INTERNAL, since = "1.0.0")
final class Discord4JEventListener<C> {

    private final Discord4JCommandManager<C> commandManager;
    private final CommandContextFactory<C> contextFactory;

    Discord4JEventListener(final @NonNull Discord4JCommandManager<C> commandManager) {
        this.commandManager = commandManager;
        this.contextFactory = new StandardCommandContextFactory<>(commandManager);
    }

    @NonNull Mono<Void> install(final @NonNull GatewayDiscordClient gateway) {
        return gateway.on(ReadyEvent.class, this::handleReadyEvent)
                .then()
                .and(gateway.on(GuildCreateEvent.class, this::handleGuildCreateEvent))
                .then()
                .and(gateway.on(ChatInputInteractionEvent.class, this::handleChatInputInteractionEvent))
                .then()
                .and(gateway.on(ChatInputAutoCompleteEvent.class, this::handleChatInputAutoCompleteEvent));
    }

    private @NonNull Mono<?> handleReadyEvent(final @NonNull ReadyEvent event) {
        final RestClient restClient = event.getClient().getRestClient();
        return restClient.getApplicationId().flatMap(applicationId ->
                restClient.getApplicationService()
                        .bulkOverwriteGlobalApplicationCommand(
                                applicationId,
                                this.commandManager.commandFactory().createCommands(CommandScope.global())
                        ).then()
        );
    }

    private @NonNull Mono<?> handleGuildCreateEvent(final @NonNull GuildCreateEvent event) {
        final RestClient restClient = event.getClient().getRestClient();
        final long guildId = event.getGuild().getId().asLong();
        return restClient.getApplicationId().flatMap(applicationId ->
                restClient.getApplicationService()
                        .bulkOverwriteGuildApplicationCommand(
                                applicationId,
                                guildId,
                                this.commandManager.commandFactory().createCommands(CommandScope.guilds(-1, guildId))
                        ).then()
        );
    }

    private @NonNull Mono<?> handleChatInputInteractionEvent(final @NonNull ChatInputInteractionEvent event) {
        return Mono.fromFuture(event.getInteraction().getCommandInteraction().map(interaction -> {
            final Discord4JInteraction discord4JInteraction = Discord4JInteraction.builder()
                    .commandInteraction(interaction)
                    .interactionEvent(event)
                    .build();
            return this.commandManager.commandExecutor().executeCommand(
                    this.commandManager.senderMapper().map(discord4JInteraction),
                    this.extractCommandName(interaction),
                    context -> context.store(Discord4JCommandManager.CONTEXT_DISCORD4J_INTERACTION, discord4JInteraction)
            );
        }).orElse(CompletableFuture.completedFuture(null)));
    }

    private @NonNull Mono<?> handleChatInputAutoCompleteEvent(final @NonNull ChatInputAutoCompleteEvent event) {
        return Mono.fromFuture(event.getInteraction().getCommandInteraction().map(interaction -> {
            String commandName = this.extractCommandName(interaction);

            final Optional<?> value = event.getFocusedOption().getValue();
            if (!value.isPresent()) {
                commandName = commandName + ' ';
            }

            final Discord4JInteraction discord4JInteraction = Discord4JInteraction.builder()
                    .commandInteraction(interaction)
                    .interactionEvent(event)
                    .build();
            final CommandContext<C> context = this.contextFactory.create(
                    true,
                    this.commandManager.senderMapper().map(discord4JInteraction)
            );
            context.store(Discord4JCommandManager.CONTEXT_DISCORD4J_INTERACTION, discord4JInteraction);

            return this.commandManager.suggestionFactory()
                    .suggest(context, commandName)
                    .thenApply(suggestions -> suggestions.list()
                            .stream()
                            .map(suggestion -> {
                                if (suggestion.suggestion().contains(" ")) {
                                    return suggestion.withSuggestion(StringUtils.trimBeforeLastSpace(
                                            suggestion.suggestion(),
                                            suggestions.commandInput()
                                    ));
                                }
                                return suggestion;
                            })
                            .filter(suggestion -> !suggestion.suggestion().isEmpty())
                            .map(suggestion -> {
                                final ImmutableApplicationCommandOptionChoiceData.Builder builder =
                                        ApplicationCommandOptionChoiceData.builder().name(suggestion.suggestion());
                                switch (event.getFocusedOption().getType()) {
                                    case INTEGER:
                                        return builder.value(Integer.parseInt(suggestion.suggestion())).build();
                                    case NUMBER:
                                        return builder.value(Double.parseDouble(suggestion.suggestion())).build();
                                    default:
                                        return builder.value(suggestion.suggestion()).build();
                                }
                            })
                            .collect(Collectors.toList()));
        })
                .orElseGet(() -> CompletableFuture.completedFuture(Collections.emptyList())))
                .<Iterable<ApplicationCommandOptionChoiceData>>map(ArrayList::new)
                .flatMap(event::respondWithSuggestions);
    }

    private @NonNull String extractCommandName(final @NonNull ApplicationCommandInteraction interaction) {
        final StringBuilder command = new StringBuilder();
        interaction.getName().ifPresent(command::append);
        interaction.getOptions().forEach(option -> command.append(" ").append(this.extractOptionString(option)));
        return command.toString();
    }

    private @NonNull String extractOptionString(final @NonNull ApplicationCommandInteractionOption option) {
        final StringBuilder string = new StringBuilder();
        if (option.getType() == ApplicationCommandOption.Type.SUB_COMMAND
                || option.getType() == ApplicationCommandOption.Type.SUB_COMMAND_GROUP) {
            string.append(option.getName());
            option.getOptions().forEach(inner -> string.append(" ").append(this.extractOptionString(inner)));
        } else {
            if (Discord4JOptionType.DISCORD4J_OPTION_TYPES.stream()
                    .anyMatch(type -> type.value() == option.getType().getValue())) {
                string.append(option.getName());
            } else {
                option.getValue().map(ApplicationCommandInteractionOptionValue::getRaw).ifPresent(string::append);
            }
        }
        return string.toString();
    }
}
