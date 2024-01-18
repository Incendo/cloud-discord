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
package org.incendo.cloud.discord.kord

import cloud.commandframework.Command
import cloud.commandframework.kotlin.MutableCommandBuilder
import cloud.commandframework.permission.Permission
import dev.kord.common.entity.Permissions
import org.incendo.cloud.discord.slash.DiscordPermission

/**
 * Returns the Cloud equivalent of this instance.
 */
public fun Permissions.asCloudPermission(): Permission = DiscordPermission.discordPermission(this.code.value)

/**
 * Sets the command [permissions].
 */
public fun <C : Any> MutableCommandBuilder<C>.permissions(permissions: Permissions) {
    permission(permissions.asCloudPermission())
}

/**
 * Sets the command [permissions].
 */
public fun <C : Any> MutableCommandBuilder<C>.permissions(vararg permission: dev.kord.common.entity.Permission) {
    permissions(Permissions(permission.asList()))
}

/**
 * Sets the command [permissions].
 */
public fun <C : Any> Command.Builder<C>.permissions(permissions: Permissions) {
    permission(permissions.asCloudPermission())
}

/**
 * Sets the command [permissions].
 */
public fun <C : Any> Command.Builder<C>.permissions(vararg permission: dev.kord.common.entity.Permission) {
    permissions(Permissions(permission.asList()))
}
