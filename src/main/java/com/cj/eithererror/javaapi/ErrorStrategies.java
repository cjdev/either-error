// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.
package com.cj.eithererror.javaapi;

public class ErrorStrategies {

    public static ErrorStrategy<String> string;

    public static ErrorStrategy<Exception> exception;

    public static ErrorStrategy<Throwable> throwable;

    static {
        string = Impl.string();
        exception = Impl.exception();
        throwable = Impl.throwable();
    }
}
