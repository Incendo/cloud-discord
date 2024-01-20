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
import cloud.commandframework.arguments.aggregate.AggregateCommandParser;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.ByteParser;
import cloud.commandframework.arguments.standard.DoubleParser;
import cloud.commandframework.arguments.standard.FloatParser;
import cloud.commandframework.arguments.standard.IntegerParser;
import cloud.commandframework.arguments.standard.LongParser;
import cloud.commandframework.arguments.standard.NumberParser;
import cloud.commandframework.arguments.standard.ShortParser;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.types.range.Range;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        this.optionRegistry = Objects.requireNonNull(optionRegistry, "optionRegistry");

        this.registerRangeMapper(new TypeToken<ByteParser<C>>() {});
        this.registerRangeMapper(new TypeToken<ShortParser<C>>() {});
        this.registerRangeMapper(new TypeToken<IntegerParser<C>>() {});
        this.registerRangeMapper(new TypeToken<LongParser<C>>() {});
        this.registerRangeMapper(new TypeToken<FloatParser<C>>() {});
        this.registerRangeMapper(new TypeToken<DoubleParser<C>>() {});
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
        Objects.requireNonNull(parserClass, "parserClass");
        Objects.requireNonNull(mapper, "mapper");

        this.rangeMappers.put(GenericTypeReflector.erase(parserClass.getType()), mapper);
    }

    private <T extends Number, P extends NumberParser<C, T, ?>> void registerRangeMapper(
            final @NonNull TypeToken<P> parserClass
    ) {
        final RangeMapper<C, T, P> mapper = parser -> {
            if (!parser.hasMin() && !parser.hasMax()) {
                return null;
            }
            return parser.range();
        };
        this.registerRangeMapper(parserClass, mapper);
    }

    @Override
    public @NonNull DiscordCommand<C> create(final @NonNull CommandNode<C> node) {
        Objects.requireNonNull(node, "node");

        final CommandComponent<C> component = node.component();
        final List<DiscordOption<C>> options = new ArrayList<>();

        CommandNode<C> currentNode = node;
        while (currentNode != null) {
            boolean subCommand = false;
            for (final CommandNode<C> child : currentNode.children()) {
                final List<DiscordOption<C>> childOptions = this.createOptions(child);
                subCommand = subCommand || (childOptions.size() == 1 && childOptions.get(0) instanceof DiscordOption.SubCommand);
                options.addAll(childOptions);
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

        final String description;
        if (component.description().isEmpty()) {
            description = component.name();
        } else {
            description = component.description().textDescription();
        }

        return DiscordCommand.<C>builder()
                .name(component.name())
                .description(description)
                .addAllOptions(options)
                .build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private @NonNull List<DiscordOption<C>> createOptions(final @NonNull CommandNode<C> node) {
        final CommandComponent<C> component = node.component();

        final String description;
        if (component.description().isEmpty()) {
            description = component.name();
        } else {
            description = component.description().textDescription();
        }

        if (component.type() == CommandComponent.ComponentType.LITERAL) {
            // We need to determine whether to flatten the children into a sub-command
            // or whether to recursively extract the arguments.
            List<DiscordOption<C>> children = new ArrayList<>(node.children()
                    .stream()
                    .flatMap(child -> this.createOptions(child).stream())
                    .collect(Collectors.toList()));

            // If there's only one child and the child isn't a sub-command, then we recursively find the children.
            if (children.size() == 1 && children.get(0) instanceof DiscordOption.Variable) {
                children.clear();

                CommandNode<C> child = node.children().get(0);
                while (child != null) {
                    children.addAll(this.createOptions(child));

                    if (child.isLeaf()) {
                        child = null;
                    } else {
                        child = child.children().get(0);
                    }
                }
            }

            return Collections.singletonList(
                    ImmutableSubCommand.<C>builder()
                            .name(component.name())
                            .description(description)
                            .addAllOptions(children)
                            .build()
            );
        }

        final List<CommandComponent<C>> components;
        if (component.parser() instanceof AggregateCommandParser) {
            final AggregateCommandParser<C, ?> aggregateCommandParser = (AggregateCommandParser<C, ?>) component.parser();
            components = aggregateCommandParser.components();
        } else {
            components = Collections.singletonList(component);
        }

        return components.stream()
                .map(innerComponent -> {
                    final DiscordOptionType optionType = this.optionRegistry.getOption(innerComponent.valueType());
                    final Collection choices = this.extractChoices(innerComponent.suggestionProvider());
                    final Range<?> range = this.extractRange(innerComponent.parser());

                    final boolean autoComplete;
                    if (choices.isEmpty()) {
                        autoComplete = DiscordOptionType.AUTOCOMPLETE.contains(optionType);
                    } else {
                        autoComplete = false;
                    }

                    final String innerDescription;
                    if (innerComponent.description().isEmpty()) {
                        innerDescription = innerComponent.name();
                    } else {
                        innerDescription = innerComponent.description().textDescription();
                    }

                    return (DiscordOption<C>) ImmutableVariable.<C>builder().name(innerComponent.name())
                            .description(innerDescription)
                            .type(optionType)
                            .required(innerComponent.required())
                            .autocomplete(autoComplete)
                            .addAllChoices(choices)
                            .range(range)
                            .build();
                }).collect(Collectors.toList());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private @Nullable Range<?> extractRange(final @NonNull ArgumentParser<C, ?> parser) {
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
