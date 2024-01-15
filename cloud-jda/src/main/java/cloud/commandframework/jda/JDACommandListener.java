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
package cloud.commandframework.jda;

import java.util.List;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * JDA Command Listener
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("deprecation")
public class JDACommandListener<C> extends ListenerAdapter {

    private final JDACommandManager<C> commandManager;
    private final String idString; // just because making strings every message is slow

    /**
     * Construct a new JDA Command Listener
     *
     * @param commandManager Command Manager instance
     */
    public JDACommandListener(final @NonNull JDACommandManager<C> commandManager) {
        this.commandManager = commandManager;
        this.idString = Long.toString(commandManager.getBotId());
    }

    @Override
    public final void onMessageReceived(final @NonNull MessageReceivedEvent event) {
        final Message message = event.getMessage();
        final C sender = this.commandManager.senderMapper().map(event);

        if (this.commandManager.getBotId() == event.getAuthor().getIdLong()) {
            return;
        }

        final String content = message.getContentRaw();

        final String prefix = this.startsWithPrefix(content, sender);

        if (prefix == null) {
            return;
        }

        this.commandManager.commandExecutor().executeCommand(sender, content.substring(prefix.length()));
    }

    /**
     * Returns whether the raw content starts with any of the prefixes. Returns {@code null} if no matching prefix is found.
     *
     * @param rawContent raw string content
     * @param sender     command sender
     * @return the prefix it begins with. Returns {@code null} if none.
     */
    private @Nullable String startsWithPrefix(final String rawContent, final C sender) {
        final String primaryPrefix = this.commandManager.getPrefixMapper().apply(sender);
        final List<String> auxiliaryPrefixes = this.commandManager.getAuxiliaryPrefixMapper().apply(sender);

        if (rawContent.startsWith(primaryPrefix)) { // first match primary prefix
            return primaryPrefix;
        }

        for (final String auxiliaryPrefix : auxiliaryPrefixes) { // second, match auxiliary prefixes
            if (rawContent.startsWith(auxiliaryPrefix)) {
                return auxiliaryPrefix;
            }
        }


        if (rawContent.startsWith("<@")) { // last, match against bot mention
            final int angleClose = rawContent.indexOf('>');
            if (angleClose != -1) {
                final StringBuilder match = new StringBuilder();
                final int prefixSize;

                if (rawContent.startsWith("<@!")) {
                    prefixSize = 3;
                } else {
                    prefixSize = 2;
                }

                if (!rawContent.substring(prefixSize, angleClose).equals(this.idString)) {
                    return null;
                }

                match.append(rawContent, prefixSize, angleClose + 1);

                if (rawContent.charAt(angleClose + 1) == ' ') {
                    match.append(' ');
                }

                return match.toString();
            }
        }


        return null;
    }
}
