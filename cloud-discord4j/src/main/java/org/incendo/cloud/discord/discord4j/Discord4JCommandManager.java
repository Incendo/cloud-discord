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

import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.setting.Configurable;
import discord4j.core.GatewayDiscordClient;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.slash.DiscordSetting;
import reactor.core.publisher.Mono;

/**
 * Command manager for Discord4J.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public class Discord4JCommandManager<C> extends CommandManager<C> {

    public static final CloudKey<Discord4JInteraction> CONTEXT_DISCORD4J_INTERACTION = CloudKey.of(
            "cloud:discord4j_interaction",
            Discord4JInteraction.class
    );

    private final Discord4JInteraction.InteractionMapper<C> senderMapper;
    private final Configurable<DiscordSetting> discordSettings = Configurable.enumConfigurable(DiscordSetting.class);

    private Discord4JCommandFactory<C> commandFactory;
    private BiPredicate<C, String> permissionPredicate;

    /**
     * Creates a new command manager.
     *
     * @param executionCoordinator execution coordinator instance
     * @param senderMapper         mapper from {@link Discord4JInteraction} to {@link C}
     */
    public Discord4JCommandManager(
            final @NonNull ExecutionCoordinator<C> executionCoordinator,
            final Discord4JInteraction.@NonNull InteractionMapper<C> senderMapper
    ) {
        super(executionCoordinator, CommandRegistrationHandler.nullCommandRegistrationHandler());
        this.commandFactory = new StandardDiscord4JCommandFactory<>(this);
        this.permissionPredicate = (sender, permission) -> true;
        this.senderMapper = Objects.requireNonNull(senderMapper, "senderMapper");

        this.registerDefaultExceptionHandlers();

        this.parserRegistry()
                .registerParser(Discord4JParser.userParser())
                .registerParser(Discord4JParser.roleParser())
                .registerParser(Discord4JParser.channelParser())
                .registerParser(Discord4JParser.mentionableParser())
                .registerParser(Discord4JParser.attachmentParser());

        // Common parameter injections.
        this.parameterInjectorRegistry().registerInjector(
                Discord4JInteraction.class,
                (ctx, annotations) -> ctx.get(CONTEXT_DISCORD4J_INTERACTION)
        );

        this.discordSettings.set(DiscordSetting.EPHEMERAL_ERROR_MESSAGES, true);
    }

    @Override
    public boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        return this.permissionPredicate.test(sender, permission);
    }

    /**
     * Sets the permission predicate.
     *
     * @param permissionPredicate permission predicate
     */
    public final void permissionPredicate(final @NonNull BiPredicate<C, String> permissionPredicate) {
        this.permissionPredicate = Objects.requireNonNull(permissionPredicate, "permissionPredicate");
    }

    /**
     * Returns the sender mapper.
     *
     * @return sender mapper
     */
    public final Discord4JInteraction.@NonNull InteractionMapper<C> senderMapper() {
        return this.senderMapper;
    }

    /**
     * Returns the command factory.
     *
     * @return command factory
     */
    public final @NonNull Discord4JCommandFactory<C> commandFactory() {
        return this.commandFactory;
    }

    /**
     * Sets the command factory.
     *
     * @param commandFactory command factory
     */
    public final void commandFactory(final @NonNull Discord4JCommandFactory<C> commandFactory) {
        this.commandFactory = Objects.requireNonNull(commandFactory, "commandFactory");
    }

    /**
     * Installs the event listener using the given {@code gateway} instance.
     *
     * <p>The event listener is responsible for command synchronization.</p>
     *
     * @param gateway gateway instance
     * @return mono that represents the termination of the installation
     */
    public final @NonNull Mono<Void> installEventListener(final @NonNull GatewayDiscordClient gateway) {
        Objects.requireNonNull(gateway, "gateway");
        final Discord4JEventListener<C> eventListener = new Discord4JEventListener<>(this);
        return eventListener.install(gateway);
    }

    /**
     * Returns the Discord settings.
     *
     * @return discord settings
     */
    public final @NonNull Configurable<DiscordSetting> discordSettings() {
        return this.discordSettings;
    }

    private void registerDefaultExceptionHandlers() {
        final BiConsumer<CommandContext<C>, String> sendMessage = (context, message) -> {
            final Discord4JInteraction interaction = context.get(CONTEXT_DISCORD4J_INTERACTION);
            interaction.commandEvent().ifPresent(event -> event.reply(message)
                    .withEphemeral(this.discordSettings().get(DiscordSetting.EPHEMERAL_ERROR_MESSAGES))
                    .subscribe());
        };

        this.exceptionController().registerHandler(
                Throwable.class,
                ctx -> sendMessage.accept(ctx.context(), ctx.exception().getMessage())
        ).registerHandler(
                CommandExecutionException.class,
                ctx -> sendMessage.accept(ctx.context(), "Invalid Command Argument: " + ctx.exception().getCause().getMessage())
        ).registerHandler(
                NoSuchCommandException.class,
                ctx -> sendMessage.accept(ctx.context(), "Unknown command")
        ).registerHandler(
                NoPermissionException.class,
                ctx -> sendMessage.accept(ctx.context(), "Insufficient permissions")
        ).registerHandler(
                InvalidCommandSenderException.class,
                ctx -> sendMessage.accept(ctx.context(), ctx.exception().getMessage())
        ).registerHandler(
                InvalidSyntaxException.class,
                ctx -> sendMessage.accept(ctx.context(),
                        "Invalid Command Syntax. Correct command syntax is: /" + ctx.exception().correctSyntax()));
    }
}
