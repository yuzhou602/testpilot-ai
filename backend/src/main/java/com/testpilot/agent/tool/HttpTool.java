package com.testpilot.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpTool implements AgentTool {

    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "executeHttpRequest";
    }

    @Override
    public String getDescription() {
        return "Execute an HTTP request against the target API. Supports GET, POST, PUT, PATCH, DELETE.";
    }

    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "method": {"type": "string", "enum": ["GET","POST","PUT","PATCH","DELETE"]},
                "url": {"type": "string"},
                "headers": {"type": "object"},
                "body": {"type": "object"},
                "queryParams": {"type": "object"}
            },
            "required": ["method", "url"]
        }""";
    }

    @Override
    public ToolResult execute(ToolContext context, Map<String, Object> args) {
        String method = (String) args.getOrDefault("method", "GET");
        String url = (String) args.get("url");
        Map<String, String> headers = (Map<String, String>) args.get("headers");
        Object body = args.get("body");

        long start = System.currentTimeMillis();
        try {
            WebClient.Builder builder = WebClient.builder()
                    .baseUrl(url)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            if (headers != null) {
                headers.forEach(builder::defaultHeader);
            }

            WebClient client = builder.build();
            String responseBody;
            int statusCode;

            var exchange = client.method(HttpMethod.valueOf(method.toUpperCase(Locale.ROOT)))
                    .bodyValue(body != null ? body : "")
                    .exchangeToMono(response -> response.bodyToMono(String.class)
                            .map(bodyStr -> Map.of(
                                    "status", response.statusCode().value(),
                                    "body", bodyStr != null ? bodyStr : "")))
                    .block(Duration.ofSeconds(30));

            statusCode = (int) exchange.get("status");
            responseBody = (String) exchange.get("body");
            long latency = System.currentTimeMillis() - start;

            Map<String, Object> result = Map.of(
                    "statusCode", statusCode,
                    "body", responseBody,
                    "latencyMs", latency,
                    "method", method,
                    "url", url
            );

            return ToolResult.builder()
                    .success(true)
                    .output("HTTP " + method + " " + url + " -> " + statusCode + " (" + latency + "ms)")
                    .data(result)
                    .latencyMs(latency)
                    .build();

        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("HTTP request failed: {} {} -> {}", method, url, e.getMessage());
            return ToolResult.builder()
                    .success(false)
                    .error("HTTP request failed: " + e.getMessage())
                    .latencyMs(latency)
                    .build();
        }
    }
}
