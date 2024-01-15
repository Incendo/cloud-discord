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
package org.incendo.cloud.discord.jda5.dummy;

import java.util.EnumSet;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL, since = "1.0.0")
public final class DummyUser implements User {

    private static final DummyUser INSTANCE = new DummyUser();

    /**
     * Returns the dummy user.
     *
     * @return dummy user
     */
    public static @NonNull User dummy() {
        return INSTANCE;
    }

    private DummyUser() {
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getGlobalName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDiscriminator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAvatarId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheRestAction<Profile> retrieveProfile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAsTag() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPrivateChannel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheRestAction<PrivateChannel> openPrivateChannel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Guild> getMutualGuilds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSystem() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JDA getJDA() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnumSet<UserFlag> getFlags() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFlagsRaw() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultAvatarId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAsMention() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getIdLong() {
        throw new UnsupportedOperationException();
    }
}
