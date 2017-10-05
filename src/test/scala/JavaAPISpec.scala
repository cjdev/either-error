// Copyright (c) 2017 CJ Engineering under the terms of the MIT License
// See LICENSE in project root.

import org.scalatest.{FlatSpec, Matchers}

class JavaAPISpec extends FlatSpec with Matchers {

  "Unity" should "implement its spec" in {
    import UnitySpec._
    instanceIsAccessible()
    unityCannotBeConstructed()
    unityIsEqualToItself()
    unityEqualityIsNotUniversallyTrue()
    unityHasIntendedStringRepresentation()
  }

  "ErrorStrategy" should "implement its spec" in {
    import ErrorStrategySpec._
    errorStrategyShouldBeVisible()
    errorStrategyShouldUseTheDefaultMethods()
    theDefaultMethodsShouldBeOverridable()
  }

  "ErrorStrategies" should "implement its spec" in {
    import ErrorStrategiesSpec._
    stringShouldBeVisible()
    exceptionShouldBeVisible()
    throwableShouldBeVisible()
    messageAndCauseShouldBeVisible()
    classNameAndMessageShouldBeVisible()
  }

  "EitherContext" should "implement its spec" in {
    import EitherContextSpec._
    eitherContextShouldBeVisible()
    eitherContextShouldMakeItsStrategyVisible()
    eitherShouldHideItsConstructor()
    unsafeShouldConstructAnEither()
    safelyShouldConstructAnEither()
    safelyShouldAcceptADeferredAlternative()
    safelyShouldNotRequireAnAlternative()
    ensureShouldConstructAnEither()
    ensureShouldAcceptADeferredAlternative()
    ensureShouldNotRequireAnAlternative()
    failureShouldConstructAnEither()
    failureShouldNotRequireAnAlternative()
    equalsShouldHaveValueSemantics()
    foldShouldDestructureAnEither()
    getShouldReturnTheValueInAnOptional()
    getErrorShouldReturnTheErrorInAnOptional()
    getOrElseShouldBeUsedToRecoverFromErrors()
    getOrThrowShouldReturnTheNakedValue()
    foreachShouldConsumeTheInnerValue()
    mapShouldMapTheInnerValue()
    flatMapShouldFlatMapTheEither()
    filterShouldFilterTheEither()
    andShouldConstructAnEither()
    andShouldAcceptADeferredEither()
    orShouldConstructAnEither()
    orShouldAcceptADeferredEither()
    recoverShouldConstructAnEither()
  }
}
