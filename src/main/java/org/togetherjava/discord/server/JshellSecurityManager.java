package org.togetherjava.discord.server;

import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.Arrays;

public class JshellSecurityManager extends SecurityManager {

  @Override
  public void checkPermission(Permission perm) {
    // allow all but Jshell to bypass this
    if (!comesFromBotCode() && comesFromJshell()) {
      if (!isLambdaReflectCall(perm)) {
        super.checkPermission(perm);
      }
    }
  }

  private boolean comesFromJshell() {
    return Arrays.stream(getClassContext())
        .anyMatch(aClass -> aClass.getName().contains("REPL"));
  }

  private boolean comesFromBotCode() {
    return Arrays.stream(getClassContext())
        .skip(2)
        .anyMatch(aClass -> aClass == getClassContext()[0]);
  }

  private boolean isLambdaReflectCall(Permission permission) {
    return permission instanceof ReflectPermission
        && permission.getName().equals("suppressAccessChecks")
        && comesFromLambdaCallsite();
  }

  /**
   * Allow lambdas to run properly. Likely also disables the check for code running <em>inside</em>
   * the lambda, which means we have to rely on the bytecode inspection to secure that.
   *
   * @return true if a lambda call site is involved
   */
  private boolean comesFromLambdaCallsite() {
    return Arrays.stream(getClassContext())
        .anyMatch(aClass -> aClass.getName().equals("java.lang.invoke.LambdaMetafactory"));
  }
}
