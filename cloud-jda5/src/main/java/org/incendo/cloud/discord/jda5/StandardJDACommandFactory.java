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

import cloud.commandframework.CommandTree;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.permission.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.slash.CommandScope;
import org.incendo.cloud.discord.slash.DiscordCommand;
import org.incendo.cloud.discord.slash.DiscordCommandFactory;
import org.incendo.cloud.discord.slash.DiscordOption;
import org.incendo.cloud.discord.slash.DiscordOptionChoice;
import org.incendo.cloud.discord.slash.DiscordOptionType;
import org.incendo.cloud.discord.slash.DiscordPermission;
import org.incendo.cloud.discord.slash.NodeProcessor;
import org.incendo.cloud.discord.slash.OptionRegistry;
import org.incendo.cloud.discord.slash.StandardDiscordCommandFactory;
import org.incendo.cloud.discord.slash.StandardOptionRegistry;

/**
 * Standard implementation of {@link JDACommandFactory}.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@API(status = API.Status.INTERNAL, since = "1.0.0")
final class StandardJDACommandFactory<C> implements JDACommandFactory<C> {

    private final CommandTree<C> commandTree;
    private final DiscordCommandFactory<C> discordCommandFactory;
    private final NodeProcessor<C> nodeProcessor;

    private CommandScopePredicate<C> commandScopePredicate = CommandScopePredicate.alwaysTrue();

    /**
     * Creates a new command factory.
     *
     * @param commandTree command tree to retrieve commands from
     */
    StandardJDACommandFactory(final @NonNull CommandTree<C> commandTree) {
        this.commandTree = Objects.requireNonNull(commandTree, "commandTree");

        final OptionRegistry<C> optionRegistry = new StandardOptionRegistry<>();
        optionRegistry
                .registerMapping(JDAOptionType.USER, JDAParser.userParser())
                .registerMapping(JDAOptionType.CHANNEL, JDAParser.channelParser())
                .registerMapping(JDAOptionType.ROLE, JDAParser.roleParser())
                .registerMapping(JDAOptionType.MENTIONABLE, JDAParser.mentionableParser())
                .registerMapping(JDAOptionType.ATTACHMENT, JDAParser.attachmentParser());

        this.discordCommandFactory = new StandardDiscordCommandFactory<>(optionRegistry);

        this.nodeProcessor = new NodeProcessor<>(this.commandTree);
    }

    @Override
    public void commandScopePredicate(final @NonNull CommandScopePredicate<C> predicate) {
        this.commandScopePredicate = Objects.requireNonNull(predicate, "predicate");
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull Collection<@NonNull CommandData> createCommands(final @NonNull CommandScope<C> scope) {
        this.nodeProcessor.prepareTree();

        final List<CommandData> commands = new ArrayList<>();
        for (final CommandNode<C> rootNode : this.commandTree.rootNodes()) {
            final CommandScope<C> rootScope = (CommandScope<C>) rootNode.nodeMeta().get(NodeProcessor.NODE_META_SCOPE);
            if (!rootScope.overlaps(scope)) {
                continue;
            }

            if (!this.commandScopePredicate.test(rootNode, scope)) {
                continue;
            }

            final DiscordCommand<C> command = this.discordCommandFactory.create(rootNode);
            SlashCommandData data = Commands.slash(command.name(), command.description());
            for (final DiscordOption<C> option : command.options()) {
                if (option instanceof DiscordOption.SubCommand) {
                    if (option.type().equals(DiscordOptionType.SUB_COMMAND)) {
                        data.addSubcommands(this.createSubCommand((DiscordOption.SubCommand<C>) option));
                    } else {
                        data.addSubcommandGroups(this.createSubCommandGroup((DiscordOption.SubCommand<C>) option));
                    }
                } else {
                    data.addOptions(this.createOption((DiscordOption.Variable<C>) option));
                }
            }

            final Permission permission = (Permission) rootNode.nodeMeta().get(CommandNode.META_KEY_PERMISSION);
            if (permission instanceof DiscordPermission) {
                data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(((DiscordPermission) permission).permission()));
            }

            commands.add(data);
        }
        return commands;
    }

    private @NonNull SubcommandData createSubCommand(final DiscordOption.@NonNull SubCommand<C> option) {
        SubcommandData subcommandData = new SubcommandData(option.name(), option.description());
        for (final DiscordOption<C> child : option.options()) {
            if (child instanceof DiscordOption.SubCommand) {
                throw new IllegalArgumentException(
                        "Cannot add subcommand " + child.name() + " as a child of subcommand " + option.name()
                );
            }
            final OptionData childOption = this.createOption((DiscordOption.Variable<C>) child);
            subcommandData.addOptions(childOption);
        }
        return subcommandData;
    }

    private @NonNull SubcommandGroupData createSubCommandGroup(final DiscordOption.@NonNull SubCommand<C> option) {
        SubcommandGroupData subcommandGroupData = new SubcommandGroupData(option.name(), option.description());
        for (final DiscordOption<C> child : option.options()) {
            if (child instanceof DiscordOption.Variable) {
                throw new IllegalArgumentException(
                        "Cannot add variable option " + child.name() + " as child of group " + option.name()
                );
            }
            subcommandGroupData = subcommandGroupData.addSubcommands(this.createSubCommand((DiscordOption.SubCommand<C>) child));
        }
        return subcommandGroupData;
    }

    private @NonNull OptionData createOption(final DiscordOption.@NonNull Variable<C> option) {
        OptionData optionData = new OptionData(
                OptionType.fromKey(option.type().value()),
                option.name(),
                option.description()
        ).setRequired(option.required());
        if (option.range() != null) {
            optionData = optionData.setMinValue(option.range().min().longValue()).setMaxValue(option.range().max().longValue());
        }
        if (option.autocomplete()) {
            optionData = optionData.setAutoComplete(true);
        } else {
            optionData = optionData.addChoices(this.createChoices(option.choices()));
        }
        return optionData;
    }

    private @NonNull Collection<Command.@NonNull Choice> createChoices(
            final @NonNull Collection<@NonNull DiscordOptionChoice<?>> choices
    ) {
        return choices.stream().map(choice -> {
            if (choice.value() instanceof Integer) {
                return new Command.Choice(choice.name(), (int) choice.value());
            } else if (choice.value() instanceof Double) {
                return new Command.Choice(choice.name(), (double) choice.value());
            }
            return new Command.Choice(choice.name(), choice.value().toString());
        }).collect(Collectors.toList());
    }
}
