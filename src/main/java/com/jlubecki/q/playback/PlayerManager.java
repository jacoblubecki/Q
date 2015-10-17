/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Jacob Lubecki
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jlubecki.q.playback;

import com.jlubecki.q.QTrack;
import com.jlubecki.q.logging.LogLevel;
import com.jlubecki.q.logging.QLog;
import org.intellij.lang.annotations.RegExp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Manages the active player.
 */
public final class PlayerManager {

    private static PlayerManager instance;

    private final Map<String, Player> players = new HashMap<String, Player>();
    private Player current;

    private PlayerManager() {
        // Singleton instance.
    }

    public static synchronized PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    public void registerPlayer(@RegExp String uriPattern, Player player) {
        if (players.containsKey(uriPattern)) {
            IllegalStateException exception =
                    new IllegalStateException("Can't have two players registered to the same URI.");

            if (QLog.shouldIgnoreIllegalStates()) {
                QLog.e(exception, this.getClass().getSimpleName(), exception.getMessage());
            } else {
                throw exception;
            }
        } else {
            log("Registered to play tracks with URI: " + uriPattern);
            players.put(uriPattern, player);
        }
    }

    public void unregisterPlayer(@RegExp String uriPattern) {
        if (players.containsKey(uriPattern)) {
            log("Player unregistered. " +
                    "URI: " + uriPattern + "    " +
                    "Name: " + players.get(uriPattern).getClass().getSimpleName());

            players.remove(uriPattern);
        } else {
            IllegalStateException exception =  new IllegalStateException("Player was never registered.");

            if (QLog.shouldIgnoreIllegalStates()) {
                QLog.e(exception, this.getClass().getSimpleName(), exception.getMessage());
            } else {
                throw exception;
            }
        }
    }

    /**
     * Releases all players and then clears the list of players.
     */
    public void removeAllPlayers() {
        log("All players removed.");

        for (Entry<String, Player> stringPlayerEntry : players.entrySet()) {
            stringPlayerEntry.getValue().release();
        }
        players.clear();
    }

    public void updatePlayer(QTrack track) {

        for (Entry<String, Player> stringPlayerEntry : players.entrySet()) {
            Pattern uriPattern = Pattern.compile(stringPlayerEntry.getKey());

            if (uriPattern.matcher(track.uri).find()) {
                current = stringPlayerEntry.getValue();

                log("Current player is now: " + current.getClass().getSimpleName());

                return;
            }
        }

        IllegalStateException exception = new IllegalStateException("No player found to handle the given track.");

        if (QLog.shouldIgnoreIllegalStates()) {
            QLog.e(exception, this.getClass().getSimpleName(), exception.getMessage());
        } else {
            throw exception;
        }
    }

    public Player getCurrentPlayer() {
        return current;
    }

    public void prepare(String uri) {
        current.prepare(uri);
    }

    public void justPrepare(String uri) {
        current.justPrepare(uri);
    }

    public void play() {
        current.play();
    }

    public void pause() {
        current.pause();
    }

    public void seekTo(int time) {
        current.seekTo(time);
    }

    public void stop() {
        current.stop();
    }

    public void release() {
        instance = null;
    }

    private void log(String message) {
        if (QLog.getLogLevel() == LogLevel.PLAYER || QLog.getLogLevel() == LogLevel.FULL) {
            QLog.i(this.getClass().getSimpleName(), message);
        }
    }
}
