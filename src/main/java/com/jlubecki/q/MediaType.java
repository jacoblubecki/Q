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

/**
 * Describes the various media types the Q should be able to handle.
 */
public enum MediaType {

    /**
     * Used as the default media type for all tracks. Players that only need one media format can then ignore the
     * {@link QTrack#mediaType} field.
     */
    DEFAULT("default"),

    /**
     * Used for music or other plain audio formats.
     */
    AUDIO("audio"),

    /**
     * Used for any type of playable video.
     */
    VIDEO("video"),

    /**
     * Used for live streamed audio content. (i.e. online radio, live podcasts, etc.)
     */
    STREAM_AUDIO("stream audio"),

    /**
     * Used for live streamed video content.
     */
    STREAM_VIDEO("stream video"),

    /**
     * Used for images. Doubt this is useful, but now the option is there.
     */
    IMAGE("image"),

    /**
     * Describes anything that can be described with a {@link com.jlubecki.q.playback.Player} but doesn't necessarily
     * match any of the explicitly provided media types.
     */
    OTHER("other");

    private final String type;

    MediaType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
