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

import org.apiguardian.api.API
import org.incendo.cloud.setting.Setting

/**
 * Kord-specific settings.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public enum class KordSetting : Setting {
    /**
     * Whether commands should be automatically registered per-guild. Defaults to `true`.
     */
    AUTO_REGISTER_GUILD,

    /**
     * Whether commands should be automatically registered globally. Defaults to `true`.
     */
    AUTO_REGISTER_GLOBAL,

    /**
     * Whether existing commands should be cleared. Defaults to `true`.
     */
    CLEAR_EXISTING
}
