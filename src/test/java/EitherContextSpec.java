// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

import com.cj.eithererror.javaapi.EitherContext;
import com.cj.eithererror.javaapi.ErrorStrategies;
import com.cj.eithererror.javaapi.ErrorStrategy;
import com.cj.eithererror.javaapi.Unity;

import java.util.concurrent.CompletableFuture;

public class EitherContextSpec {

    private static void assert_(String msg, boolean p) {
        if (!p) throw new AssertionError(msg);
    }

    public static void eitherContextShouldBeVisible() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        assert_("EitherContext should be visible.",
                ec != null);
    }

    public static void eitherContextShouldMakeItsStrategyVisible() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        assert_("EitherContext should make its ErrorStrategy visible.",
                ec.strategy.equals(es));
    }

    public static void eitherShouldHideItsConstructor() {
        assert_("Either should hide its constructor.",
                EitherContext.Either.class.getConstructors().length == 0);
    }

    public static void unsafeShouldConstructAnEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        assert_("unsafe should construct and Either.",
                foo.get().get().equals(42));
    }

    public static void safelyShouldConstructAnEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.safely(() -> 42, "bar");
        assert_("safely should construct and Either.",
                foo.get().get().equals(42));
    }

    public static void safelyShouldAcceptADeferredAlternative() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.safely(() -> 42, () -> "bar");
        assert_("safely should accept a deferred alternative.",
                foo.get().get().equals(42));
    }

    public static void safelyShouldNotRequireAnAlternative() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.safely(() -> 42);
        assert_("safely should not require an alternative.",
                foo.get().get().equals(42));
    }

    public static void ensureShouldConstructAnEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Unity> foo = ec.ensure(false, "bar");
        assert_("ensure should construct and Either.",
                foo.getError().get().equals("bar"));
    }

    public static void ensureShouldAcceptADeferredAlternative() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Unity> foo = ec.ensure(true, () -> "bar");
        assert_("ensure should accept a deferred alternative.",
                foo.get().get().equals(Unity.instance));
    }

    public static void ensureShouldNotRequireAnAlternative() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Unity> foo = ec.ensure(false);
        assert_("ensure should not require an alternative.",
                foo.getError().get().equals(""));
    }

    public static void failureShouldConstructAnEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.failure("bar");
        assert_("failure should construct and Either.",
                foo.getError().get().equals("bar"));
    }

    public static void failureShouldNotRequireAnAlternative() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.failure();
        assert_("failure should not require an alternative.",
                foo.getError().get().equals(""));
    }

    public static void equalsShouldHaveValueSemantics() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(12);
        EitherContext<String>.Either<Integer> bar = ec.unsafe(12);
        EitherContext<String>.Either<Integer> baz = ec.unsafe(0);
        EitherContext<String>.Either<Integer> qux = ec.failure();
        EitherContext<String>.Either<Integer> mos = ec.failure("mos");
        EitherContext<String>.Either<Integer> rit = ec.failure("mos");

        assert_("Successes with the same value should be equal",
                foo.equals(bar));
        assert_("Successes with different values should not be equal",
                !foo.equals(baz));
        assert_("Successes should not equal failures",
                !foo.equals(qux));
        assert_("Failures with different values should not be equal",
                !qux.equals(mos));
        assert_("Failures with the same value should be equal",
                mos.equals(rit));
    }

    public static void foldShouldDestructureAnEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        assert_("fold should deconstruct and Either.",
                foo.fold(String::length, (Integer n) -> n).equals(42));
    }

    public static void getShouldReturnTheValueInAnOptional() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        assert_("get should return the value in an Optional.",
                foo.get().get().equals(42));
    }

    public static void getErrorShouldReturnTheErrorInAnOptional() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.failure("bar");
        assert_("getError should return the error in an Optional.",
                foo.getError().get().equals("bar"));
    }

    public static void getOrElseShouldBeUsedToRecoverFromErrors() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.failure("bar");
        assert_("getOrElse should be used to recover from errors.",
                foo.getOrElse(21).equals(21));
    }

    public static void getOrElseShouldAcceptADeferredAlternative() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.failure("bar");
        assert_("getOrElse should accept a deferred alternative",
                foo.getOrElse(() -> 21).equals(21));
    }

    public static void getOrThrowShouldReturnTheNakedValue() throws Throwable {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        assert_("getOrThrow should return the naked value.",
                foo.getOrThrow().equals(42));
    }

    public static void foreachShouldConsumeTheInnerValue() throws Throwable {
        CompletableFuture<Integer> bar = new CompletableFuture<>();
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        foo.foreach(bar::complete);
        assert_("foreach should consume the inner value.",
                bar.isDone() && bar.get().equals(42));
    }

    public static void mapShouldMapTheInnerValue() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        assert_("map should map the inner value.",
                foo.map(Number::toString).get().get().equals("42"));
    }

    public static void flatMapShouldFlatMapTheEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        assert_("flatMap should flatMap the Either.",
                foo.flatMap((Integer n) -> ec.failure()).getError().get().equals(""));
    }

    public static void filterShouldFilterTheEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        assert_("filter should filter the Either.",
                foo.filter((Integer n) -> n % 2 == 1).getError().get().equals(""));
    }

    public static void andShouldConstructAnEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        EitherContext<String>.Either<String> bar = ec.unsafe("33");
        assert_("and should construct and Either.",
                foo.and(bar).get().get().equals("33"));
    }

    public static void andShouldAcceptADeferredEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        EitherContext<String>.Either<String> bar = ec.unsafe("33");
        assert_("and should accept a deferred Either.",
                foo.and(() -> bar).get().get().equals("33"));
    }

    public static void orShouldConstructAnEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        EitherContext<String>.Either<Integer> bar = ec.unsafe(33);
        assert_("or should construct an Either.",
                foo.or(bar).get().get().equals(42));
    }

    public static void orShouldAcceptADeferredEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.unsafe(42);
        EitherContext<String>.Either<Integer> bar = ec.unsafe(33);
        assert_("or should accept a deferred Either.",
                foo.or(() -> bar).get().get().equals(42));
    }

    public static void recoverShouldConstructAnEither() {
        ErrorStrategy<String> es = ErrorStrategies.string;
        EitherContext<String> ec = new EitherContext<>(es);
        EitherContext<String>.Either<Integer> foo = ec.failure("bar");
        assert_("recover should construct an Either.",
                foo.recover((String s) -> ec.unsafe(s.length())).get().get().equals(3));
    }
}
