package one.xis.jsc;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = XISConfiguration.class)
@ServletComponentScan(basePackageClasses = XISConfiguration.class)
public class XISConfiguration {
}
