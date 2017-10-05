// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror.javaapi;

public interface ErrorStrategy<E> {

    E fromMessage(String msg);

    default E getDefault() {
        return Impl.esGetDefault(this);
    }

    default E fromThrowable(Throwable err) {
        return Impl.esFromThrowable(this, err);
    }

    default Throwable toThrowable(E e) {
        return Impl.esToThrowable(e);
    }
}
