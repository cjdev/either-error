// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.
package com.cj.eithererror.javaapi;

public class ErrorStrategies {

    public static ErrorStrategy<String> string;

    public static ErrorStrategy<Exception> exception;

    public static ErrorStrategy<Throwable> throwable;

    static {
        string = Cnv.cnv(Cnv.string());
        exception = Cnv.cnv(Cnv.exception());
        throwable = Cnv.cnv(Cnv.throwable());
    }
}
