// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror.javaapi;

public interface ErrorStrategy<E> {

    E fromMessage(String msg);

    default E getDefault() {
        return fromMessage("");
    }

    default E fromThrowable(Throwable err) {
        return fromMessage(err.toString());
    }

    default Throwable toThrowable(E e) {
        return new Exception(e.toString());
    }
}
