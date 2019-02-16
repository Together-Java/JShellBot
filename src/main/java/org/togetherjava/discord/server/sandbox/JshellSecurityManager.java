package org.togetherjava.discord.server.sandbox;

import java.security.Permission;
import java.util.Arrays;

/**
 * The {@link SecurityManager} used to limit JShell's permissions.
 */
public class JshellSecurityManager extends SecurityManager {

  @Override
  public void checkPermission(Permission perm) {
    if (comesFromMe()) {
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

  private boolean comesFromMe() {
    return Arrays.stream(getClassContext())
        // one frame for this method, one frame for the call to checkPermission
        .skip(2)
        // see if the security manager appears anywhere else in the context. If so, we initiated
        // the call
        .anyMatch(aClass -> aClass == getClass());
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
