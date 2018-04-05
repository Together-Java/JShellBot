package org.togetherjava.discord.server.sandbox;

import java.security.*;
import java.util.function.Supplier;

public class Sandbox {

    private AccessControlContext controlContext;

    public Sandbox() {
        PermissionCollection permissionCollection = new Permissions();
        permissionCollection.add(new RuntimePermission("accessDeclaredMembers"));

        // allow all class access, as only a whitelist is possible
        permissionCollection.add(new RuntimePermission("accessClassInPackage"));

        ProtectionDomain protectionDomain = new ProtectionDomain(null, permissionCollection);
        this.controlContext = new AccessControlContext(new ProtectionDomain[]{protectionDomain});
    }

    /**
     * Runs some code in a makeshift sandbox, allowing access to <strong>all</strong> classes, but no
     * file / network IO etc.
     *
     * @param supplier the code to run
     * @param <T>      the value the method returns
     * @return the result of running it
     */
    public <T> T runInSandBox(Supplier<T> supplier) {
        return AccessController.doPrivileged(
                (PrivilegedAction<T>) supplier::get,
                controlContext
        );
    }
}
