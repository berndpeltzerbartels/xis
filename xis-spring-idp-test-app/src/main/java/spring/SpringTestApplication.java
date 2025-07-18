package spring;

import one.xis.EnablePushClients;
import one.xis.MainClass;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MainClass
@SpringBootApplication
@EnablePushClients(basePackages = "spring.test")
class SpringTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTestApplication.class, args);
    }
}
