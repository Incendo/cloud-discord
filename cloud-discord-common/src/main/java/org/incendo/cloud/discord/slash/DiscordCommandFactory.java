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

import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = API.Status.STABLE, since = "1.0.0")
public interface DiscordCommandFactory<C> {

    /**
     * Creates the command that represent the given {@code node}.
     *
     * @param node cloud node
     * @return the option
     */
    @NonNull DiscordCommand<C> create(@NonNull CommandNode<C> node);

    /**
     * Sets the suggestion registration mapper.
     *
     * <p>The suggestion registration mapper determines how {@link SuggestionProvider}s are handled during registration of the
     * command. {@link DiscordChoices} can be returned to use native Discord choices, or
     * {@link SuggestionProvider#noSuggestions()} can be returned to disable auto-complete for the argument.</p>
     *
     * @param suggestionRegistrationMapper The function to map suggestion providers to other suggestion provider during
     *                                     registration of the command to discord.
     */
    void suggestionRegistrationMapper(@NonNull Function<SuggestionProvider<C>,
                                         SuggestionProvider<C>> suggestionRegistrationMapper);

    /**
     * Returns the current suggestion registration mapper.
     *
     * @see DiscordCommandFactory#suggestionRegistrationMapper(Function)
     * @return The current set suggestion registration mapper.
     */
    @NonNull Function<SuggestionProvider<C>, SuggestionProvider<C>> suggestionRegistrationMapper();
}
