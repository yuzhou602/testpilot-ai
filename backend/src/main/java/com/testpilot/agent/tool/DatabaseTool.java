package com.testpilot.agent.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseTool implements AgentTool {

    private final JdbcTemplate jdbcTemplate;

    private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
            "DROP", "DELETE", "TRUNCATE", "ALTER", "CREATE", "INSERT", "UPDATE", "GRANT", "REVOKE"
    );

    @Override
    public String getName() {
        return "executeSql";
    }

    @Override
    public String getDescription() {
        return "Execute SQL queries on the test database. SELECT only for safety. Used to check data state and verify test preconditions.";
    }

    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "sql": {"type": "string"},
                "maxRows": {"type": "integer"}
            },
            "required": ["sql"]
        }""";
    }

    @Override
    public ToolResult execute(ToolContext context, Map<String, Object> args) {
        String sql = ((String) args.get("sql")).trim();
        int maxRows = args.get("maxRows") != null ? ((Number) args.get("maxRows")).intValue() : 100;

        // Safety check
        String upperSql = sql.toUpperCase();
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                return ToolResult.fail("Forbidden SQL keyword detected: " + keyword + ". Only SELECT queries allowed.");
            }
        }

        if (!upperSql.startsWith("SELECT")) {
            return ToolResult.fail("Only SELECT queries are allowed for safety.");
        }

        long start = System.currentTimeMillis();
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            long latency = System.currentTimeMillis() - start;

            if (results.size() > maxRows) {
                results = results.subList(0, maxRows);
            }

            return ToolResult.builder()
                    .success(true)
                    .output("Query returned " + results.size() + " rows (" + latency + "ms)")
                    .data(results)
                    .latencyMs(latency)
                    .build();
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("SQL execution failed: {}", e.getMessage());
            return ToolResult.builder()
                    .success(false)
                    .error("SQL error: " + e.getMessage())
                    .latencyMs(latency)
                    .build();
        }
    }
}
