// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror.javaapi;

import com.cj.eithererror.ErrorC;
import scala.Option;
import scala.Tuple2;

import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;
import java.util.function.Function;

public final class ErrorStrategies {

    public static final ErrorStrategy<String> string;

    public static final ErrorStrategy<Exception> exception;

    public static final ErrorStrategy<Throwable> throwable;

    public static final ErrorStrategy<SimpleEntry<String, Optional<Throwable>>> messageAndCause;

    public static final ErrorStrategy<String> classNameAndMessage;

    static {
        string = fromErrorC(ErrorC.Instances$.MODULE$.errorString());
        exception = fromErrorC(ErrorC.Instances$.MODULE$.errorException());
        throwable = fromErrorC(ErrorC.Instances$.MODULE$.errorThrowable());
        classNameAndMessage = fromErrorC(ErrorC.Instances$.MODULE$.classNameAndMessage());
        messageAndCause = fromErrorC(ErrorC.Instances$.MODULE$.messageAndCause(), ErrorStrategies::toSimpleEntry, ErrorStrategies::toTuple2);
    }

    private static <S, J> ErrorStrategy<J> fromErrorC(ErrorC<S> ec, Function<S, J> fromScala, Function<J, S> toScala) {
        return new ErrorStrategy<J>() {
            @Override public J getDefault() {
                return fromScala.apply(ec.getDefault());
            }
            @Override public J fromMessage(String msg) {
                return fromScala.apply(ec.fromMessage(msg));
            }
            @Override public J fromThrowable(Throwable err) {
                return fromScala.apply(ec.fromThrowable(err));
            }
            @Override public Throwable toThrowable(J e) {
                return ec.toThrowable(toScala.apply(e));
            }
        };
    }

    private static <E> ErrorStrategy<E> fromErrorC(ErrorC<E> ec) {
        return fromErrorC(ec, Function.identity(), Function.identity());
    }

    private static <A> Option<A> toOption(Optional<A> optional) {
        return optional.map(Option::apply).orElse(Option.<A>empty());
    }

    private static <A> Optional<A> toOptional(Option<A> option) {
        if (option.isDefined())
            return Optional.of(option.get());
        else
            return Optional.empty();
    }

    private static Tuple2<String, Option<Throwable>> toTuple2(SimpleEntry<String, Optional<Throwable>> simpleEntry) {
        return new Tuple2<>(simpleEntry.getKey(), toOption(simpleEntry.getValue()));
    }

    private static SimpleEntry<String, Optional<Throwable>> toSimpleEntry(Tuple2<String, Option<Throwable>> tuple2) {
        return new SimpleEntry<>(tuple2._1, toOptional(tuple2._2));
    }
}
