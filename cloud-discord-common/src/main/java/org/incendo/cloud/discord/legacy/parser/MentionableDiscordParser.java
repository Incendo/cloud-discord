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
package org.incendo.cloud.discord.legacy.parser;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL, since = "1.0.0")
public abstract class MentionableDiscordParser<C, G, T> extends DiscordParser<C, G, T> {

    private final Set<DiscordParserMode> modes;

    protected MentionableDiscordParser(final @NonNull Set<DiscordParserMode> modes) {
        Objects.requireNonNull(modes, "modes");
        if (modes.isEmpty()) {
            throw new IllegalArgumentException("At least one parsing mode is required");
        }
        this.modes = modes;
    }

    /**
     * Returns the enabled modes for this parser.
     *
     * @return the modes
     */
    public @NonNull Set<DiscordParserMode> modes() {
        return Collections.unmodifiableSet(this.modes);
    }
}
