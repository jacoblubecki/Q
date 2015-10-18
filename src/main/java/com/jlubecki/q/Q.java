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

package com.jlubecki.q;


import com.jlubecki.q.playback.PlayerState;
import com.jlubecki.q.logging.LogLevel;
import com.jlubecki.q.logging.QLog;
import com.jlubecki.q.playback.Player;
import com.jlubecki.q.playback.PlayerManager;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains a list of generic tracks which are registered to an adapter that recognizes the URI and determines the
 * relevant player.
 */
@SuppressWarnings({"unused", "LawOfDemeter"})
public final class Q {

    /**
     * Should only access with {@link #getInstance()}.
     */
    private Q() {
        // Singleton instance.

        manager = PlayerManager.getInstance();
        setState(QState.CREATED);
    }

    private static Q instance;

    //region Error Messages

    private static final String UNKNOWN_LOOP_STATE = "Unknown loop state.";
    private static final String INVALID_LOG_LEVEL = "Tried to log with invalid log level.";

    //endregion

    //region Logging

    /**
     * Returns the singleton instance of the Q object. The lazy initialization of the singleton object is synchronized
     * to maintain thread safety.
     *
     * @return the singleton instance of the Q.
     */
    public static synchronized Q getInstance() {
        if (instance == null) {
            instance = new Q();
        }
        return instance;
    }

    /**
     * Logs the current index and track name. Log priority is INFO.
     *
     * @param prefix Simple modifier to help identify what method changed the index / where the index is being logged.
     */
    private void logIndex(@NonNls String prefix) {
        if (QLog.getLogLevel() != LogLevel.NONE && QLog.getLogLevel() != LogLevel.PLAYER) {
            String message = prefix + "  ::  Index: %d    Track: %s";
            message = String.format(message, index, current.title);

            QLog.i(this.getClass().getSimpleName(), message);
        }
    }

    /**
     * Logs playback calls made by the Q. Log priority is VERBOSE.
     *
     * @param eventName Name of the playback event being logged.
     */
    private void logPlayback(@NonNls String eventName) {
        String message;
        switch (QLog.getLogLevel()) {
            case NONE:
                // We can go through to the next statement because the QLog will ignore the method call when the log
                // level is NONE.

            case PLAYER:
                // Don't log anything in the Q for PLAYER log level.
                break;

            case BASIC:
                message = eventName + "  ::  Track: %s";
                message = String.format(message, current.title);

                QLog.v(this.getClass().getSimpleName(), message);
                break;

            case Q:
            case FULL:
                // Don't print the whole image Data array
                String data = current.imageData != null ? "Yes" : "No";
                message = eventName + "  ::  " +
                        "Track: %s    Artist: %s    URI: %s    Image: %s    Image Data: %s    State: %s    Index: %d";
                message = String.format(message, current.title, current.artist, current.uri, current.imagePath, data,
                        state, index);

                QLog.v(this.getClass().getSimpleName(), message);
                break;
        }
    }

    /**
     * Logs state changes. Log priority is VERBOSE.
     */
    private void logState() {
        if (QLog.getLogLevel() == LogLevel.FULL || QLog.getLogLevel() == LogLevel.Q) {
            String message = String.format("Q State changed to: %s", state.toString());

            QLog.v(this.getClass().getSimpleName(), message);
        }
    }

    private void logMediaType() {
        if (QLog.getLogLevel() == LogLevel.FULL || QLog.getLogLevel() == LogLevel.Q) {
            String message = String.format("Current media type changed to: %s", current.mediaType.toString());

            QLog.v(this.getClass().getSimpleName(), message);
        }
    }

    //endregion

    //region Playback

    private List<QTrack> tracks;
    private List<QTrack> original; // never modified, used to revert shuffle
    private int index;
    private QEventListener listener;
    private final PlayerManager manager;
    private QTrack current;
    private Loop loop = Loop.NONE;
    private QState state;
    private MediaType mediaType = MediaType.DEFAULT;
    private boolean resetOnPrevious = true; // reset when previous is executed by default is past the minDelay time
    private int minDelay = 2000; // by default, if 2 seconds pass on a track, hitting previous will reset the track

    //region Public Methods

    /**
     * Adds a player to the {@link PlayerManager}.
     *
     * @param uriPattern the pattern linked to the provided {@link Player}.
     * @param player     the {@link Player} that should handle a track uri matching the provided pattern.
     */
    public void addPlayer(@RegExp String uriPattern, Player player) {
        manager.registerPlayer(uriPattern, player);
    }

    /**
     * Removes a player from the {@link PlayerManager}.
     *
     * @param uriPattern the pattern linked to the {@link Player} that should be removed.
     */
    public void removePlayer(@RegExp String uriPattern) {
        manager.unregisterPlayer(uriPattern);
    }

