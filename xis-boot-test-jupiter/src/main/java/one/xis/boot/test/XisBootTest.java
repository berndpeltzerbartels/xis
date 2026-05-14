package one.xis.boot.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(XisBootTestExtension.class)
public @interface XisBootTest {

    String[] packages() default {};

    Class<?>[] packageClasses() default {};
}
