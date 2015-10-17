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
 * Class which describes a track that can be handled by the Q.
 */
@SuppressWarnings("FieldNotUsedInToString")
public class QTrack {

    /**
     * Track title.
     */
    public String title;

    /**
     * Track artist(s).
     */
    public String artist;

    /**
     * URL, file path, etc. that describes the album artwork.
     */
    public String image;

    /**
     * The URI used to play the track. This will be used to determine the correct player to use.
     */
    public String uri;

    /**
     * The media type of the provided track.
     */
    public MediaType mediaType = MediaType.DEFAULT;

    @Override
    public String toString() {
        return String.format("Title: %s    Artist: %s", title, artist);
    }
}
