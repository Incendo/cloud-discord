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
package org.incendo.cloud.discord.legacy.repository;

import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A repository for Discord objects.
 *
 * @param <G> guild type
 * @param <T> value type
 * @since 1.0.0
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public interface DiscordRepository<G, T> {

    /**
     * Returns the object by its {@code id}.
     *
     * @param id id to retrieve object by
     * @return result, or {@code null}
     */
    @Nullable T getById(long id);

    /**
     * Returns the object by its {@code id}.
     *
     * @param id id to retrieve object by
     * @return result, or {@code null}
     * @throws NumberFormatException if the given {@code id} is invalid
     */
    default @Nullable T getById(final @NonNull String id) throws NumberFormatException {
        return this.getById(Long.parseLong(id));
    }

    /**
     * Returns all objects with the given {@code name}.
     *
     * @param name name to retrieve objects by
     * @return the objects
     */
    @NonNull Collection<? extends @NonNull T> getByName(@NonNull String name);
}
