package org.togetherjava.discord.server.sandbox;

import java.security.Permission;
import java.util.Arrays;

/**
 * The {@link SecurityManager} used to limit JShell's permissions.
 */
public class JshellSecurityManager extends SecurityManager {

  private static final String[] WHITELISTED_CLASSES = {
      // CallSite is needed for lambdas
      "java.lang.invoke.CallSite",
      // enum set/map because they do a reflective invocation to get the universe
      // let's hope that is actually safe and EnumSet/Map can not be used to invoke arbitrary code
      "java.util.EnumSet", "java.util.EnumMap",
      // Character.getName accesses a system resource (uniName.dat)
      "java.lang.CharacterName",
      // Local specific decimal formatting
      "java.text.DecimalFormatSymbols"
  };


  @Override
  public void checkPermission(Permission perm) {
    if (comesFromMe()) {
      return;
    }

    // lambda init call
    if (containsWhitelistedClass()) {
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

  private boolean containsWhitelistedClass() {
    for (Class<?> aClass : getClassContext()) {
      for (String s : WHITELISTED_CLASSES) {
        if (s.equals(aClass.getName())) {
          return true;
        }
      }
    }
    return false;
  }
}
