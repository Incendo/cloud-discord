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

import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandTree;
import org.incendo.cloud.discord.slash.CommandScope;
import org.incendo.cloud.discord.slash.CommandScopePredicate;
import org.incendo.cloud.discord.slash.DiscordCommand;
import org.incendo.cloud.discord.slash.DiscordCommandFactory;
import org.incendo.cloud.discord.slash.DiscordOption;
import org.incendo.cloud.discord.slash.DiscordOptionChoice;
import org.incendo.cloud.discord.slash.NodeProcessor;
import org.incendo.cloud.discord.slash.OptionRegistry;
import org.incendo.cloud.discord.slash.StandardDiscordCommandFactory;
import org.incendo.cloud.discord.slash.StandardOptionRegistry;
import org.incendo.cloud.internal.CommandNode;

@API(status = API.Status.STABLE, since = "1.0.0")
final class StandardDiscord4JCommandFactory<C> implements Discord4JCommandFactory<C> {

    private final CommandTree<C> commandTree;
    private final DiscordCommandFactory<C> discordCommandFactory;
    private final NodeProcessor<C> nodeProcessor;

    private CommandScopePredicate<C> commandScopePredicate = CommandScopePredicate.alwaysTrue();

    StandardDiscord4JCommandFactory(final @NonNull Discord4JCommandManager<C> commandManager) {
        this.commandTree = commandManager.commandTree();

        final OptionRegistry<C> optionRegistry = new StandardOptionRegistry<>();
        optionRegistry
                .registerMapping(Discord4JOptionType.USER, Discord4JParser.userParser())
                .registerMapping(Discord4JOptionType.CHANNEL, Discord4JParser.channelParser())
                .registerMapping(Discord4JOptionType.ROLE, Discord4JParser.roleParser())
                .registerMapping(Discord4JOptionType.MENTIONABLE, Discord4JParser.mentionableParser())
                .registerMapping(Discord4JOptionType.ATTACHMENT, Discord4JParser.attachmentParser());

        this.discordCommandFactory = new StandardDiscordCommandFactory<>(optionRegistry);

        this.nodeProcessor = new NodeProcessor<>(this.commandTree);
    }

    @Override
    public @NonNull List<@NonNull ApplicationCommandRequest> createCommands(final @NonNull CommandScope<C> scope) {
        this.nodeProcessor.prepareTree();

        final List<ApplicationCommandRequest> commands = new ArrayList<>();
        for (final CommandNode<C> rootNode : this.commandTree.rootNodes()) {
            final CommandScope<C> rootScope = (CommandScope<C>) rootNode.nodeMeta().get(NodeProcessor.NODE_META_SCOPE);
            if (!rootScope.overlaps(scope)) {
                continue;
            }

            if (!this.commandScopePredicate.test(rootNode, scope)) {
                continue;
            }

            final DiscordCommand<C> command = this.discordCommandFactory.create(rootNode);
            final ApplicationCommandRequest request = ApplicationCommandRequest.builder()
                    .name(command.name())
                    .description(command.description())
                    .addAllOptions(this.createOptions(command.options()))
                    .build();
            commands.add(request);
        }

        return commands;
    }

    @Override
    public void commandScopePredicate(final @NonNull CommandScopePredicate<C> predicate) {
        this.commandScopePredicate = Objects.requireNonNull(predicate, "predicate");
    }

    private @NonNull List<@NonNull ApplicationCommandOptionData> createOptions(
            final @NonNull List<@NonNull DiscordOption<C>> options
    ) {
       return options.stream()
               .map(this::createOption)
               .collect(Collectors.toList());
    }

    private @NonNull ApplicationCommandOptionData createOption(final @NonNull DiscordOption<C> option) {
        final ImmutableApplicationCommandOptionData.Builder builder = ApplicationCommandOptionData.builder()
                .name(option.name())
                .description(option.description())
                .type(option.type().value());

        if (option instanceof DiscordOption.SubCommand) {
            builder.options(this.createOptions(((DiscordOption.SubCommand<C>) option).options()));
        } else if (option instanceof DiscordOption.Variable) {
            final DiscordOption.Variable<C> variable = (DiscordOption.Variable<C>) option;
            builder.required(variable.required())
                    .autocomplete(variable.autocomplete());

            for (final DiscordOptionChoice<?> choice : variable.choices()) {
                builder.addChoice(ApplicationCommandOptionChoiceData.builder()
                        .name(choice.name())
                        .value(choice.value())
                        .build());
            }

            if (variable.range() != null) {
                builder.minValue(variable.range().min().doubleValue()).maxValue(variable.range().max().doubleValue());
            }
        }

        return builder.build();
    }
}
