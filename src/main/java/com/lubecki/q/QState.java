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

package com.lubecki.q;

import java.util.List;

/**
 * Describes the state of the overall Q. Player state is managed separately.
 */
public enum QState {
    /**
     * {@link Q#getInstance()} was called.
     */
    CREATED("created"),

    /**
     * {@link Q#setTrackList(List)} was called and the list was not empty.
     */
    NOT_EMPTY("not empty"),

    /**
     * {@link Q#start()} was called.
     */
    STARTING("starting"),

    /**
     * {@link Q#start()} was called and playback started, or {@link Q#play()} was called.
     */
    PLAYING("playing"),

    /**
     * {@link Q#pause()} was called.
     */
    PAUSED("paused"),

    /**
     * {@link Q#stop()} was called.
     */
    STOPPED("stopped"),

    /**
     * The last track in the list was played and the Q isn't looping.
     */
    PLAYBACK_ENDED("playback ended"),

    /**
     * {@link Q#release()} was called.
     */
    RELEASED("released");

    private final String state;

    QState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return state;
    }
}
