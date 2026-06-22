package com.aicode.agent;

import java.util.Collections;
import java.util.List;

/**
 * Agent 统一规范
 */
public interface Agent {

    AgentType getType();

    default List<AgentType> dependencies() {
        return Collections.emptyList();
    }

    AgentResult execute(AgentContext context);
}
