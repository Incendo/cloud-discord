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

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import discord4j.common.util.Snowflake;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.discord.slash.NullableParser;
import reactor.core.publisher.Mono;

/**
 * A parser which wraps Discord4J options.
 *
 * @param <C> command sender type
 * @param <T> Discord4J type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public final class Discord4JParser<C, T> extends NullableParser<C, T> {

    /**
     * Creates a new {@link User} parser.
     *
     * @param <C> command sender type
     * @return user parser
     */
    public static <C> @NonNull ParserDescriptor<C, User> userParser() {
        return createParser(ApplicationCommandInteractionOptionValue::asUser, User.class);
    }

    /**
     * Creates a new {@link Role} parser.
     *
     * @param <C> command sender type
     * @return role parser
     */
    public static <C> @NonNull ParserDescriptor<C, Role> roleParser() {
        return createParser(ApplicationCommandInteractionOptionValue::asRole, Role.class);
    }

    /**
     * Creates a new {@link Channel} parser.
     *
     * @param <C> command sender type
     * @return channel parser
     */
    public static <C> @NonNull ParserDescriptor<C, Channel> channelParser() {
        return createParser(ApplicationCommandInteractionOptionValue::asChannel, Channel.class);
    }

    /**
     * Creates a new {@link Snowflake} parser.
     *
     * @param <C> command sender type
     * @return snowflake parser
     */
    public static <C> @NonNull ParserDescriptor<C, Snowflake> mentionableParser() {
        return createParser(
                value -> Mono.fromSupplier(value::asSnowflake),
                Snowflake.class
        );
    }

    /**
     * Creates a new {@link Attachment} parser.
     *
     * @param <C> command sender type
     * @return snowflake parser
     */
    public static <C> @NonNull ParserDescriptor<C, Attachment> attachmentParser() {
        return createParser(
                value -> Mono.fromSupplier(value::asAttachment),
                Attachment.class
        );
    }

    private static <C, T> @NonNull ParserDescriptor<C, T> createParser(
            final @NonNull Function<@NonNull ApplicationCommandInteractionOptionValue, @NonNull Mono<T>> extractor,
            final @NonNull Class<T> clazz
    ) {
        return ParserDescriptor.of(new Discord4JParser<>(extractor), clazz);
    }

    private final Function<@NonNull ApplicationCommandInteractionOptionValue, @NonNull Mono<T>> extractor;

    private Discord4JParser(
            final @NonNull Function<@NonNull ApplicationCommandInteractionOptionValue, @NonNull Mono<T>> extractor
    ) {
        this.extractor = extractor;
    }

    @Override
    public @NonNull CompletableFuture<@Nullable ArgumentParseResult<T>> parseNullable(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final Discord4JInteraction interaction = commandContext.get(Discord4JCommandManager.CONTEXT_DISCORD4J_INTERACTION);
        return this.findOption(interaction.commandInteraction().getOptions(), commandInput.readString())
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(this.extractor)
                .map(mono -> mono.map(ArgumentParseResult::success).toFuture())
                .orElseGet(() -> CompletableFuture.completedFuture(null));
    }

    private @NonNull Optional<ApplicationCommandInteractionOption> findOption(
            final @NonNull List<@NonNull ApplicationCommandInteractionOption> options,
            final @NonNull String name
    ) {
        for (final ApplicationCommandInteractionOption option : options) {
            if (option.getName().equalsIgnoreCase(name)) {
                return Optional.of(option);
            }
            final Optional<ApplicationCommandInteractionOption> childOption = this.findOption(option.getOptions(), name);
            if (childOption.isPresent()) {
                return childOption;
            }
        }
        return Optional.empty();
    }
}
