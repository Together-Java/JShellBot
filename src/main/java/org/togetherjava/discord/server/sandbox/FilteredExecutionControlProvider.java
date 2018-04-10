package org.togetherjava.discord.server.sandbox;

import jdk.jshell.execution.JdiExecutionControlProvider;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class FilteredExecutionControlProvider implements ExecutionControlProvider {

    private final JdiExecutionControlProvider jdiExecutionControlProvider;

    public FilteredExecutionControlProvider() {
        jdiExecutionControlProvider = new JdiExecutionControlProvider();
    }

    @Override
    public String name() {
        return "filtered";
    }

    @Override
    public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters) throws Throwable {
        ExecutionControl hijackedExecutionControl = jdiExecutionControlProvider.generate(env, parameters);
        FilteredExecutionControl filteredExecutionControl = new FilteredExecutionControl();

        return (ExecutionControl) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ExecutionControl.class},
                new ExecutionControlDelegatingProxy(filteredExecutionControl, hijackedExecutionControl)
        );
    }

    private static class ExecutionControlDelegatingProxy implements InvocationHandler {
        private FilteredExecutionControl target;
        private ExecutionControl hijackedExecutionControl;

        private ExecutionControlDelegatingProxy(FilteredExecutionControl target, ExecutionControl hijackedExecutionControl) {
            this.target = target;
            this.hijackedExecutionControl = hijackedExecutionControl;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("load".equals(method.getName())
                    && method.getParameterTypes()[0] == ExecutionControl.ClassBytecodes.class
                    && args.length != 0) {

                target.load((ExecutionControl.ClassBytecodes[]) args[0]);
            }

            return method.invoke(hijackedExecutionControl, args);
        }
    }
}