    /**
     * Recommended to get callbacks for the state of the Q. Some use cases may not need to listen for {@link QState}
     * changes.
     *
     * @param listener the object that should listen for changes to the state of the Q.
     */
    public void setListener(QEventListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the list of tracks for the Q to play.
     *
     * @param tracks the list of tracks that should be played.
     */
    public void setTrackList(List<? extends QTrack> tracks) {
        this.tracks = new ArrayList<QTrack>(tracks);
        this.original = new ArrayList<QTrack>(tracks);

        index = 0;
        setCurrent();

        setState(QState.NOT_EMPTY);
    }

    /**
     * Moves to the previous track.
     */
    public void previous() {
        if (manager.getCurrentPlayer().getCurrentTime() < minDelay || !resetOnPrevious) {
            stop();

            switch (loop) {
                case SINGLE:
                    start();
                    break;

                // Since previous movement should only be made explicitly, the playlist should always treat a movement
                // in reverse as an attempt to play a song. If at the beginning of a list, move to the end and play.
                case NONE:
                case LIST:
                    decrement();
                    start();
                    break;
            }
        } else {
            seekTo(0);
        }
    }

    /**
     * Moves to the next track.
     */
    public void next() {
        stop();

        switch (loop) {
            case SINGLE:
                start();
                break;

            case NONE:
                increment();

                // If the Q is not looping, then it should stop playback after the last track. However, it should still
                // reset to the start of the list.
                if (index == 0) {
                    setState(QState.PLAYBACK_ENDED);
                } else {
                    start();
                }
                break;

            case LIST:
                increment();
                start();
                break;
        }
    }

    /**
     * Plays a track for a given index. This should be used when a user selects a track to play.
     *
     * @param index Index of the track to play from the unshuffled list.
     * @throws IllegalStateException whenever the index is out of bounds.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public void setIndex(int index) {
        // Corrects for shuffled list
        this.index = tracks.indexOf(original.get(index));

        stop();
        setCurrent();
        start();

        logIndex("SetIndex");
    }

    /**
     * Sets the shuffle state for the track list.
     *
     * @param shuffling true if the tracks should be shuffled.
     *                  <p/>
     *                  false if the tracks should be returned to their original order.
     * @param reset     True for a 'shuffle button' type behavior. The current track will become the first track of the new
     *                  list and will begin playing.
     *                  <p/>
     *                  False for a 'shuffle toggle' type behavior. The current track (including playback state) will not
     *                  change, but the rest of the list will be shuffled, and the current track will become the first track
     *                  in the list.
     *                  <p/>
     *                  No effect if 'shuffling' is false.
     */
    public void setShuffling(boolean shuffling, boolean reset) {

        if (QLog.getLogLevel() == LogLevel.FULL) {
            QLog.i(this.getClass().getSimpleName(), tracks.toString());
            QLog.i(this.getClass().getSimpleName(), original.toString());
        }

        if (shuffling) {
            Collections.shuffle(tracks);
            index = 0;

            if (reset) {
                setCurrent(); // Current track is the first track.
                start();
            } else {
                tracks.remove(tracks.indexOf(current));

                if (QLog.getLogLevel() == LogLevel.FULL) {
                    QLog.i(this.getClass().getSimpleName(), tracks.toString());
                    QLog.i(this.getClass().getSimpleName(), original.toString());
                }

                tracks.add(0, current);

                if (QLog.getLogLevel() == LogLevel.FULL) {
                    QLog.i(this.getClass().getSimpleName(), tracks.toString());
                    QLog.i(this.getClass().getSimpleName(), original.toString());
                }

                setCurrent(); // The current track is unchanged, but it is now the first in the list.
            }
        } else {

            tracks = new ArrayList<QTrack>(original);

            if (QLog.getLogLevel() == LogLevel.FULL) {
                QLog.i(this.getClass().getSimpleName(), tracks.toString());
                QLog.i(this.getClass().getSimpleName(), original.toString());
            }

            index = tracks.indexOf(current);
            setCurrent(); // Never reset playback if shuffling is toggled off.
        }
    }

    /**
     * Sets the style of looping the Q should use.
     *
     * @param loop the type of looping that the Q should use.
     */
    public void setLooping(Loop loop) {
        this.loop = loop;
    }

    /**
     * Seeks to a specific point in a song.
     *
     * @param time The time in the song to seek to.
     */
    public void seekTo(int time) {
        manager.seekTo(time);

        logPlayback("SEEKING");
    }

    /**
     * Determines whether or not to simply reset the current track when previous is hit, and if so, how much time must
     * elapse for this to happen (there has to be a window of time where the previous command actually moves to the next
     * track.
     *
     * @param resetTrack true if the track should be reset to time = 0 seconds when previous is hit.
     * @param minDelay   does nothing if resetTrack is false. If resetTrack is true, the previous button should move to
     *                   the previous track as long as the track time is less than this value.
     */
    public void setPreviousBehavior(boolean resetTrack, int minDelay) {
        this.resetOnPrevious = resetTrack;
        this.minDelay = minDelay;
    }

    /**
     * Method to get the current track.
     *
     * @return the current track.
     */
    public QTrack getCurrent() {
        return current;
    }

    /**
     * Gets the Q ready to begin playback without starting playback.
     */
    public void prepare() {
        setState(QState.STARTING);

        manager.justPrepare(current.uri);

        if(listener == null && QLog.getLogLevel() != LogLevel.PLAYER) {
            QLog.w(this.getClass().getSimpleName(), "The QEventListener has not been set.");
        }

        logPlayback("PREPARE");
    }

    /**
     * Starts playback for the current track. Should only be called when playback is started. Should also force stop
     * anything currently playing.
     */
    public void start() {
        setState(QState.STARTING);

        manager.prepare(current.uri);

        logPlayback("START");
    }

    /**
     * Resumes paused playback or begins playing a track. Should only be called if playback is paused.
     */
    public void play() {
        PlayerState playerState = manager.getCurrentPlayer().getState();

        // Play pressed when a current track exists but is not prepared.
        if (playerState == PlayerState.CREATED || playerState == PlayerState.STOPPED) {
            start();
        } else {
            manager.play();

            setState(QState.PLAYING);

            logPlayback("PLAY");
        }
    }

    /**
     * Pauses playback. Should only be called if playback is ongoing.
     */
    public void pause() {
        manager.pause();

        setState(QState.PAUSED);

        logPlayback("PAUSE");
    }


    /**
     * Should stop the current player in an intelligent way. If replaying the same track, just set to beginning. If
     * moving to the next track, release resources if possible, etc. TODO
     */
    public void stop() {
        manager.stop();

        setState(QState.STOPPED);

        logPlayback("STOP");
    }

    /**
     * Calls release method for all player objects and makes the Q instance null.
     */
    public void release() {
        manager.removeAllPlayers();
        manager.release();
        instance = null;

        setState(QState.RELEASED);

        QLog.log("Q RELEASED.");
    }

    //endregion

    //region Private Methods

    /**
     * Increases the index. Loops to the beginning if already at the end of the list.
     */
    private void increment() {
        index++;
        index = index > tracks.size() - 1 ? 0 : index;
        setCurrent();

        logIndex("INCREMENT");
    }

    /**
     * Decreases the index. Loops to the end of the list if already at index 0.
     */
    private void decrement() {
        index--;
        index = index < 0 ? tracks.size() - 1 : index;
        setCurrent();

        logIndex("DECREMENT");
    }

    /**
     * Simply sets the current track. Kills the Q immediately if a user manages to try to play something when nothing is
     * there to be played.
     *
     * @throws IllegalStateException when the user tries to play an empty list.
     */
    private void setCurrent() {
        if (validIndex()) {
            current = tracks.get(index);
            manager.updatePlayer(current);
            setMediaType(current.mediaType);
        } else {
            String message = tracks != null ?
                    String.format("Couldn't set current track  ::  Index: %d    Tracks: %d", index, tracks.size()) :
                    "Track list was null.";

            IllegalStateException exception = new IllegalStateException(message);

            if (QLog.shouldIgnoreIllegalStates()) {
                QLog.e(exception, this.getClass().getSimpleName(), exception.getMessage());
            } else {
                throw exception;
            }
        }
    }

    @SuppressWarnings({"OverlyComplexBooleanExpression", "BooleanMethodNameMustStartWithQuestion"})
    private boolean validIndex() {
        return index >= 0 &&
                index < tracks.size() &&
                tracks != null &&
                !tracks.isEmpty();
    }


    /**
     * Sets the current state for the Q.
     *
     * @param state New state of the Q.
     */
    private void setState(QState state) {
        this.state = state;

        logState();

        if (listener != null) {
            listener.onEvent(state);
        } else {
            QLog.w(this.getClass().getSimpleName(), "QEventCallback was null.");
        }
    }

    /**
     * Used by {@link #setCurrent()} to fire off a {@link QEventListener#onMediaTypeChanged(MediaType)} event.
     *
     * @param mediaType the new media type as determined by the current track.
     */
    private void setMediaType(MediaType mediaType) {
        // only make adjustments when media type actually changes
        if (this.mediaType != mediaType) {
            this.mediaType = mediaType;

            logMediaType();

            if (listener != null) {
                listener.onMediaTypeChanged(mediaType);
            } else {
                QLog.w(this.getClass().getSimpleName(), "QEventCallback was null.");
            }
        }
    }

    //endregion

    //endregion
}
