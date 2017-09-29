package com.cj.eithererror.javaapi;

import java.io.Serializable;

final class Unity implements Serializable {

    public static Unity instance;

    private Unity() {}

    static {
        instance = new Unity();
    }

    public boolean equals(Object other) {
        return other instanceof Unity;
    }

    public String toString() {
        return "()";
    }
}
