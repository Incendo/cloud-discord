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
package org.incendo.cloud.discord.jda;

import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.discord.jda.parser.ChannelParser;
import org.incendo.cloud.discord.jda.parser.MemberParser;
import org.incendo.cloud.discord.jda.parser.RoleParser;
import org.incendo.cloud.discord.jda.parser.UserParser;
import org.incendo.cloud.discord.jda.permission.BotPermissionPostProcessor;
import org.incendo.cloud.discord.jda.permission.UserPermissionPostProcessor;
import org.incendo.cloud.discord.legacy.parser.DiscordParserMode;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command manager for use with JDA
 *
 * @param <C> Command sender type
 * @deprecated Use {@link JDA4CommandManager}
 */
@Deprecated
public class JDACommandManager<C> extends CommandManager<C> implements SenderMapperHolder<MessageReceivedEvent, C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDACommandManager.class);

    private final JDA jda;
    private final long botId;

    private final Function<@NonNull C, @NonNull String> prefixMapper;
    private final Function<@NonNull C, @NonNull List<String>> auxiliaryPrefixMapper;
    private final BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper;
    private final SenderMapper<MessageReceivedEvent, C> senderMapper;

    /**
     * Construct a new JDA Command Manager
     *
     * @param jda                          JDA instance to register against
     * @param prefixMapper                 Function that maps the sender to a command prefix string
     * @param permissionMapper             Function used to check if a command sender has the permission to execute a command
     * @param commandExecutionCoordinator  Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                     commands. Some considerations must be made when picking a suitable execution coordinator
     *                                     for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                     when the parsers used in that particular platform are not thread safe. If you have
     *                                     commands that perform blocking operations, however, it might not be a good idea to
     *                                     use a synchronous execution coordinator. In most cases you will want to pick between
     *                                     {@link ExecutionCoordinator#simpleCoordinator()} and
     *                                     {@link ExecutionCoordinator#simpleCoordinator()}
     * @param senderMapper                 Function that maps {@link MessageReceivedEvent} to the command sender type
     * @throws InterruptedException If the jda instance does not ready correctly
     */
    public JDACommandManager(
            final @NonNull JDA jda,
            final @NonNull Function<@NonNull C, @NonNull String> prefixMapper,
            final @Nullable BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<MessageReceivedEvent, C> senderMapper
    ) throws InterruptedException {
        this(
                jda,
                prefixMapper,
                (c) -> Collections.emptyList(),
                permissionMapper,
                commandExecutionCoordinator,
                senderMapper
        );
    }

    /**
     * Construct a new JDA Command Manager
     *
     * @param jda                          JDA instance to register against
     * @param prefixMapper                 Function that maps the sender to a command prefix string
     * @param auxiliaryPrefixMapper        Auxiliary prefix mapper used as a workaround for breaking changes until 2.0 is
     *                                     released. This has been added because there may be instances where you'd like more
     *                                     than 1 prefix to be checked against
     * @param permissionMapper             Function used to check if a command sender has the permission to execute a command
     * @param commandExecutionCoordinator  Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                     commands. Some considerations must be made when picking a suitable execution coordinator
     *                                     for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                     when the parsers used in that particular platform are not thread safe. If you have
     *                                     commands that perform blocking operations, however, it might not be a good idea to
     *                                     use a synchronous execution coordinator. In most cases you will want to pick between
     *                                     {@link ExecutionCoordinator#simpleCoordinator()} and
     *                                     {@link ExecutionCoordinator#simpleCoordinator()}
     * @param senderMapper                 Function that maps {@link MessageReceivedEvent} to the command sender type
     * @throws InterruptedException If the jda instance does not ready correctly
     */
    public JDACommandManager(
            final @NonNull JDA jda,
            final @NonNull Function<@NonNull C, @NonNull String> prefixMapper,
            final @NonNull Function<@NonNull C, @NonNull List<String>> auxiliaryPrefixMapper,
            final @Nullable BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<MessageReceivedEvent, C> senderMapper
    )
            throws InterruptedException {
        super(commandExecutionCoordinator, CommandRegistrationHandler.nullCommandRegistrationHandler());
        this.jda = jda;
        this.prefixMapper = prefixMapper;
        this.auxiliaryPrefixMapper = auxiliaryPrefixMapper;
        this.permissionMapper = permissionMapper;
        this.senderMapper = senderMapper;
        jda.addEventListener(new JDACommandListener<>(this));
        jda.awaitReady();
        this.botId = jda.getSelfUser().getIdLong();

        /* Register JDA Preprocessor */
        this.registerCommandPreProcessor(new JDACommandPreprocessor<>(this));

        /* Register JDA Command Postprocessors */
        this.registerCommandPostProcessor(new BotPermissionPostProcessor<>());
        this.registerCommandPostProcessor(new UserPermissionPostProcessor<>());

        /* Register JDA Parsers */
        this.parserRegistry().registerParserSupplier(TypeToken.get(User.class), parserParameters ->
                new UserParser<>(
                        EnumSet.allOf(DiscordParserMode.class),
                        UserParser.Isolation.GLOBAL
                ));
        this.parserRegistry().registerParserSupplier(TypeToken.get(Member.class), parserParameters ->
                new MemberParser<>(
                        EnumSet.allOf(DiscordParserMode.class)
                ));
        this.parserRegistry().registerParserSupplier(TypeToken.get(MessageChannel.class), parserParameters ->
                new ChannelParser<>(
                        EnumSet.allOf(DiscordParserMode.class)
                ));
        this.parserRegistry().registerParserSupplier(TypeToken.get(Role.class), parserParameters ->
                new RoleParser<>(
                        EnumSet.allOf(DiscordParserMode.class)
                ));

        // No "native" command system means that we can delete commands just fine.
        this.registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);

        this.registerDefaultExceptionHandlers();
    }

    /**
     * Get the JDA instance
     *
     * @return JDA instance
     */
    public final @NonNull JDA getJDA() {
        return this.jda;
    }

    /**
     * Get the prefix mapper
     *
     * @return Prefix mapper
     */
    public final @NonNull Function<@NonNull C, @NonNull String> getPrefixMapper() {
        return this.prefixMapper;
    }

    /**
     * Get the auxiliary prefix mapper
     *
     * @return Auxiliary prefix mapper
     */
    public final @NonNull Function<C, List<String>> getAuxiliaryPrefixMapper() {
        return this.auxiliaryPrefixMapper;
    }

    /**
     * Get the bots discord id
     *
     * @return Bots discord id
     */
    public final long getBotId() {
        return this.botId;
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        if (permission.isEmpty()) {
            return true;
        }

        if (this.permissionMapper != null) {
            return this.permissionMapper.apply(sender, permission);
        }

        final JDACommandSender jdaSender =
                JDACommandSender.of(this.senderMapper.reverse(sender));

        if (!(jdaSender instanceof JDAGuildSender)) {
            return true;
        }

        final JDAGuildSender guildSender = (JDAGuildSender) jdaSender;

        return guildSender.getMember().hasPermission(Permission.valueOf(permission));
    }

    private void registerDefaultExceptionHandlers() {
        this.registerDefaultExceptionHandlers(
                triplet -> {
                    final CommandContext<C> context = triplet.first();
                    final MessageChannel messageChannel = context.get("MessageChannel");
                    final String message = context.formatCaption(triplet.second(), triplet.third());
                    messageChannel.sendMessage(message).queue();
                },
                pair -> LOGGER.error(pair.first(), pair.second())
        );
    }

    @Override
    public final @NonNull SenderMapper<MessageReceivedEvent, C> senderMapper() {
        return this.senderMapper;
    }
}
