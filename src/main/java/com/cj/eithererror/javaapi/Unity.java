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

    @Override public boolean equals(Object other) {
        return other instanceof Unity;
    }

    @Override public int hashCode() {
        return 0;
    }

    public String toString() {
        return "()";
    }
}
