package com.cj.eithererror.javaapi;

public interface ErrorStrategy<E> {

    E fromMessage(String msg);

    E getDefault();

    E fromThrowable(Throwable err);

    Throwable toThrowable(E e);
}
