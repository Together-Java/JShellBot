package org.togetherjava.discord.server.sandbox;

import java.security.Permission;
import java.util.Arrays;

public class JshellSecurityManager extends SecurityManager {

  @Override
  public void checkPermission(Permission perm) {
    if (!comesFromBotCode()) {
      return;
    }

    // lambda init call
    if (containsClass("java.lang.invoke.CallSite")) {
      return;
    }

    // allow all but Jshell to bypass this
    if (comesFromJshell()) {
      super.checkPermission(perm);
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

  private boolean containsClass(String name) {
    for (Class<?> aClass : getClassContext()) {
      if (aClass.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }
}
