package org.incendo.cloud.discord.jda5.annotations;

import org.apiguardian.api.API;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation equivalent of {@link org.incendo.cloud.discord.slash.CommandScope}.
 *
 * <p>This requires the installation of {@link CommandScopeBuilderModifier}.</p>
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@API(status = API.Status.STABLE, since = "1.0.0")
public @interface CommandScope {

    /**
     * Returns the guilds that the command should exist in.
     *
     * <p>Leave this empty to make the command globally scoped. Set to {@code -1} to make it apply to all guilds.</p>
     *
     * @return guilds that the command should exist in
     */
    long[] guilds() default {};
}
