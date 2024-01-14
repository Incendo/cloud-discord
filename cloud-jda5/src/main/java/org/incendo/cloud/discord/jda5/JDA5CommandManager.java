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
import io.leangen.geantyref.TypeToken;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.EventListener;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.discord.slash.CommandScope;
import org.incendo.cloud.discord.slash.DiscordSetting;

/**
 * Command manager for JDA5.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public class JDA5CommandManager<C> extends CommandManager<C> {

    public static final CloudKey<JDAInteraction> CONTEXT_JDA_INTERACTION = CloudKey.of(
            "cloud:jda_interaction",
            JDAInteraction.class
    );
    public static final CloudKey<ReplySetting<?>> META_REPLY_SETTING = CloudKey.of(
            "cloud:reply_setting",
            new TypeToken<ReplySetting<?>>() {
            }
    );

    private final JDAInteraction.InteractionMapper<C> senderMapper;
    private final Configurable<DiscordSetting> discordSettings;

    private BiPredicate<C, String> permissionPredicate;
    private JDACommandFactory<C> commandFactory;

    /**
     * Creates a new command manager.
     *
     * @param executionCoordinator execution coordinator instance
     * @param senderMapper         mapper from {@link JDAInteraction} to {@link C}
     */
    public JDA5CommandManager(
            final @NonNull ExecutionCoordinator<C> executionCoordinator,
            final JDAInteraction.@NonNull InteractionMapper<C> senderMapper
    ) {
        super(executionCoordinator, CommandRegistrationHandler.nullCommandRegistrationHandler());
        this.commandFactory = new StandardJDACommandFactory<>(this.commandTree());
        this.discordSettings = Configurable.enumConfigurable(DiscordSetting.class);
        this.permissionPredicate = (sender, permission) -> true;
        this.senderMapper = senderMapper;
        this.registerCommandPostProcessor(new ReplyCommandPostprocessor<>(this));

        this.discordSettings.set(DiscordSetting.AUTO_REGISTER_SLASH_COMMANDS, true);
        this.registerDefaultExceptionHandlers();
    }

    @Override
    public boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        return this.permissionPredicate.test(sender, permission);
    }

    /**
     * Returns the command factory.
     *
     * @return the command factory
     */
    public final @NonNull JDACommandFactory<C> commandFactory() {
        return this.commandFactory;
    }

    /**
     * Sets the command factory.
     *
     * @param commandFactory command factory
     */
    public final void commandFactory(final @NonNull JDACommandFactory<C> commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Returns the mapper that maps from the Discord {@link User} to the sender of type {@link C}.
     *
     * @return the sender mapper
     */
    public final JDAInteraction.@NonNull InteractionMapper<C> senderMapper() {
        return this.senderMapper;
    }

    /**
     * Creates an event listener.
     *
     * @return the listener
     */
    public final @NonNull EventListener createListener() {
        return new CommandListener<>(this);
    }

    /**
     * Returns the Discord settings.
     *
     * @return discord settings
     */
    public final @NonNull Configurable<DiscordSetting> discordSettings() {
        return this.discordSettings;
    }

    /**
     * Sets the permission predicate.
     *
     * @param permissionPredicate permission predicate
     */
    public final void permissionPredicate(final @NonNull BiPredicate<C, String> permissionPredicate) {
        this.permissionPredicate = permissionPredicate;
    }

    /**
     * Registers global commands.
     *
     * @param jda JDA instance
     */
    public void registerGlobalCommands(final @NonNull JDA jda) {
        jda.updateCommands()
                .addCommands(this.commandFactory.createCommands(CommandScope.global()))
                .queue();
    }

    /**
     * Registers guild commands.
     *
     * @param guild guild to register commands to
     */
    public void registerGuildCommands(final @NonNull Guild guild) {
        guild.updateCommands()
                .addCommands(this.commandFactory.createCommands(CommandScope.guilds(-1, guild.getIdLong())))
                .queue();
    }

    @SuppressWarnings("unchecked")
    private void registerDefaultExceptionHandlers() {
        final BiConsumer<CommandContext<C>, String> sendMessage = (context, message) -> {
            final JDAInteraction interaction = context.get(CONTEXT_JDA_INTERACTION);
            final ReplySetting<C> replySetting = (ReplySetting<C>) context
                    .getOrDefault(META_REPLY_SETTING, null);

            if (replySetting == null && this.discordSettings().get(DiscordSetting.EPHEMERAL_ERROR_MESSAGES)) {
                interaction.replyCallback().deferReply(true).queue();
                interaction.interactionEvent().getHook().sendMessage(message).queue();
            } else if (replySetting != null && replySetting.defer()) {
                interaction.interactionEvent().getHook().sendMessage(message).queue();
            } else {
                interaction.replyCallback().reply(message).queue();
            }
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
        ).registerHandler(InvalidCommandSenderException.class,
                ctx -> sendMessage.accept(ctx.context(), ctx.exception().getMessage())
        ).registerHandler(InvalidSyntaxException.class,
                ctx -> sendMessage.accept(ctx.context(),
                        "Invalid Command Syntax. Correct command syntax is: /" + ctx.exception().correctSyntax()));
    }
}
