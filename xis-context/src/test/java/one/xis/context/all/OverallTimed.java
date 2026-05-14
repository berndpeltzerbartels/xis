package one.xis.context.all;

import one.xis.context.UseAdvice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@UseAdvice(OverallTimingAdvice.class)
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface OverallTimed {
}
