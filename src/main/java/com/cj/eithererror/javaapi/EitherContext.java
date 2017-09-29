package com.cj.eithererror.javaapi;

import com.cj.eithererror.EitherMonad.EitherMonadInstance;
import com.cj.eithererror.ErrorC;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EitherContext<E> {

    public final ErrorStrategy<E> strategy;

    private final ErrorC<E> ev;

    private final EitherContext<E> context;

    public EitherContext(ErrorStrategy<E> strategy) {
        this.strategy = strategy;
        this.ev = Cnv.cnv(strategy);
        this.context = this;
    }

    final class Either<A> {

        protected final EitherMonadInstance<E, A> repr;

        private Either(EitherMonadInstance<E, A> repr) {
            this.repr = repr;
        }

        public <X> X fold(Function<E, X> withLeft, Function<A, X> withRight) {
            return repr.self().fold(Cnv.cnv(withLeft), Cnv.cnv(withRight));
        }

        public Optional<A> get() {
            return Cnv.cnv(repr.get());
        }

        public Optional<E> getError() {
            return Cnv.cnv(repr.getError());
        }

        public A getOrElse(A a) {
            return repr.getOrElse(a);
        }

        public A getOrThrow() throws Throwable {
            return repr.getOrThrow(ev);
        }

        public void foreach(Consumer<A> f) {
            repr.foreach(Cnv.cnv(f));
        }

        public <B> Either<B> map(Function<A, B> f) {
            return new Either<B>(Cnv.cnv(repr.<B>map(Cnv.cnv(f))));
        }

        public <B> Either<B> flatMap(Function<A, Either<B>> f) {
            return new Either<B>(Cnv.<E, B>cnv(repr.<B>flatMap(
                    Cnv.<E, A, B>kleisli(context).apply(f))));
        }

        public Either<A> filter(Function<A, Boolean> p) {
            return new Either<A>(Cnv.cnv(repr.filter(Cnv.<A>pred(p), ev)));
        }

        public <B> Either<B> and(Supplier<Either<B>> other) {
            return null;
        }

        public Either<A> or(Supplier<Either<A>> other) {
            return null;
        }

        public Either<A> recover(Function<E, Either<A>> f) {
            return null;
        }
    }

    public <A> Either<A> safely(Supplier<A> a, Supplier<Object> alt) {
        return null;
    }

    public <A> Either<A> safely(Supplier<A> a) {
        return null;
    }

    public Either<Unity> ensure(boolean p, Supplier<Object> alt) {
        return null;
    }

    public Either<Unity> ensure(boolean p) {
        return null;
    }

    public <A> Either<A> failure(Object alt) {
        return null;
    }

    public <A> Either<A> failure() {
        return null;
    }
}
