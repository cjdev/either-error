// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.
package com.cj.eithererror.javaapi;

public interface ErrorStrategy<E> {

    E fromMessage(String msg);

    E getDefault();

    E fromThrowable(Throwable err);

    Throwable toThrowable(E e);
}
