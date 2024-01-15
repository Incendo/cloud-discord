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

import java.awt.Color;
import java.util.Collection;
import java.util.EnumSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.RoleIcon;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

@API(status = API.Status.INTERNAL, since = "1.0.0")
public final class DummyRole implements Role {

    private static final DummyRole INSTANCE = new DummyRole();

    /**
     * Returns the dummy role.
     *
     * @return dummy role
     */
    public static @NonNull Role dummy() {
        return INSTANCE;
    }

    private DummyRole() {
    }

    @Override
    public int getPosition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPositionRaw() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isManaged() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHoisted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMentionable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getPermissionsRaw() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Color getColor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColorRaw() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPublicRole() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canInteract(final Role role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Guild getGuild() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnumSet<Permission> getPermissions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnumSet<Permission> getPermissions(final GuildChannel channel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnumSet<Permission> getPermissionsExplicit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnumSet<Permission> getPermissionsExplicit(final GuildChannel channel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPermission(final Permission... permissions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPermission(final Collection<Permission> permissions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPermission(final GuildChannel channel, final Permission... permissions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPermission(final GuildChannel channel, final Collection<Permission> permissions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSync(final IPermissionContainer targetChannel, final IPermissionContainer syncSource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSync(final IPermissionContainer channel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RoleAction createCopy(final Guild guild) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RoleManager getManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuditableRestAction<Void> delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JDA getJDA() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RoleTags getTags() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RoleIcon getIcon() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(@NotNull final Role o) {
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
