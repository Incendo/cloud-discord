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

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.jda5.dummy.DummyChannel;
import org.incendo.cloud.discord.jda5.dummy.DummyMentionable;
import org.incendo.cloud.discord.jda5.dummy.DummyRole;
import org.incendo.cloud.discord.jda5.dummy.DummyUser;

/**
 * A parser which wraps a JDA {@link OptionMapping}.
 *
 * @param <C> command sender type
 * @param <T> JDA type
 * @since 1.0.0
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "1.0.0")
public interface JDAParser<C, T> extends ArgumentParser<C, T> {

    /**
     * Returns a parser which extracts a {@link User}.
     *
     * @param <C> command sender type
     * @return the parser
     */
    static <C> @NonNull ParserDescriptor<C, User> userParser() {
        return ParserDescriptor.of((JDAParser<C, User>) mapping -> ArgumentParseResult.success(mapping.getAsUser()), User.class);
    }

    /**
     * Returns a parser which extracts a {@link Channel}.
     *
     * @param <C> command sender type
     * @return the parser
     */
    static <C> @NonNull ParserDescriptor<C, Channel> channelParser() {
        return ParserDescriptor.of(
                (JDAParser<C, Channel>) mapping -> ArgumentParseResult.success(mapping.getAsChannel()),
                Channel.class
        );
    }

    /**
     * Returns a parser which extracts a {@link Role}.
     *
     * @param <C> command sender type
     * @return the parser
     */
    static <C> @NonNull ParserDescriptor<C, Role> roleParser() {
        return ParserDescriptor.of(
                (JDAParser<C, Role>) mapping -> ArgumentParseResult.success(mapping.getAsRole()),
                Role.class
        );
    }

    /**
     * Returns a parser which extracts a {@link IMentionable}.
     *
     * @param <C> command sender type
     * @return the parser
     */
    static <C> @NonNull ParserDescriptor<C, IMentionable> mentionableParser() {
        return ParserDescriptor.of(
                (JDAParser<C, IMentionable>) mapping -> ArgumentParseResult.success(mapping.getAsMentionable()),
                IMentionable.class
        );
    }

    /**
     * Returns a parser which extracts an {@link Message.Attachment}.
     *
     * @param <C> command sender type
     * @return the parser
     */
    static <C> @NonNull ParserDescriptor<C, Message.Attachment> attachmentParser() {
        return ParserDescriptor.of(
                (JDAParser<C, Message.Attachment>) mapping -> ArgumentParseResult.success(mapping.getAsAttachment()),
                Message.Attachment.class
        );
    }

    /**
     * Returns the result of extracting the argument from the given {@code mapping}.
     *
     * @param mapping JDA option mapping
     * @return the result
     */
    @NonNull ArgumentParseResult<T> extract(@NonNull OptionMapping mapping);

    @Override
    @SuppressWarnings("unchecked")
    default @NonNull ArgumentParseResult<@NonNull T> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final JDAInteraction interaction = commandContext.get(JDA5CommandManager.CONTEXT_JDA_INTERACTION);
        final OptionMapping mapping = interaction.getOptionMapping(commandInput.readString()).orElse(null);
        try {
            return this.extract(mapping);
        } catch (final Exception e) {
            if (commandContext.isSuggestions()) {
                switch (mapping.getType()) {
                    case USER: return (ArgumentParseResult<T>) ArgumentParseResult.success(DummyUser.dummy());
                    case CHANNEL: return (ArgumentParseResult<T>) ArgumentParseResult.success(DummyChannel.dummy());
                    case ROLE: return (ArgumentParseResult<T>) ArgumentParseResult.success(DummyRole.dummy());
                    case MENTIONABLE: return (ArgumentParseResult<T>) ArgumentParseResult.success(DummyMentionable.dummy());
                    // TODO(City): Support attachments too.
                    default: break;
                }
            }
            return ArgumentParseResult.failure(e);
        }
    }
}
