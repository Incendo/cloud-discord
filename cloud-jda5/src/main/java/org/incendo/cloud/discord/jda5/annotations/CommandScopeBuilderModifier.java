package org.incendo.cloud.discord.jda5.annotations;

import cloud.commandframework.Command;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.BuilderModifier;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Builder modifier that enables the use of {@link CommandScope}.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public class CommandScopeBuilderModifier<C> implements BuilderModifier<CommandScope, C> {

    /**
     * Installs the builder modifier.
     *
     * @param <C> command sender type
     * @param annotationParser annotation parser
     */
    public static <C> void install(final @NonNull AnnotationParser<C> annotationParser) {
        annotationParser.registerBuilderModifier(CommandScope.class, new CommandScopeBuilderModifier<>());
    }

    @Override
    public Command.@NonNull Builder<? extends C> modifyBuilder(
            final @NonNull CommandScope annotation,
            final Command.@NonNull Builder<C> builder
    ) {
        if (annotation.guilds().length == 0) {
            return builder.apply(org.incendo.cloud.discord.slash.CommandScope.global());
        }
        return builder.apply(org.incendo.cloud.discord.slash.CommandScope.guilds(annotation.guilds()));
    }
}
