package org.togetherjava.discord.server.sandbox;

import org.togetherjava.discord.server.JshellSecurityManager;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

/**
 * An agent that sets the security manager JShell uses.
 */
public class AgentMain implements ClassFileTransformer {

    public static void premain(String args, Instrumentation inst) {
        System.setSecurityManager(new JshellSecurityManager());
    }
}
