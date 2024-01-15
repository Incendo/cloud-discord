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

import io.leangen.geantyref.TypeToken;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.common.truth.Truth.assertThat;

class StandardOptionRegistryTest {

    private StandardOptionRegistry<Object> optionRegistry;

    @BeforeEach
    void setup() {
        this.optionRegistry = new StandardOptionRegistry<>();
    }

    @ParameterizedTest
    @MethodSource("testDefaultMappingsSource")
    void testDefaultMappings(final @NonNull Class<?> clazz, final @NonNull DiscordOptionType<?> expected) {
        // Act
        final DiscordOptionType<?> actual = this.optionRegistry.getOption(TypeToken.get(clazz));

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> testDefaultMappingsSource() {
        return Stream.of(
                Arguments.arguments(String.class, DiscordOptionType.STRING),
                Arguments.arguments(Integer.class, DiscordOptionType.INTEGER),
                Arguments.arguments(Boolean.class, DiscordOptionType.BOOLEAN),
                Arguments.arguments(Double.class, DiscordOptionType.NUMBER)
        );
    }
}
