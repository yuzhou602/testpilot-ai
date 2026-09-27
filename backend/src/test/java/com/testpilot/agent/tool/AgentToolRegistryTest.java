package com.testpilot.agent.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AgentToolRegistryTest {

    private AgentToolRegistry registry;

    @BeforeEach
    void setUp() {
        AgentTool testTool = new AgentTool() {
            @Override
            public String getName() {
                return "testTool";
            }

            @Override
            public String getDescription() {
                return "Test tool";
            }

            @Override
            public String getParametersSchema() {
                return "{}";
            }

            @Override
            public ToolResult execute(ToolContext context, Map<String, Object> args) {
                return ToolResult.ok("Test result", null);
            }
        };
        registry = new AgentToolRegistry(List.of(testTool));
    }

    @Test
    void shouldRegisterTool() {
        AgentTool tool = registry.getTool("testTool");
        assertNotNull(tool);
        assertEquals("testTool", tool.getName());
    }

    @Test
    void shouldRejectUnknownTool() {
        assertThrows(IllegalArgumentException.class, () -> registry.getTool("unknownTool"));
    }

    @Test
    void shouldGetAllTools() {
        var tools = registry.getAllTools();
        assertTrue(tools.size() >= 1);
    }
}
