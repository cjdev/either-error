// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

import com.cj.eithererror.javaapi.Unity;

public class UnitySpec {

    private static void assert_(String msg, boolean p) {
        if (!p) throw new AssertionError(msg);
    }

    public static void instanceIsAccessible() {
        Unity unity = Unity.instance;
        assert_("Unity should be accessible.",
                unity != null);
    }

    public static void instanceIsFinal() {
        throw new RuntimeException("Test not implemented");
    }

    public static void unityCannotBeConstructed() {
        assert_("Unity should hide its constructor.",
                Unity.class.getConstructors().length == 0);
    }

    public static void unityIsEqualToItself() {
        Unity unity1 = Unity.instance;
        Unity unity2 = Unity.instance;
        assert_("Unity should equal itself.",
                unity1.equals(unity2));
    }

    public static void unityEqualityIsNotUniversallyTrue() {
        Unity unity = Unity.instance;
        assert_("Unity should not equal randos.",
                !unity.equals(new Object()));
        assert_("Randos should not equal unity.",
                !(new Object()).equals(unity));
    }

    public static void unityHasIntendedStringRepresentation() {
        assert_("Unity should look like ()",
                Unity.instance.toString().equals("()"));
    }
}
