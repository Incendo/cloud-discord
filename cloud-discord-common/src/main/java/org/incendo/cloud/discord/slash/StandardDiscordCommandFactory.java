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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.ByteParser;
import cloud.commandframework.arguments.standard.DoubleParser;
import cloud.commandframework.arguments.standard.FloatParser;
import cloud.commandframework.arguments.standard.IntegerParser;
import cloud.commandframework.arguments.standard.LongParser;
import cloud.commandframework.arguments.standard.ShortParser;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.internal.CommandNode;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.INTERNAL, since = "1.0.0")
public class StandardDiscordCommandFactory<C> implements DiscordCommandFactory<C> {

    private final OptionRegistry<C> optionRegistry;
    private final Map<Class<?>, RangeMapper<C, ?, ?>> rangeMappers = new HashMap<>();

    /**
     * Creates a new factory instance.
     *
     * @param optionRegistry option registry to retrieve option types from
     */
    public StandardDiscordCommandFactory(final @NonNull OptionRegistry<C> optionRegistry) {
        this.optionRegistry = optionRegistry;
        this.registerRangeMapper(new TypeToken<ByteParser<C>>() {
        }, parser -> {
            if (!parser.hasMin() && !parser.hasMax()) {
                return null;
            }
            return Range.of(parser.min(), parser.max());
        });
        this.registerRangeMapper(new TypeToken<ShortParser<C>>() {
        }, parser -> {
            if (!parser.hasMin() && !parser.hasMax()) {
                return null;
            }
            return Range.of(parser.min(), parser.max());
        });
        this.registerRangeMapper(new TypeToken<IntegerParser<C>>() {
        }, parser -> {
            if (!parser.hasMin() && !parser.hasMax()) {
                return null;
            }
            return Range.of(parser.min(), parser.max());
        });
        this.registerRangeMapper(new TypeToken<LongParser<C>>() {
        }, parser -> {
            if (!parser.hasMin() && !parser.hasMax()) {
                return null;
            }
            return Range.of(parser.min(), parser.max());
        });
        this.registerRangeMapper(new TypeToken<FloatParser<C>>() {
        }, parser -> {
            if (!parser.hasMin() && !parser.hasMax()) {
                return null;
            }
            return Range.of(parser.min(), parser.max());
        });
        this.registerRangeMapper(new TypeToken<DoubleParser<C>>() {
        }, parser -> {
            if (!parser.hasMin() && !parser.hasMax()) {
                return null;
            }
            return Range.of(parser.min(), parser.max());
        });
    }

    /**
     * Registers the given range {@code mapper}.
     *
     * @param <T>         type produced by parser
     * @param <P>         parser type
     * @param parserClass parser class
     * @param mapper      range mapper
     */
    public <T extends Number, P extends ArgumentParser<C, T>> void registerRangeMapper(
            final @NonNull TypeToken<P> parserClass,
            final @NonNull RangeMapper<C, T, P> mapper
    ) {
        this.rangeMappers.put(GenericTypeReflector.erase(parserClass.getType()), mapper);
    }

    @Override
    public @NonNull DiscordCommand<C> create(final @NonNull CommandNode<C> node) {
        final CommandComponent<C> component = node.component();
        final List<DiscordOption<C>> options = new ArrayList<>();

        CommandNode<C> currentNode = node;
        while (currentNode != null) {
            boolean subCommand = false;
            for (final CommandNode<C> child : currentNode.children()) {
                final DiscordOption<C> childOption = this.createOption(child);
                subCommand = subCommand || childOption instanceof DiscordOption.SubCommand;
                options.add(childOption);
            }

            // If we encountered a subcommand or a subcommand group, then we let the subcommand deal with the
            // options for us. Otherwise, we keep iterating until we've constructed the tree.
            if (subCommand) {
                break;
            }

            if (currentNode.isLeaf()) {
                currentNode = null;
            } else {
                currentNode = currentNode.children().get(0);
            }
        }

        return DiscordCommand.<C>builder()
                .name(component.name())
                .description(node.component().description().textDescription())
                .addAllOptions(options)
                .build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private @NonNull DiscordOption<C> createOption(final @NonNull CommandNode<C> node) {
        final CommandComponent<C> component = node.component();

        if (component.type() == CommandComponent.ComponentType.LITERAL) {
            // We need to determine whether to flatten the children into a sub-command
            // or whether to recursively extract the arguments.
            List<DiscordOption<C>> children = new ArrayList<>(node.children()
                    .stream()
                    .map(this::createOption)
                    .collect(Collectors.toList()));

            // If there's only one child and the child isn't a sub-command, then we recursively find the children.
            if (children.size() == 1 && children.get(0) instanceof DiscordOption.Variable) {
                children.clear();

                CommandNode<C> child = node.children().get(0);
                while (child != null) {
                    children.add(this.createOption(child));

                    if (child.isLeaf()) {
                        child = null;
                    } else {
                        child = child.children().get(0);
                    }
                }
            }

            return ImmutableSubCommand.<C>builder()
                    .name(component.name())
                    .description(component.description().textDescription())
                    .addAllOptions(children)
                    .build();
        }

        final DiscordOptionType optionType = this.optionRegistry.getOption(component.valueType());
        final Collection choices = this.extractChoices(component.suggestionProvider());
        final Range range = this.extractRange(component.parser());

        final boolean autoComplete;
        if (choices.isEmpty()) {
            autoComplete = DiscordOptionType.AUTOCOMPLETE.contains(optionType);
        } else {
            autoComplete = false;
        }

        return ImmutableVariable.<Object>builder().name(component.name()).description(component.description().textDescription())
                .type(optionType)
                .required(component.required())
                .autocomplete(autoComplete)
                .addAllChoices(choices)
                .range(range)
                .build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private @Nullable Range extractRange(final @NonNull ArgumentParser<C, ?> parser) {
        final RangeMapper rangeMapper = this.rangeMappers.get(parser.getClass());
        if (rangeMapper == null) {
            return null;
        }
        return rangeMapper.map(parser);
    }

    private @NonNull Collection<? extends @NonNull DiscordOptionChoice<?>> extractChoices(
            final @NonNull SuggestionProvider<C> suggestionProvider
    ) {
        if (!(suggestionProvider instanceof DiscordChoiceProvider)) {
            return Collections.emptyList();
        }
        return ((DiscordChoiceProvider<C, ?>) suggestionProvider).choices();
    }
}
