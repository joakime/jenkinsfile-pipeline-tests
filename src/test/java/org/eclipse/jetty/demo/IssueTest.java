package org.eclipse.jetty.demo;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.util.Comparator;

import org.junit.jupiter.api.Test;

public class IssueTest
{
    @Test
    public void testStupidJdk11()
    {
        System.getProperties().entrySet().stream()
                .filter(entry -> entry.getKey().toString().matches("^(java|javax|sun|user)\\..*"))
                .sorted(Comparator.comparing(e -> e.getKey().toString()))
                .forEach(entry -> System.err.println("  " + entry.getKey() + " = " + entry.getValue()));

        String jvmRuntime = System.getProperty("java.runtime.version");
        assertThat("java.runtime.version", jvmRuntime, not(startsWith("11")));
    }
}
