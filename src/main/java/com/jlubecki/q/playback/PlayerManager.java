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

    /**
     * @return the singleton instance of the PlayerManager.
     */
    public static synchronized PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    /**
     * Adds a player to PlayerManager to handle the given URI pattern.
     *
     * @param uriPattern the pattern to register with the PlayerManager.
     * @param player the player associated with the pattern.
     */
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

    /**
     * Removes a player from the PlayerManager that matches the given URI.
     *
     * @param uriPattern the URI pattern that should no longer be associated with the PlayerManager.
     */
    public void unregisterPlayer(@RegExp String uriPattern) {
        if (players.containsKey(uriPattern)) {
            log(String.format("Player unregistered. URI: %s    Name: %s", uriPattern,
                    players.get(uriPattern).getClass().getSimpleName()));

            players.get(uriPattern).release();
            players.remove(uriPattern);
        } else {
            IllegalStateException exception = new IllegalStateException("Player was never registered.");

            if (QLog.shouldIgnoreIllegalStates()) {
                QLog.e(exception, this.getClass().getSimpleName(), exception.getMessage());
            } else {
                throw exception;
            }
        }
    }

    /**
     * Releases all players and then clears the list of registered players.
     */
    public void removeAllPlayers() {
        log("All players removed.");

        for (Entry<String, Player> stringPlayerEntry : players.entrySet()) {
            stringPlayerEntry.getValue().release();
        }
        players.clear();
    }

    /**
     * Updates the current player in the Q. Don't call this method.
     *
     * @param track track that needs to be played.
     */
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

    /**
     * @return the current {@link Player} in the PlayerManager.
     */
    public Player getCurrentPlayer() {
        return current;
    }

    /**
     * Prepares a track for immediate playback.
     *
     * @param uri the URI of the track to prepare.
     */
    public void prepare(String uri) {
        current.prepare(uri);
    }

    /**
     * Prepares a track for eventual playback.
     * @param uri the URI of the track to prepare.
     */
    public void justPrepare(String uri) {
        current.justPrepare(uri);
    }

    /**
     * Calls {@link Player#play()} for the current {@link Player}.
     */
    public void play() {
        current.play();
    }

    /**
     * Calls {@link Player#play()} for the current {@link Player}.
     */
    public void pause() {
        current.pause();
    }

    /**
     * Calls {@link Player#seekTo(int)} for the current {@link Player}.
     */
    public void seekTo(int time) {
        current.seekTo(time);
    }

    /**
     * Calls {@link Player#stop()} for the current {@link Player}.
     */
    public void stop() {
        current.stop();
    }

    /**
     * Makes the instance of the PlayerManager null.
     */
    public void release() {
        instance = null;
    }

    /**
     * Logs messages for this class.
     *
     * @param message the message to log.
     */
    private void log(String message) {
        if (QLog.getLogLevel() == LogLevel.PLAYER || QLog.getLogLevel() == LogLevel.FULL) {
            QLog.i(this.getClass().getSimpleName(), message);
        }
    }
}
