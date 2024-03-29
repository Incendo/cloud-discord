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
package org.incendo.cloud.discord.jda;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.dv8tion.jda.api.JDA;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;

/**
 * Command manager for use with JDA 4
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
@SuppressWarnings("deprecation")
public class JDA4CommandManager<C> extends JDACommandManager<C> {

    /**
     * Construct a new JDA Command Manager
     *
     * @param jda                          JDA instance to register against
     * @param prefixMapper                 Function that maps the sender to a command prefix string
     * @param permissionMapper             Function used to check if a command sender has the permission to execute a command
     * @param commandExecutionCoordinator  Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                     commands. Some considerations must be made when picking a suitable execution coordinator
     *                                     for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                     when the parsers used in that particular platform are not thread safe. If you have
     *                                     commands that perform blocking operations, however, it might not be a good idea to
     *                                     use a synchronous execution coordinator. In most cases you will want to pick between
     *                                     {@link ExecutionCoordinator#simpleCoordinator()} and
     *                                     {@link ExecutionCoordinator#simpleCoordinator()}
     * @param senderMapper          Function that maps {@link JDACommandSender} to the command sender type
     * @throws InterruptedException If the jda instance does not ready correctly
     */
    public JDA4CommandManager(
            final @NonNull JDA jda,
            final @NonNull Function<@NonNull C, @NonNull String> prefixMapper,
            final @Nullable BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<JDACommandSender, C> senderMapper
    )
            throws InterruptedException {
        super(
                jda,
                prefixMapper,
                permissionMapper,
                commandExecutionCoordinator,
                SenderMapper.create(
                        sender -> senderMapper.map(JDACommandSender.of(sender)),
                        sender -> senderMapper.reverse(sender).getEvent().orElseThrow(IllegalStateException::new)
                )
        );
    }

    /**
     * Construct a new JDA Command Manager
     *
     * @param jda                          JDA instance to register against
     * @param prefixMapper                 Function that maps the sender to a command prefix string
     * @param auxiliaryPrefixMapper        Auxiliary prefix mapper used as a workaround for breaking changes until 2.0 is
     *                                     released. This has been added because there may be instances where you'd like more
     *                                     than 1 prefix to be checked against
     * @param permissionMapper             Function used to check if a command sender has the permission to execute a command
     * @param commandExecutionCoordinator  Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                     commands. Some considerations must be made when picking a suitable execution coordinator
     *                                     for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                     when the parsers used in that particular platform are not thread safe. If you have
     *                                     commands that perform blocking operations, however, it might not be a good idea to
     *                                     use a synchronous execution coordinator. In most cases you will want to pick between
     *                                     {@link ExecutionCoordinator#simpleCoordinator()} and
     *                                     {@link ExecutionCoordinator#simpleCoordinator()}
     * @param senderMapper          Function that maps {@link JDACommandSender} to the command sender type
     * @throws InterruptedException If the jda instance does not ready correctly
     */
    public JDA4CommandManager(
            final @NonNull JDA jda,
            final @NonNull Function<@NonNull C, @NonNull String> prefixMapper,
            final @NonNull Function<@NonNull C, @NonNull List<String>> auxiliaryPrefixMapper,
            final @Nullable BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<JDACommandSender, C> senderMapper
    )
            throws InterruptedException {
        super(
                jda,
                prefixMapper,
                auxiliaryPrefixMapper,
                permissionMapper,
                commandExecutionCoordinator,
                SenderMapper.create(
                        sender -> senderMapper.map(JDACommandSender.of(sender)),
                        sender -> senderMapper.reverse(sender).getEvent().orElseThrow(IllegalStateException::new)
                )
        );
    }
}
