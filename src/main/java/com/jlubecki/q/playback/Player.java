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

import com.jlubecki.q.logging.QLog;
import com.jlubecki.q.logging.LogLevel;
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

    //region Abstract methods

    /**
     * Prepares a track for playback. Call {@link #notifyIfPrepared()} when this method reaches its endpoint (whether
     * synchronous or asynchronous).
     *
     * @param uri the URI of the track to prepare.
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
     * @param time the time to seek to in milliseconds.
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

    //endregion

    //region Playback methods

    /**
     * Used to start playing a track.
     */
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

    /**
     * Used to pause a track.
     */
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

    /**
     * Used to stop a player.
     */
    public void stop() {
        changeState(PlayerState.STOPPED);
    }

    /**
     * Used to release a player's resources. This is automatically called by
     * {@link PlayerManager#unregisterPlayer(String)} or {@link PlayerManager#removeAllPlayers()}.
     */
    public void release() {
        changeState(PlayerState.RELEASED);
        this.playerEventCallback = null;
    }

    //endregion

    //region State Changes

    /**
     * Changes the state of the player.
     *
     * @param state the new state of the player.
     */
    protected void changeState(PlayerState state) {
        PlayerState previousState = this.state;

        this.state = state;

        postPlayerEvent(state, "Previous state: " + previousState);
    }

    /**
     * @return the current state of the player.
     */
    public PlayerState getState() {
        return state;
    }

    /**
     * Checks that the player is prepared, then fires an event with the {@link PlayerEventCallback} to say the player is
     * prepared for playback.
     */
    public void notifyIfPrepared() {
        if (state == PlayerState.PREPARING) {
            changeState(PlayerState.PREPARED);
        } else {
            QLog.e(this.getClass().getSimpleName(), "State was: " + state + ". " +
                    "The track could not have just finished preparing.");
        }
    }

    /**
     * Checks that the player finished playing a track, then fires an event with the {@link PlayerEventCallback} to say
     * the player is done with the track it was playing.
     */
    public void notifyIfTrackEnded() {
        if (state == PlayerState.PLAYING) {
            changeState(PlayerState.TRACK_ENDED);
        } else {
            QLog.e(this.getClass().getSimpleName(), "State was: " + state + ". The track could not have ended.");
        }
    }

    //endregion

    //region Private Methods

    /**
     * Helper method to check for a valid state prior to playing.
     *
     * @return true if the current state should let the Player begin playback.
     */
    private boolean canPlay() {
        return state == PlayerState.PREPARED ||
                state == PlayerState.PAUSED;
    }

    /**
     * Helper method to check for a valid state prior ro pausing.
     *
     * @return true if the current state should let the Player pause.
     */
    private boolean canPause() {
        return state == PlayerState.PLAYING ||
                state == PlayerState.PREPARING;
    }

    /**
     * Helper method that posts playback state changes.
     *
     * @param event the new {@link PlayerState} of the Player.
     * @param info a string with additional info about the state change. Used for logging.
     */
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

    //endregion
}
