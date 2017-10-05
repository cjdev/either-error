// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

import com.cj.eithererror.javaapi.ErrorStrategy;

public class ErrorStrategySpec {

    private static void assert_(String msg, boolean p) {
        if (!p) throw new AssertionError(msg);
    }

    public static void errorStrategyShouldBeVisible() {
        ErrorStrategy<Integer> strategy = new ErrorStrategy<Integer>() {
            @Override
            public Integer fromMessage(String msg) {
                return msg.length();
            }
        };

        assert_("ErrorStrategy should be visible.", strategy != null);
    }

    public static void errorStrategyShouldUseTheDefaultMethods() {
        ErrorStrategy<Integer> strategy = new ErrorStrategy<Integer>() {
            @Override
            public Integer fromMessage(String msg) {
                return msg.length();
            }
        };

        String msg = "foobar";
        Throwable err = new Exception(msg);

        assert_("ErrorStrategy should use default getDefault.",
                strategy.getDefault().equals("".length()));
        assert_("ErrorStrategy should use default fromMessage.",
                strategy.fromMessage(msg).equals(msg.length()));
        assert_("ErrorStrategy should use default fromThrowable.",
                strategy.fromThrowable(err).equals(err.getMessage().length()));
        assert_("ErrorStrategy should use default toThrowable.",
                strategy.toThrowable(42).toString().contains("42"));
    }

    public static void theDefaultMethodsShouldBeOverridable() {
        ErrorStrategy<Integer> strategy = new ErrorStrategy<Integer>() {
            @Override
            public Integer fromMessage(String msg) {
                return null;
            }
            @Override
            public Integer fromThrowable(Throwable err) {
                return null;
            }
            @Override
            public Integer getDefault() {
                return null;
            }
            @Override
            public Throwable toThrowable(Integer e) {
                return null;
            }
        };

        assert_("ErrorStrategy should use provided getDefault.",
                strategy.getDefault() == null);
        assert_("ErrorStrategy should use provided fromThrowable.",
                strategy.fromThrowable(new Exception("foobar")) == null);
        assert_("ErrorStrategy should used provided fromMessage.",
                strategy.fromMessage("foobar") == null);
        assert_("ErrorStrategy should use provided toThrowable.",
                strategy.toThrowable(42) == null);
    }
}
