package org.togetherjava.discord.server.sandbox;

import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;

import java.util.Map;

public class FilteredExecutionControlProvider implements ExecutionControlProvider {

    @Override
    public String name() {
        return "filtered";
    }

    @Override
    public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters) throws Throwable {
        return new FilteredExecutionControl();
    }
}
