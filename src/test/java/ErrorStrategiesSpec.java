// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

import com.cj.eithererror.javaapi.ErrorStrategies;
import com.cj.eithererror.javaapi.ErrorStrategy;

public class ErrorStrategiesSpec {

    private static void assert_(String msg, boolean p) {
        if (!p) throw new AssertionError(msg);
    }

    public static void stringShouldBeVisible() {
        ErrorStrategy<String> strategy = ErrorStrategies.string;
        assert_("ErrorStrategies.string should be visible.",
                strategy != null);
    }

    public static void stringShouldBeFinal() {
        throw new RuntimeException("Test not implemented");
    }

    public static void exceptionShouldBeVisible() {
        ErrorStrategy<Exception> strategy = ErrorStrategies.exception;
        assert_("ErrorStrategies.exception should be visible.",
                strategy != null);
    }

    public static void exceptionShouldBeFinal() {
        throw new RuntimeException("Test not implemented");
    }

    public static void throwableShouldBeVisible() {
        ErrorStrategy<Throwable> strategy = ErrorStrategies.throwable;
        assert_("ErrorStrategies.throwable should be visible.",
                strategy != null);
    }

    public static void throwableShouldBeFinal() {
        throw new RuntimeException("Test not implemented");
    }

    public static void messageAndCauseShouldBeVisible() {
        throw new RuntimeException("Test not implemented");
    }

    public static void messageAndCauseShouldBeFinal() {
        throw new RuntimeException("Test not implemented");
    }

    public static void classNameAndMessageShouldBeVisible() {
        throw new RuntimeException("Test not implemented");
    }

    public static void classNameAndMessageShouldBeFinal() {
        throw new RuntimeException("Test not implemented");
    }
}
