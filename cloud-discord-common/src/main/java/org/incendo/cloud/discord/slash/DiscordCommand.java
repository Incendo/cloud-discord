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
package org.incendo.cloud.discord.slash;

import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.discord.immutables.StagedImmutableBuilder;

/**
 * Represents a Discord command.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@StagedImmutableBuilder
@Value.Immutable
@API(status = API.Status.STABLE, since = "1.0.0")
public interface DiscordCommand<C> extends Named {

    /**
     * Creates a new command builder.
     *
     * @param <C> command sender type
     * @return the builder
     */
    static <C> ImmutableDiscordCommand.@NonNull NameBuildStage<C> builder() {
        return ImmutableDiscordCommand.builder();
    }

    /**
     * Returns the command name.
     *
     * @return the name
     */
    @Override
    @NonNull String name();

    /**
     * Returns the command description.
     *
     * @return the description
     */
    @NonNull String description();

    /**
     * Returns the command options.
     *
     * @return the options
     */
    @NonNull List<@NonNull DiscordOption<C>> options();
}
