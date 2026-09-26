package com.testpilot.agent.tool;

import com.testpilot.agent.knowledge.KnowledgeBase;
import com.testpilot.agent.knowledge.KnowledgeBaseService;
import com.testpilot.agent.rag.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeTool implements AgentTool {

    private final KnowledgeBaseService knowledgeBaseService;
    private final RagService ragService;

    @Override
    public String getName() {
        return "searchTestingKnowledge";
    }

    @Override
    public String getDescription() {
        return "Search testing knowledge base using RAG (Retrieval-Augmented Generation) for test methods, historical bugs, project rules, business requirements, and API documentation.";
    }

    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "query": {"type": "string"},
                "category": {"type": "string", "enum": ["test_method","bug_history","project_rule","requirement","api_doc"]},
                "limit": {"type": "integer"},
                "useRag": {"type": "boolean"}
            },
            "required": ["query"]
        }""";
    }

    @Override
    public ToolResult execute(ToolContext context, Map<String, Object> args) {
        String query = (String) args.get("query");
        String category = (String) args.get("category");
        int limit = args.get("limit") != null ? ((Number) args.get("limit")).intValue() : 5;
        boolean useRag = args.get("useRag") != null && Boolean.TRUE.equals(args.get("useRag"));

        long start = System.currentTimeMillis();
        try {
            Long projectId = context.getProjectId();
            List<Map<String, Object>> results;

            if (useRag) {
                // Use RAG for semantic search
                List<RagService.RagResult> ragResults = ragService.search(projectId, query, category, limit);
                results = ragResults.stream().map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", r.getId());
                    map.put("content", r.getContent());
                    map.put("category", r.getCategory());
                    map.put("source", r.getSource());
                    map.put("relevanceScore", r.getRelevanceScore());
                    return map;
                }).collect(Collectors.toList());
            } else {
                // Use traditional keyword search
                List<KnowledgeBase> knowledgeResults = knowledgeBaseService.search(projectId, query, category, limit);
                results = knowledgeResults.stream().map(k -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", k.getId());
                    map.put("title", k.getTitle());
                    map.put("category", k.getCategory());
                    map.put("content", k.getContent());
                    map.put("tags", k.getTags());
                    return map;
                }).collect(Collectors.toList());
            }

            long latency = System.currentTimeMillis() - start;
            return ToolResult.builder()
                    .success(true)
                    .output("Found " + results.size() + " knowledge entries for: " + query)
                    .data(results)
                    .latencyMs(latency)
                    .build();
        } catch (Exception e) {
            return ToolResult.fail("Knowledge search failed: " + e.getMessage());
        }
    }
}
