// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror.javaapi;

import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;

public final class ErrorStrategies {

    public static final ErrorStrategy<String> string;

    public static final ErrorStrategy<Exception> exception;

    public static final ErrorStrategy<Throwable> throwable;

    public static final ErrorStrategy<SimpleEntry<String, Optional<Throwable>>> messageAndCause;

    public static final ErrorStrategy<String> classNameAndMessage;

    static {
        string = Impl.string();
        exception = Impl.exception();
        throwable = Impl.throwable();
        classNameAndMessage = Impl.classNameAndMessage();

        messageAndCause = new ErrorStrategy<SimpleEntry<String, Optional<Throwable>>>() {

            @Override
            public SimpleEntry<String, Optional<Throwable>> fromMessage(String msg) {
                return Impl.mcFromMessage(msg);
            }

            @Override
            public SimpleEntry<String, Optional<Throwable>> getDefault() {
                return Impl.mcGetDefault();
            }

            @Override
            public SimpleEntry<String, Optional<Throwable>> fromThrowable(Throwable err) {
                return Impl.mcFromThrowable(err);
            }

            @Override
            public Throwable toThrowable(SimpleEntry<String, Optional<Throwable>> e) {
                return Impl.mcToThrowable(e);
            }
        };
    }
}
