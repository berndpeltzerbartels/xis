package test.page.security;

import one.xis.context.IntegrationTestContext;
import one.xis.security.LocalUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class ProtectedModelDataTest {

    @Nested
    @DisplayName("Protected model data is accessed without token and login form is displayed")
    class AccessDeniedNoUserTest {
        private IntegrationTestContext testContext;

        @BeforeEach
        void init() {
            testContext = IntegrationTestContext.builder()
                    .withSingleton(ProtectedModelDataPage1.class)
                    .withSingleton(ProtectedModelDataPage2.class)
                    .build();
        }

        @Test
        void test() {
            var result = testContext.openPage("/page1.html");
            var document = result.getDocument();
            var link = document.getElementById("link");

            link.click();

            assertThat(document.getElementByTagName("title").innerText).isEqualTo("Login");
        }
    }

    @Nested
    @DisplayName("Protected model data is accessed with token and user is logged in, but roles do not match")
    class AccessDeniedWrongRoleTest {
        private IntegrationTestContext testContext;

        @BeforeEach
        void init() {
            var userInfo = new LocalUserInfo();
            userInfo.setUserId("user1");
            userInfo.setPassword("passwd");
            userInfo.setRoles(Set.of("simple-user"));


            testContext = IntegrationTestContext.builder()
                    .withSingleton(ProtectedModelDataPage1.class)
                    .withSingleton(ProtectedModelDataPage2.class)
                    .withLoggedInUser(userInfo)
                    .withTestUserService(userInfo)
                    .build();
        }

        @Test
        void test() {
            var result = testContext.openPage("/page1.html");
            var document = result.getDocument();
            var link = document.getElementById("link");

            link.click();

            assertThat(document.getElementByTagName("title").innerText).isEqualTo("Login");
        }
    }

    @Nested
    @DisplayName("Protected model data is accessed with token and user is logged in, roles match")
    class AccessGrantedTest {
        private IntegrationTestContext testContext;

        @BeforeEach
        void init() {
            var userInfo = new LocalUserInfo();
            userInfo.setUserId("user1");
            userInfo.setPassword("passwd");
            userInfo.setRoles(Set.of("simple-user", "admin"));

            testContext = IntegrationTestContext.builder()
                    .withSingleton(ProtectedModelDataPage1.class)
                    .withSingleton(ProtectedModelDataPage2.class)
                    .withLoggedInUser(userInfo)
                    .withTestUserService(userInfo)
                    .build();
        }

        @Test
        void test() {
            var result = testContext.openPage("/page1.html");
            var document = result.getDocument();
            var link = document.getElementById("link");
            System.err.println(document.asString());
            assertThat(document.getElementByTagName("title").innerText).isEqualTo("Page 1");


            link.click();

            assertThat(document.getElementByTagName("title").innerText).isEqualTo("Login");
            assertThat(document.getElementById("redirect").getAttribute("value")).isEqualTo("/page2.html");

            document.getInputElementById("username").setValue("user1");
            document.getInputElementById("password").setValue("passwd");
            document.getElementByTagName("button").click();

            assertThat(document.getElementByTagName("title").innerText).isEqualTo("Page 2");


        }
    }


}
