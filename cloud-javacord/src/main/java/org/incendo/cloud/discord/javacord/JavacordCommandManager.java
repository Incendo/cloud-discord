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
package org.incendo.cloud.discord.javacord;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.discord.javacord.sender.JavacordCommandSender;
import org.incendo.cloud.discord.javacord.sender.JavacordServerSender;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.key.CloudKey;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;

public class JavacordCommandManager<C> extends CommandManager<C> {

    private static final Logger LOGGER = LogManager.getLogger(JavacordCommandManager.class);

    public static final CloudKey<JavacordCommandSender> JAVACORD_COMMAND_SENDER_KEY = CloudKey.of(
            "__internal_javacord_sender__",
            JavacordCommandSender.class
    );

    private final DiscordApi discordApi;
    private final Function<@NonNull JavacordCommandSender, @NonNull C> commandSenderMapper;
    private final Function<@NonNull C, @NonNull JavacordCommandSender> backwardsCommandSenderMapper;

    private final Function<@NonNull C, @NonNull String> commandPrefixMapper;
    private final BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> commandPermissionMapper;

    /**
     * Construct a new Javacord command manager
     *
     * @param discordApi                   Instance of {@link DiscordApi} used to register listeners
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link Object} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link Object}
     * @param commandPrefixMapper          Function that maps the command sender type to the command prefix
     * @param commandPermissionMapper      Function used to check if a command sender has the permission to execute a command
     */
    @SuppressWarnings("unchecked")
    public JavacordCommandManager(
            final @NonNull DiscordApi discordApi,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull Function<@NonNull JavacordCommandSender, @NonNull C> commandSenderMapper,
            final @NonNull Function<@NonNull C,
                    @NonNull JavacordCommandSender> backwardsCommandSenderMapper,
            final @NonNull Function<@NonNull C, @NonNull String> commandPrefixMapper,
            final @Nullable BiFunction<@NonNull C,
                    @NonNull String, @NonNull Boolean> commandPermissionMapper
    ) {
        super(commandExecutionCoordinator, new JavacordRegistrationHandler<>());
        ((JavacordRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);
        this.discordApi = discordApi;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;

        this.commandPrefixMapper = commandPrefixMapper;
        this.commandPermissionMapper = commandPermissionMapper;

        this.registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);
        this.registerDefaultExceptionHandlers();
    }

    @Override
    public final boolean hasPermission(
            final @NonNull C sender, final @NonNull String permission
    ) {
        if (permission.isEmpty()) {
            return true;
        }

        if (this.commandPermissionMapper != null) {
            return this.commandPermissionMapper.apply(sender, permission);
        }

        final JavacordCommandSender commandSender = this.backwardsCommandSenderMapper.apply(sender);
        if (!(commandSender instanceof JavacordServerSender)) {
            return false;
        }

        final Optional<User> authorOptional = commandSender.getAuthor().asUser();
        if (!authorOptional.isPresent()) {
            return false;
        }

        final JavacordServerSender serverSender = (JavacordServerSender) commandSender;
        return serverSender.getServer().hasPermission(authorOptional.get(), PermissionType.valueOf(permission));
    }

    final @NonNull Function<@NonNull JavacordCommandSender, @NonNull C> commandSenderMapper() {
        return this.commandSenderMapper;
    }

    /**
     * Gets the current command prefix
     *
     * @param sender Sender used to get the prefix (probably won't used anyways)
     * @return the command prefix
     */
    public @NonNull String getCommandPrefix(final @NonNull C sender) {
        return this.commandPrefixMapper.apply(sender);
    }

    /**
     * Returns the DiscordApi instance.
     *
     * @return current DiscordApi instance
     */
    public @NonNull DiscordApi discordApi() {
        return this.discordApi;
    }

    private void registerDefaultExceptionHandlers() {
        this.registerDefaultExceptionHandlers(
                triplet -> {
                    final CommandContext<C> context = triplet.first();
                    final String message = context.formatCaption(triplet.second(), triplet.third());
                    final JavacordCommandSender commandSender = context.get(JAVACORD_COMMAND_SENDER_KEY);

                    commandSender.sendErrorMessage(message);
                },
                pair -> LOGGER.error(pair.first(), pair.second())
        );
    }
}
