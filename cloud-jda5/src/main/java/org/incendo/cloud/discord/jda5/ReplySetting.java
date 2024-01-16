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

import cloud.commandframework.Command;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.discord.immutables.ImmutableImpl;

/**
 * Setting that determines how replies will be handled.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE, since = "1.0.0")
public interface ReplySetting<C> extends Command.Builder.Applicable<C> {

    /**
     * Returns a setting that does not defer the reply.
     *
     * @param <C> command sender type
     * @return the setting
     */
    static <C> @NonNull ReplySetting<C> doNotDefer() {
        return ReplySettingImpl.<C>of(false, false);
    }

    /**
     * Returns a setting that defers the reply.
     *
     * @param <C>       command sender type
     * @param ephemeral whether the reply is ephemeral
     * @return the setting
     */
    static <C> @NonNull ReplySetting<C> defer(final boolean ephemeral) {
        return ReplySettingImpl.<C>of(true, ephemeral);
    }

    /**
     * Whether to defer the reply.
     *
     * @return whether to defer
     */
    boolean defer();

    /**
     * Whether the reply is ephemeral.
     *
     * @return whether the reply is ephemeral.
     */
    boolean ephemeral();

    @Override
    default Command.@NonNull Builder<C> applyToCommandBuilder(Command.@NonNull Builder<C> builder) {
        return builder.meta(JDA5CommandManager.META_REPLY_SETTING, this);
    }
}
