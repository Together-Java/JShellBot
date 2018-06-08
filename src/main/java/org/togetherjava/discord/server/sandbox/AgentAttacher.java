package org.togetherjava.discord.server.sandbox;

import me.ialistannen.jvmagentutils.instrumentation.JvmUtils;
import org.togetherjava.discord.server.JshellSecurityManager;

import java.nio.file.Path;

public class AgentAttacher {

    private static final Path agentJar = JvmUtils.generateAgentJar(
            AgentMain.class, AgentMain.class, JshellSecurityManager.class
    );

    /**
     * Returns the command line argument that attaches the agent.
     *
     * @return the command line argument to start it
     */
    public static String getCommandLineArgument() {
        return "-javaagent:" + agentJar.toAbsolutePath();
    }
}
