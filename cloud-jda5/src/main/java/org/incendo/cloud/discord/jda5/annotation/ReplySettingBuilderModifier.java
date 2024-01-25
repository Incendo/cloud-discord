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
package org.incendo.cloud.discord.jda5.annotation;

import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.BuilderModifier;

/**
 * Builder modifier that enables the use of {@link ReplySetting}.
 *
 * @param <C> command sender type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public final class ReplySettingBuilderModifier<C> implements BuilderModifier<ReplySetting, C> {

    /**
     * Installs the builder modifier.
     *
     * @param <C> command sender type
     * @param annotationParser annotation parser
     */
    public static <C> void install(final @NonNull AnnotationParser<C> annotationParser) {
        Objects.requireNonNull(annotationParser, "annotationParser");
        annotationParser.registerBuilderModifier(ReplySetting.class, new ReplySettingBuilderModifier<>());
    }

    @Override
    public Command.@NonNull Builder<? extends C> modifyBuilder(
            final @NonNull ReplySetting annotation,
            final Command.@NonNull Builder<C> builder
    ) {
        if (annotation.defer()) {
            return builder.apply(org.incendo.cloud.discord.jda5.ReplySetting.defer(annotation.ephemeral()));
        }
        return builder.apply(org.incendo.cloud.discord.jda5.ReplySetting.doNotDefer());
    }
}
