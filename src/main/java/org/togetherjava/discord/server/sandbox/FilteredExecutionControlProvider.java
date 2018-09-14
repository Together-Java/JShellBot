package org.togetherjava.discord.server.sandbox;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import jdk.jshell.execution.JdiExecutionControlProvider;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;
import org.apache.commons.lang3.tuple.Pair;

public class FilteredExecutionControlProvider implements ExecutionControlProvider {

  private final JdiExecutionControlProvider jdiExecutionControlProvider;
  private final Supplier<FilteredExecutionControl> executionControlSupplier;

  public FilteredExecutionControlProvider(Collection<String> blockedPackages,
      Collection<String> blockedClasses,
      Collection<Pair<String, String>> blockedMethods) {
    this.jdiExecutionControlProvider = new JdiExecutionControlProvider();
    this.executionControlSupplier = () -> new FilteredExecutionControl(
        blockedPackages, blockedClasses, blockedMethods
    );
  }

  @Override
  public String name() {
    return "filtered";
  }

  @Override
  public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters)
      throws Throwable {
    ExecutionControl hijackedExecutionControl = jdiExecutionControlProvider
        .generate(env, parameters);
    FilteredExecutionControl filteredExecutionControl = executionControlSupplier.get();

    return (ExecutionControl) Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class[]{ExecutionControl.class},
        new ExecutionControlDelegatingProxy(filteredExecutionControl, hijackedExecutionControl)
    );
  }

  private static class ExecutionControlDelegatingProxy implements InvocationHandler {

    private FilteredExecutionControl target;
    private ExecutionControl hijackedExecutionControl;

    private ExecutionControlDelegatingProxy(FilteredExecutionControl target,
        ExecutionControl hijackedExecutionControl) {
      this.target = target;
      this.hijackedExecutionControl = hijackedExecutionControl;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if ("load".equals(method.getName())
          && method.getParameterTypes()[0] == ExecutionControl.ClassBytecodes[].class
          && args.length != 0) {

        target.load((ExecutionControl.ClassBytecodes[]) args[0]);
      }

      // this unwrapping is necessary for JShell to detect that an exception it can handle was thrown
      try {
        return method.invoke(hijackedExecutionControl, args);
      } catch (InvocationTargetException e) {
        throw e.getCause();
      }
    }
  }
}
