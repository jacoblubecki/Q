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

/**
 * Used to track the state of a player.
 */
public enum PlayerState {
    /**
     * A player was created.
     */
    CREATED("created"),

    /**
     * {@link Player#prepare(String)} ()} was called.
     */
    PREPARING("preparing"),

    /**
     * {@link Player#prepare(String)} ()} finished.
     */
    PREPARED("prepared"),

    /**
     * {@link Player#play()} was called.
     */
    PLAYING("playing"),

    /**
     * {@link Player#pause()} was called.
     */
    PAUSED("paused"),

    /**
     * {@link Player#stop()} was called.
     */
    STOPPED("stopped"),

    /**
     * {@link Player#release()} was called.
     */
    RELEASED("released"),

    /**
     * The state of a player once a track ends.
     */
    TRACK_ENDED("track ended");

    private final String state;

    PlayerState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return state;
    }
}
