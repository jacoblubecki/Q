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

package com.lubecki.q.playback;

import com.lubecki.q.logging.LogLevel;
import com.lubecki.q.logging.QLog;
import org.jetbrains.annotations.NotNull;

/**
 * Abstraction of a player. Methods should be overridden to determine individual player behaviors.
 */
@SuppressWarnings({"StringConcatenation", "DesignForExtension", "unused", "ConstantDeclaredInAbstractClass"})
public abstract class Player {

    /**
     * This value should be used whenever the duration of a track is not a known value.
     */
    public static final int DURATION_UNKNOWN = -1;

    private PlayerEventCallback playerEventCallback;
    private PlayerState state;

    protected Player(@NotNull PlayerEventCallback playerEventCallback) {
        this.playerEventCallback = playerEventCallback;
        changeState(PlayerState.CREATED);
    }

    /**
     * Prepares a track for playback. Call {@link #notifyIfPrepared()} when this method reaches its endpoint (whether
     * synchronous or asynchronous).
     *
     * @param uri The URI of the track to prepare.
     */
    public abstract void prepare(String uri);

    /**
     * Prepares a track for playback. This method should never call {@link #notifyIfPrepared()}, this is used to make
     * sure the next track is ready before it starts.
     */
    public abstract void justPrepare(String uri);

    /**
     * Seeks to a specified time during the track.
     *
     * @param time The time to seek to in milliseconds.
     */
    public abstract void seekTo(int time);

    /**
     * Returns the current time of playback in milliseconds.
     *
     * @return the time of playback in milliseconds.
     */
    public abstract int getCurrentTime();

    /**
     * Returns the duration of the media loaded into the player.
     *
     * @return the duration of the active track in milliseconds.
     */
    public abstract int getDuration();

    public void play() {
        if (canPlay()) {
            changeState(PlayerState.PLAYING);
        } else {
            IllegalStateException exception = new IllegalStateException("Tried to play when player was " + this.state);

            if (QLog.shouldIgnoreIllegalStates()) {
                QLog.e(exception, this.getClass().getSimpleName(), exception.getMessage());
            } else {
                throw exception;
            }
        }
    }

    public void pause() {
        if (canPause()) {
            changeState(PlayerState.PAUSED);
        } else {
            IllegalStateException exception = new IllegalStateException("Tried to pause when player was " + this.state);

            if (QLog.shouldIgnoreIllegalStates()) {
                QLog.e(exception, this.getClass().getSimpleName(), exception.getMessage());
            } else {
                throw exception;
            }
        }
    }

    public void stop() {
        changeState(PlayerState.STOPPED);
    }

    public void release() {
        changeState(PlayerState.RELEASED);
        this.playerEventCallback = null;
    }

    public void notifyIfPrepared() {
        if (state == PlayerState.PREPARING) {
            changeState(PlayerState.PREPARED);
        } else {
            QLog.e("Player", "State was: " + state + ". " +
                    "The track could not have just finished preparing.");
        }
    }

    public void notifyIfTrackEnded() {
        if (state == PlayerState.PLAYING) {
            changeState(PlayerState.TRACK_ENDED);
        } else {
            QLog.e("Player", "State was: " + state + ". The track could not have ended.");
        }
    }

    private boolean canPlay() {
        return state == PlayerState.PREPARED ||
                state == PlayerState.PAUSED;
    }

    private boolean canPause() {
        return state == PlayerState.PLAYING ||
                state == PlayerState.PREPARING;
    }

    /**
     * Should only be overridden to add logging functionality. For the most part though, the Q should handle logging.
     *
     * @param state The new state of the player.
     */
    protected void changeState(PlayerState state) {
        PlayerState previousState = this.state;

        this.state = state;

        postPlayerEvent(state, "Previous state: " + previousState);
    }

    public PlayerState getState() {
        return state;
    }

    private void postPlayerEvent(@NotNull PlayerState event, String info) {
        if (playerEventCallback != null) {
            playerEventCallback.onEvent(event, info);
        } else {
            QLog.wtf(this.getClass().getSimpleName(), "playerEventCallback was null.");
        }

        if (shouldLog()) {
            QLog.i(this.getClass().getSimpleName(), "Event: " + event + "    Info: " + info);
        }
    }

    /**
     * @return true if LogLevel is {@link LogLevel#BASIC}, {@link LogLevel#FULL}, or {@link LogLevel#PLAYER}.
     */
    private static boolean shouldLog() {
        return QLog.getLogLevel() == LogLevel.FULL ||
                QLog.getLogLevel() == LogLevel.BASIC ||
                QLog.getLogLevel() == LogLevel.PLAYER;
    }
}
