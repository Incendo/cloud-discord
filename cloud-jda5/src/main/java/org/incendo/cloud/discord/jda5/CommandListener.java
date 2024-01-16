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
package org.incendo.cloud.discord.jda5;

import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.Suggestions;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandContextFactory;
import cloud.commandframework.context.StandardCommandContextFactory;
import cloud.commandframework.util.StringUtils;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.slash.DiscordSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.INTERNAL, since = "1.0.0")
final class CommandListener<C> extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

    private final JDA5CommandManager<C> commandManager;
    private final CommandContextFactory<C> contextFactory;

    CommandListener(final @NonNull JDA5CommandManager<C> commandManager) {
        this.commandManager = Objects.requireNonNull(commandManager, "commandManager");
        this.contextFactory = new StandardCommandContextFactory<>(commandManager);
    }

    @Override
    public void onGuildReady(final @NonNull GuildReadyEvent event) {
        if (!this.commandManager.discordSettings().get(DiscordSetting.AUTO_REGISTER_SLASH_COMMANDS)) {
            return;
        }

        LOGGER.debug("Registering guild commands for guild: {}", event.getGuild());
        this.commandManager.registerGuildCommands(event.getGuild());
    }

    @Override
    public void onReady(final @NonNull ReadyEvent event) {
        if (!this.commandManager.discordSettings().get(DiscordSetting.AUTO_REGISTER_SLASH_COMMANDS)) {
            return;
        }

        LOGGER.debug("Registering global commands");
        this.commandManager.registerGlobalCommands(event.getJDA());
    }

    @Override
    public void onSlashCommandInteraction(final @NonNull SlashCommandInteractionEvent event) {
        final JDAInteraction interaction = JDAInteraction.builder()
                .user(event.getUser())
                .guild(event.getGuild())
                .replyCallback(event)
                .interactionEvent(event)
                .addAllOptionMappings(event.getOptions())
                .build();
        this.commandManager.commandExecutor().executeCommand(
                this.commandManager.senderMapper().map(interaction),
                this.extractCommandName(event),
                context -> context.store(JDA5CommandManager.CONTEXT_JDA_INTERACTION, interaction)
        );
    }

    @Override
    public void onCommandAutoCompleteInteraction(final @NonNull CommandAutoCompleteInteractionEvent event) {
        String commandName = this.extractCommandName(event);

        final String value = event.getFocusedOption().getValue();
        if (value.isEmpty()) {
            commandName = commandName + ' ';
        }

        final JDAInteraction interaction = JDAInteraction.builder()
                .user(event.getUser())
                .guild(event.getGuild())
                .replyCallback(null)
                .interactionEvent(null)
                .addAllOptionMappings(event.getOptions())
                .build();

        final CommandContext<C> context = this.contextFactory.create(
                true,
                this.commandManager.senderMapper().map(interaction)
        );
        context.store(JDA5CommandManager.CONTEXT_JDA_INTERACTION, interaction);

        try {
            final Suggestions<C, ? extends Suggestion> suggestions = this.commandManager.suggestionFactory()
                    .suggest(context, commandName)
                    .join();
            event.replyChoices(suggestions.list()
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
                                switch (event.getFocusedOption().getType()) {
                                    case INTEGER:
                                        return new Command.Choice(suggestion.suggestion(), Integer.parseInt(suggestion.suggestion()));
                                    case NUMBER:
                                        return new Command.Choice(suggestion.suggestion(),
                                                Double.parseDouble(suggestion.suggestion()));
                                    default:
                                        return new Command.Choice(suggestion.suggestion(), suggestion.suggestion());
                                }

                            })
                            .collect(Collectors.toList())
            ).queue();
        } catch (final CompletionException completionException) {
            final Throwable cause = completionException.getCause();
            // We unwrap if we can, otherwise we don't. There's no point in wrapping again.
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            }
            throw completionException;
        }
    }

    private @NonNull String extractCommandName(final @NonNull CommandInteractionPayload payload) {
        final StringBuilder command = new StringBuilder(payload.getFullCommandName());
        payload.getOptions().forEach(option -> {
            command.append(" ");
            if (JDAOptionType.JDA_TYPES.stream().anyMatch(type -> type.value() == option.getType().getKey())) {
                command.append(option.getName());
            } else {
                command.append(option.getAsString());
            }
        });
        return command.toString();
    }
}
