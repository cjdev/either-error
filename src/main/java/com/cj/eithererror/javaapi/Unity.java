// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror.javaapi;

import java.io.Serializable;

public final class Unity implements Serializable {

    public static final Unity instance;

    static {
        instance = new Unity();
    }

    private Unity() {}

    public boolean equals(Object other) {
        return other instanceof Unity;
    }

    public String toString() {
        return "()";
    }
}
