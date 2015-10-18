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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Logging class for Q. Behavior can be easily customized to use any logging mechanism.
 */
@SuppressWarnings({"ConstantNamingConvention", "unused", "StaticMethodNamingConvention"})
public final class QLog {

    private static Logger logger = new DefaultLog();
    private static LogLevel logLevel = LogLevel.NONE;
    private static boolean shouldIgnoreIllegalStates = false;

    public static final int VERBOSE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;
    public static final int WTF = 5;

    private QLog() {
        // No instances.
    }

    public static void setLogger(Logger logger) {
        QLog.logger = logger;
    }

    public static void setLogLevel(LogLevel logLevel) {
        QLog.logLevel = logLevel;
    }

    public static LogLevel getLogLevel() {
        return logLevel;
    }

    public static void ignoreIllegalStates(boolean ignore) {
        shouldIgnoreIllegalStates = ignore;
    }

    public static boolean shouldIgnoreIllegalStates() {
        return shouldIgnoreIllegalStates;
    }

    public static void log(int priority, String tag, String message) {
        if(logLevel.log()) {
            logger.log(priority, tag, message);
        }
    }

    public static void log(String tag, String message) {
        if(logLevel.log()) {
            logger.log(tag, message);
        }
    }

    public static void log(String message) {
        if(logLevel.log()) {
            logger.log(message);
        }
    }

    public static void v(String tag, String message) {
        log(VERBOSE, tag, message);
    }

    public static void d(String tag, String message) {
        log(DEBUG, tag, message);
    }

    public static void i(String tag, String message) {
        log(INFO, tag, message);
    }

    public static void w(String tag, String message) {
        log(WARN, tag, message);
    }

    public static void e(String tag, String message) {
        log(ERROR, tag, message);
    }

    public static void wtf(String tag, String message) {
        log(WTF, tag, message);
    }

    public static void w(Throwable throwable, String tag, String message) {
        log(WARN, tag, String.format("%s%n%s", message, getStackTraceAsString(throwable)));
    }

    public static void e(Throwable throwable, String tag, String message) {
        log(ERROR, tag, String.format("%s%n%s", message, getStackTraceAsString(throwable)));
    }

    public static void wtf(Throwable throwable, String tag, String message) {
        log(WTF, tag, String.format("%s%n%s", message, getStackTraceAsString(throwable)));
    }

    private static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));

        return sw.toString();
    }
}
