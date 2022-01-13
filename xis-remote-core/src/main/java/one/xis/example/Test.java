package one.xis.example;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;

import java.lang.instrument.Instrumentation;

public class Test {

    public static void main(String[] args) {
        Instrumentation instrumentation = ByteBuddyAgent.install();
        new ByteBuddy()
                .redefine(T1.class)
                .make()
                .load(
                        T1.class.getClassLoader(),
                        ClassReloadingStrategy.fromInstalledAgent());


    }
}
