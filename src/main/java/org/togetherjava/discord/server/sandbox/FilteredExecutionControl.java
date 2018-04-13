package org.togetherjava.discord.server.sandbox;

import jdk.jshell.execution.LocalExecutionControl;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.*;

import java.util.HashSet;
import java.util.Set;

public class FilteredExecutionControl extends LocalExecutionControl {

    private Set<Pair<String, String>> blockedMethods;
    private Set<String> blockedPackages;
    private Set<String> blockedClasses;

    public FilteredExecutionControl() {
        blockedMethods = new HashSet<>();
        blockedPackages = new HashSet<>();
        blockedClasses = new HashSet<>();

        blockPackage("java.lang.reflect");
        blockPackage("java.lang.invoke");
        blockClass("java.lang.ProcessBuilder");
        blockClass("java.lang.ProcessHandle");
        blockClass("java.lang.Runtime");
        blockMethod("java.lang.reflect.Method", "invoke");
        blockMethod("java.lang.System", "exit");
        blockMethod("java.lang.Thread", "sleep");
        blockMethod("java.lang.Thread", "wait");
        blockMethod("java.lang.Thread", "notify");
        blockMethod("java.lang.Thread", "currentThread");
    }

    public void blockMethod(String clazz, String methodName) {
        blockedMethods.add(new ImmutablePair<>(clazz, methodName));
    }

    public void blockPackage(String packageName) {
        blockedPackages.add(packageName);
    }

    public void blockClass(String clazz) {
        blockedClasses.add(clazz);
    }


    @Override
    public void load(ClassBytecodes[] cbcs) throws ClassInstallException, NotImplementedException, EngineTerminationException {
        for (ClassBytecodes bytecodes : cbcs) {
            ClassReader classReader = new ClassReader(bytecodes.bytecodes());
            classReader.accept(new ClassVisitor(Opcodes.ASM6) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                                 String[] exceptions) {
                    return new FilteringMethodVisitor();
                }
            }, 0);
        }

        super.load(cbcs);
    }

    private class FilteringMethodVisitor extends MethodVisitor {
        private FilteringMethodVisitor() {
            super(Opcodes.ASM6);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
                                    boolean isInterface) {
            if (blockedClasses.contains(sanitizeClassName(owner))) {
                throw new UnsupportedOperationException("Naughty (class): " + owner);
            }
            if (blockedMethods.contains(new ImmutablePair<>(sanitizeClassName(owner), name))) {
                throw new UnsupportedOperationException("Naughty (meth): " + owner + "#" + name);
            }
            if (isPackageOrParentBlocked(sanitizeClassName(owner))) {
                throw new UnsupportedOperationException("Naughty (pack): " + owner);
            }
        }

        private boolean isPackageOrParentBlocked(String sanitizedPackage) {
            if (sanitizedPackage == null || sanitizedPackage.isEmpty()) {
                return false;
            }
            if (blockedPackages.contains(sanitizedPackage)) {
                return true;
            }

            int nextDot = sanitizedPackage.lastIndexOf('.');

            return nextDot >= 0 && isPackageOrParentBlocked(sanitizedPackage.substring(0, nextDot));
        }

        private String sanitizeClassName(String owner) {
            return owner.replace("/", ".");
        }


        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
                                           Object... bootstrapMethodArguments) {
            // TODO: 04.04.18 Implement this method
            System.out.println("Calling dymn " + name + " " + descriptor + " " + bootstrapMethodHandle);
        }
    }
}
