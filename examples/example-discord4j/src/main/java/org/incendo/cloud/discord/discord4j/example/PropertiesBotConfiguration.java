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
package org.incendo.cloud.discord.discord4j.example;

import java.io.File;
import java.io.FileReader;
import java.util.Objects;
import java.util.Properties;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link BotConfiguration} that is backed by {@link Properties}.
 */
final class PropertiesBotConfiguration implements BotConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesBotConfiguration.class);

    private final Properties properties;

    /**
     * Creates a new properties instance.
     *
     * @param properties properties
     */
    PropertiesBotConfiguration(final @NonNull Properties properties) {
        this.properties = properties;
    }

    /**
     * Creates a new configuration instance.
     *
     * @param file file to read from
     */
    PropertiesBotConfiguration(final @NonNull File file) {
        this(new Properties());
        try (FileReader reader = new FileReader(file)) {
            this.properties.load(reader);
        } catch (final Exception e) {
            LOGGER.error("Failed to load bot.properties", e);
        }
    }

    @Override
    public @NonNull String token() {
        return Objects.requireNonNull(this.properties.getProperty("token"), "missing property: token");
    }
}
