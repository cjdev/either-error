// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

package com.cj.eithererror.javaapi;

import com.cj.eithererror.ErrorC;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EitherContext<E> {

    public final ErrorStrategy<E> strategy;

    private final ErrorC<E> ev;

    private final EitherContext<E> ctx;

    public EitherContext(ErrorStrategy<E> strategy) {
        this.strategy = strategy;
        this.ev = Impl.inst(strategy);
        this.ctx = this;
    }

    public final class Either<A> {

        protected final scala.util.Either<E, A> repr;

        private Either(scala.util.Either<E, A> repr) {
            this.repr = repr;
        }

        @Override public boolean equals(Object other) {
            return Impl.<E,A>equals(ctx, repr, other);
        }

        // https://www.artima.com/lejava/articles/equality.html
        @Override public int hashCode() {
            return repr.hashCode();
        }

        public <X> X fold(Function<E, X> withLeft, Function<A, X> withRight) {
            return Impl.<E,A,X>fold(repr, withLeft, withRight);
        }

        public Optional<A> get() {
            return Impl.<E,A>get(repr);
        }

        public Optional<E> getError() {
            return Impl.<E,A>getError(repr);
        }

        public A getOrElse(A a) {
            return Impl.<E,A>getOrElse1(repr, a);
        }

        public A getOrElse(Supplier<A> a) {
            return Impl.<E,A>getOrElse2(repr, a);
        }

        public A getOrThrow() throws Throwable {
            return Impl.<E,A>getOrThrow(ev, repr);
        }

        public void foreach(Consumer<A> f) {
            Impl.<E,A>foreach(repr, f);
        }

        public <B> Either<B> map(Function<A, B> f) {
            return new Either<B>(Impl.<E,A,B>map(repr, f));
        }

        public <B> Either<B> flatMap(Function<A, Either<B>> f) {
            return new Either<B>(Impl.<E,A,B>flatMap(ctx, repr, f));
        }

        public Either<A> filter(Function<A, Boolean> p) {
            return new Either<A>(Impl.<E,A>filter(ev, repr, p));
        }

        public <B> Either<B> and(Either<B> other) {
            return new Either<B>(Impl.<E,A,B>and1(ctx, repr, other));
        }

        public <B> Either<B> and(Supplier<Either<B>> other) {
            return new Either<B>(Impl.<E,A,B>and2(ctx, repr, other));
        }

        public Either<A> or(Either<A> other) {
            return new Either<A>(Impl.<E,A>or1(ctx, repr, other));
        }

        public Either<A> or(Supplier<Either<A>> other) {
            return new Either<A>(Impl.<E,A>or2(ctx, repr, other));
        }

        public Either<A> recover(Function<E, Either<A>> f) {
            return new Either<A>(Impl.<E,A>recover(ctx, repr, f));
        }
    }

    public <A> Either<A> unsafe(A a) {
        return new Either<A>(Impl.<E,A>unsafe(a));
    }

    public <A> Either<A> safely(Supplier<A> a, Object alt) {
        return new Either<A>(Impl.<E,A>safely1(ev, a, alt));
    }

    public <A> Either<A> safely(Supplier<A> a, Supplier<Object> alt) {
        return new Either<A>(Impl.<E,A>safely2(ev, a, alt));
    }

    public <A> Either<A> safely(Supplier<A> a) {
        return new Either<A>(Impl.<E,A>safely3(ev, a));
    }

    public Either<Unity> ensure(Boolean p, Object alt) {
        return new Either<Unity>(Impl.<E>ensure1(ev, p, alt));
    }

    public Either<Unity> ensure(Boolean p, Supplier<Object> alt) {
        return new Either<Unity>(Impl.<E>ensure2(ev, p, alt));
    }

    public Either<Unity> ensure(Boolean p) {
        return new Either<Unity>(Impl.<E>ensure3(ev, p));
    }

    public <A> Either<A> failure(Object alt) {
        return new Either<A>(Impl.<E,A>failure1(ev, alt));
    }

    public <A> Either<A> failure() {
        return new Either<A>(Impl.<E,A>failure2(ev));
    }
}
