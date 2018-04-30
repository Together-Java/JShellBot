package org.togetherjava.discord.server.sandbox;

import jdk.jshell.execution.LocalExecutionControl;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class FilteredExecutionControl extends LocalExecutionControl {

    private final List<Pair<String, Pattern>> blockedMethods;
    private final List<Pattern> blockedPackages;
    private final List<Pattern> blockedClasses;

    /**
     * Creates a new {@link FilteredExecutionControl}.
     *
     * @param blockedPackages a collection with regular expressions for blocked packages
     * @param blockedClasses  a collection with regular expressions for blocked classes
     * @param blockedMethods  a collection in the form of {@code "ClassName -> Method regular expression"}
     */
    FilteredExecutionControl(Collection<String> blockedPackages, Collection<String> blockedClasses,
                             Collection<Pair<String, String>> blockedMethods) {

        this.blockedMethods = new ArrayList<>();
        this.blockedPackages = new ArrayList<>();
        this.blockedClasses = new ArrayList<>();

        // TODO: Not really our job to parse that.
        for (String packagePattern : blockedPackages) {
            blockPackage(Pattern.compile(packagePattern));
        }
        for (String classPattern : blockedClasses) {
            blockClass(Pattern.compile(classPattern));
        }
        for (Pair<String, String> pair : blockedMethods) {
            blockMethod(pair.getKey(), Pattern.compile(pair.getValue()));
        }
    }

    private void blockMethod(String clazz, Pattern methodName) {
        blockedMethods.add(new ImmutablePair<>(clazz, methodName));
    }

    private void blockPackage(Pattern packagePattern) {
        blockedPackages.add(packagePattern);
    }

    private void blockClass(Pattern classPattern) {
        blockedClasses.add(classPattern);
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

    private boolean isClassBlocked(String name) {
        return blockedClasses.stream()
                .map(FilteredExecutionControl::asMatchingPredicate)
                .anyMatch(pred -> pred.test(name));
    }

    private boolean isMethodBlocked(String className, String methodName) {
        return blockedMethods.stream()
                .filter(pair -> pair.getKey().equals(className))
                .map(pair -> asMatchingPredicate(pair.getValue()))
                .anyMatch(pred -> pred.test(methodName));
    }

    private boolean isPackageBlocked(String packageName) {
        return blockedPackages.stream()
                .map(FilteredExecutionControl::asMatchingPredicate)
                .anyMatch(pred -> pred.test(packageName));
    }

    private static Predicate<String> asMatchingPredicate(Pattern pattern) {
        return s -> pattern.matcher(s).matches();
    }

    private boolean isPackageOrParentBlocked(String sanitizedPackage) {
        if (sanitizedPackage == null || sanitizedPackage.isEmpty()) {
            return false;
        }
        if (isPackageBlocked(sanitizedPackage)) {
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
            if (isClassBlocked(sanitizeClassName(owner))) {
                throw new UnsupportedOperationException("Naughty (class): " + owner);
            }
            if (isMethodBlocked(sanitizeClassName(owner), name)) {
                throw new UnsupportedOperationException("Naughty (meth): " + owner + "#" + name);
            }
            if (isPackageOrParentBlocked(sanitizeClassName(owner))) {
                throw new UnsupportedOperationException("Naughty (pack): " + owner);
            }
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
