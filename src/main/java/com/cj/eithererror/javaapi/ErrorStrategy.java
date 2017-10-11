// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror.javaapi;

import com.cj.eithererror.ErrorC;

public interface ErrorStrategy<E> {

    E fromMessage(String msg);

    default E getDefault() {
        return ErrorC.Defaults$.MODULE$.<E>getDefault(ErrorC.Defaults$.MODULE$.<String, E>asScala(this::fromMessage));
    }

    default E fromThrowable(Throwable err) {
        return ErrorC.Defaults$.MODULE$.<E>fromThrowable(ErrorC.Defaults$.MODULE$.<String, E>asScala(this::fromMessage), err);
    }

    default Throwable toThrowable(E e) {
        return ErrorC.Defaults$.MODULE$.<E>toThrowable(e);
    }
}
