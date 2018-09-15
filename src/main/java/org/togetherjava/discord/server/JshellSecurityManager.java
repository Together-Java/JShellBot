package org.togetherjava.discord.server;

import java.security.Permission;
import java.util.Arrays;

public class JshellSecurityManager extends SecurityManager {

  @Override
  public void checkPermission(Permission perm) {
    // allow all but Jshell to bypass this
    if (!comesFromBotCode() && comesFromJshell()) {
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
}
