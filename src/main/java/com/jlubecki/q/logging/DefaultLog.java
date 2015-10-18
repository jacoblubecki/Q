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

package com.jlubecki.q.logging;

/**
 * Simple implementation of {@link Logger} that uses {@link System#out} to print log messages.
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "ClassWithoutConstructor", "PublicConstructor", "DesignForExtension"})
public class DefaultLog implements Logger {

    private static final int NO_PRIORITY = -1;

    @Override
    public void log(int priority, String tag, String message) {
        switch (priority) {
            case NO_PRIORITY:
                System.out.printf("%d/%s: %s%n", priority, tag, message);
                break;

            default:
                System.out.printf("%s: %s%n", tag, message);
        }
    }

    @Override
    public void log(String tag, String message) {
        log(NO_PRIORITY, tag, message);
    }

    @Override
    public void log(String message) {
        log(NO_PRIORITY, "", message);
    }
}
