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

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.discord.slash.NullableParser;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ParserDescriptor;

/**
 * A parser which wraps a JDA {@link OptionMapping}.
 *
 * @param <C> command sender type
 * @param <T> JDA type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public final class JDAParser<C, T> extends NullableParser<C, T> {

    /**
     * Returns a parser which extracts a {@link User}.
     *
     * @param <C> command sender type
     * @return the parser
     */
    public static <C> @NonNull ParserDescriptor<C, User> userParser() {
        return createParser(OptionMapping::getAsUser, User.class);
    }

    /**
     * Returns a parser which extracts a {@link Channel}.
     *
     * @param <C> command sender type
     * @return the parser
     */
    public static <C> @NonNull ParserDescriptor<C, Channel> channelParser() {
        return createParser(OptionMapping::getAsChannel, Channel.class);
    }

    /**
     * Returns a parser which extracts a {@link Role}.
     *
     * @param <C> command sender type
     * @return the parser
     */
    public static <C> @NonNull ParserDescriptor<C, Role> roleParser() {
        return createParser(OptionMapping::getAsRole, Role.class);
    }

    /**
     * Returns a parser which extracts a {@link IMentionable}.
     *
     * @param <C> command sender type
     * @return the parser
     */
    public static <C> @NonNull ParserDescriptor<C, IMentionable> mentionableParser() {
        return createParser(OptionMapping::getAsMentionable, IMentionable.class);
    }

    /**
     * Returns a parser which extracts an {@link Message.Attachment}.
     *
     * @param <C> command sender type
     * @return the parser
     */
    public static <C> @NonNull ParserDescriptor<C, Message.Attachment> attachmentParser() {
        return createParser(OptionMapping::getAsAttachment, Message.Attachment.class);
    }

    private static <C, T> @NonNull ParserDescriptor<C, T> createParser(
            final @NonNull Function<@NonNull OptionMapping, @Nullable T> extractor,
            final @NonNull Class<T> clazz
    ) {
        return ParserDescriptor.of(new JDAParser<>(extractor), clazz);
    }

    private final Function<@NonNull OptionMapping, @Nullable T> extractor;

    private JDAParser(final @NonNull Function<@NonNull OptionMapping, @Nullable T> extractor) {
        this.extractor = extractor;
    }

    @Override
    public @NonNull CompletableFuture<@Nullable ArgumentParseResult<T>> parseNullable(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final JDAInteraction interaction = commandContext.get(JDA5CommandManager.CONTEXT_JDA_INTERACTION);
        return interaction.getOptionMapping(commandInput.readString())
                .map(mapping -> {
                    try {
                        return this.extractor.apply(mapping);
                    } catch (final IllegalStateException ignored) {
                        return null;
                    }
                })
                .map(ArgumentParseResult::successFuture)
                .orElseGet(() -> CompletableFuture.completedFuture(null));
    }
}
