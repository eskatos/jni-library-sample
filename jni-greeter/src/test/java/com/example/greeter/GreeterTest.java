package com.example.greeter;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class GreeterTest {

    @Test
    public void testGreeter() {
        Greeter greeter = new Greeter();
        String greeting = greeter.sayHello("World");
        assertThat(greeting, equalTo("Bonjour, World!"));
    }

    @Test
    public void testNullGreeter() {
        Greeter greeter = new Greeter();
        String greeting = greeter.sayHello(null);
        assertThat(greeting, equalTo("name cannot be null"));
    }
}
