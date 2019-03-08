package org.togetherjava.discord.server.sandbox;

import jdk.jshell.execution.LocalExecutionControl;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilteredExecutionControl extends LocalExecutionControl {

  private static final Logger LOGGER = LoggerFactory.getLogger(FilteredExecutionControl.class);

  private final WhiteBlackList whiteBlackList;

  /**
   * Creates a new {@link FilteredExecutionControl}.
   *
   * @param whiteBlackList the {@link WhiteBlackList}
   */
  FilteredExecutionControl(WhiteBlackList whiteBlackList) {
    this.whiteBlackList = whiteBlackList;
  }

  @Override
  public void load(ClassBytecodes[] cbcs)
      throws ClassInstallException, NotImplementedException, EngineTerminationException {
    for (ClassBytecodes bytecodes : cbcs) {
      ClassReader classReader = new ClassReader(bytecodes.bytecodes());
      classReader.accept(new ClassVisitor(Opcodes.ASM6) {
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
            String signature,
            String[] exceptions) {
          return new FilteringMethodVisitor();
        }
      }, 0);
    }

    super.load(cbcs);
  }

  private boolean isBlocked(String name) {
    return whiteBlackList.isBlocked(name);
  }

  private boolean isPackageOrParentBlocked(String sanitizedPackage) {
    if (sanitizedPackage == null || sanitizedPackage.isEmpty()) {
      return false;
    }
    if (isBlocked(sanitizedPackage)) {
      return true;
    }

    int nextDot = sanitizedPackage.lastIndexOf('.');

    return nextDot >= 0 && isPackageOrParentBlocked(sanitizedPackage.substring(0, nextDot));
  }


  private class FilteringMethodVisitor extends MethodVisitor {

    private FilteringMethodVisitor() {
      super(Opcodes.ASM6);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
        boolean isInterface) {
      checkAccess(owner, name);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
      checkAccess(owner, name);
    }

    private void checkAccess(String owner, String name) {
      String sanitizedClassName = sanitizeClassName(owner);

      if (isBlocked(sanitizedClassName)) {
        throw new UnsupportedOperationException("Naughty (class): " + sanitizedClassName);
      }
      if (isBlocked(sanitizedClassName + "#" + name)) {
        throw new UnsupportedOperationException(
            "Naughty (meth): " + sanitizedClassName + "#" + name
        );
      }

      // do not check the package if the class or method was explicitely allowed
      if (whiteBlackList.isWhitelisted(sanitizedClassName)
          || whiteBlackList.isWhitelisted(sanitizedClassName + "#" + name)) {
        return;
      }

      if (isPackageOrParentBlocked(sanitizedClassName)) {
        throw new UnsupportedOperationException("Naughty (pack): " + sanitizedClassName);
      }
    }

    private String sanitizeClassName(String owner) {
      return owner.replace("/", ".");
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
        Object... bootstrapMethodArguments) {
      // TODO: 04.04.18 Implement this method
      LOGGER.warn("Calling dymn " + name + " " + descriptor + " " + bootstrapMethodHandle);
    }
  }
}
