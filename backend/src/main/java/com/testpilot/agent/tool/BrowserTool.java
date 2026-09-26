package com.testpilot.agent.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrowserTool implements AgentTool {

    private final ObjectMapper objectMapper;
    private final ChatClient.Builder chatClientBuilder;
    private static final ThreadLocal<BrowserContext> contextHolder = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageHolder = new ThreadLocal<>();
    private static final int MAX_HEALING_ATTEMPTS = 3;

    @Override
    public String getName() {
        return "browser";
    }

    @Override
    public String getDescription() {
        return "Control a headless browser with Self-Healing. Actions: navigate, click, fill, select, hover, wait, screenshot, getDOM, findElement. Automatically retries with alternative locators when primary fails.";
    }

    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "action": {"type": "string", "enum": ["navigate","click","fill","select","hover","wait","screenshot","getDOM","findElement"]},
                "url": {"type": "string"},
                "selector": {"type": "string"},
                "value": {"type": "string"},
                "timeout": {"type": "integer"},
                "healLocator": {"type": "boolean"}
            },
            "required": ["action"]
        }""";
    }

    @Override
    public ToolResult execute(ToolContext context, Map<String, Object> args) {
        String action = (String) args.get("action");
        boolean healLocator = args.get("healLocator") != null && Boolean.TRUE.equals(args.get("healLocator"));
        long start = System.currentTimeMillis();

        try {
            ensureBrowser();
            Page page = pageHolder.get();

            switch (action) {
                case "navigate": {
                    String url = (String) args.get("url");
                    page.navigate(url);
                    page.waitForLoadState();
                    return ToolResult.ok("Navigated to " + url, Map.of("url", url, "title", page.title()));
                }
                case "click": {
                    String selector = (String) args.get("selector");
                    return executeWithHealing(page, selector, "click", null, healLocator);
                }
                case "fill": {
                    String selector = (String) args.get("selector");
                    String value = (String) args.get("value");
                    return executeWithHealing(page, selector, "fill", value, healLocator);
                }
                case "select": {
                    String selector = (String) args.get("selector");
                    String value = (String) args.get("value");
                    return executeWithHealing(page, selector, "select", value, healLocator);
                }
                case "hover": {
                    String selector = (String) args.get("selector");
                    return executeWithHealing(page, selector, "hover", null, healLocator);
                }
                case "wait": {
                    int timeout = args.get("timeout") != null ? ((Number) args.get("timeout")).intValue() : 1000;
                    page.waitForTimeout(timeout);
                    return ToolResult.ok("Waited " + timeout + "ms", null);
                }
                case "screenshot": {
                    byte[] screenshot = page.screenshot();
                    String base64 = Base64.getEncoder().encodeToString(screenshot);
                    return ToolResult.ok("Screenshot captured", Map.of("image", base64));
                }
                case "getDOM": {
                    String html = page.content();
                    return ToolResult.ok("DOM retrieved (" + html.length() + " chars)", Map.of("html", html));
                }
                case "findElement": {
                    String selector = (String) args.get("selector");
                    int count = page.locator(selector).count();
                    return ToolResult.ok("Found " + count + " elements for: " + selector,
                            Map.of("selector", selector, "count", count));
                }
                default:
                    return ToolResult.fail("Unknown browser action: " + action);
            }
        } catch (Exception e) {
            log.error("Browser action failed: {}", e.getMessage());
            return ToolResult.fail("Browser error: " + e.getMessage());
        }
    }

    private ToolResult executeWithHealing(Page page, String originalSelector, String action,
                                           String value, boolean healLocator) {
        long start = System.currentTimeMillis();

        // Try original selector first
        try {
            ToolResult result = performAction(page, originalSelector, action, value);
            long latency = System.currentTimeMillis() - start;
            return ToolResult.builder()
                    .success(true)
                    .output(result.getOutput())
                    .data(result.getData())
                    .latencyMs(latency)
                    .build();
        } catch (Exception e) {
            if (!healLocator) {
                return ToolResult.fail("Element not found: " + originalSelector + " - " + e.getMessage());
            }
            log.warn("Primary selector failed: {}, attempting healing...", originalSelector);
        }

        // Self-Healing: try alternative locators
        for (int attempt = 0; attempt < MAX_HEALING_ATTEMPTS; attempt++) {
            try {
                String healedSelector = healSelector(page, originalSelector, action, attempt);
                if (healedSelector != null && !healedSelector.equals(originalSelector)) {
                    ToolResult result = performAction(page, healedSelector, action, value);
                    long latency = System.currentTimeMillis() - start;
                    return ToolResult.builder()
                            .success(true)
                            .output("Self-Healed: " + originalSelector + " -> " + healedSelector + " | " + result.getOutput())
                            .data(result.getData())
                            .latencyMs(latency)
                            .build();
                }
            } catch (Exception ex) {
                log.debug("Healing attempt {} failed: {}", attempt + 1, ex.getMessage());
            }
        }

        return ToolResult.fail("Self-Healing failed for: " + originalSelector);
    }

    private ToolResult performAction(Page page, String selector, String action, String value) {
        Locator locator = page.locator(selector);

        switch (action) {
            case "click":
                locator.click();
                return ToolResult.ok("Clicked: " + selector, null);
            case "fill":
                locator.fill(value);
                return ToolResult.ok("Filled: " + selector, null);
            case "select":
                locator.selectOption(value);
                return ToolResult.ok("Selected: " + value + " in " + selector, null);
            case "hover":
                locator.hover();
                return ToolResult.ok("Hovered: " + selector, null);
            default:
                return ToolResult.fail("Unknown action: " + action);
        }
    }

    private String healSelector(Page page, String originalSelector, String action, int attempt) {
        try {
            // Get DOM for analysis
            String html = page.content();

            String prompt = """
                The CSS selector "%s" failed to find an element for action "%s".

                Here is the current page HTML (truncated):
                %s

                Suggest up to 5 alternative selectors in order of likelihood. Consider:
                1. Text content matching
                2. ARIA roles and labels
                3. Data attributes
                4. Class name variations
                5. ID variations
                6. XPath alternatives

                Return ONLY the selectors as a JSON array of strings, no explanation.
                Example: ["button:text('Login')", "[data-testid='submit']", "#login-btn"]
                """.formatted(originalSelector, action, html.substring(0, Math.min(html.length(), 5000)));

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .system("You are a Playwright selector expert. Return only JSON arrays.")
                    .user(prompt)
                    .call()
                    .content();

            String json = extractJson(response);
            List<String> alternatives = objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

            if (attempt < alternatives.size()) {
                String candidate = alternatives.get(attempt);
                if (page.locator(candidate).count() > 0) {
                    return candidate;
                }
            }

        } catch (Exception e) {
            log.debug("Healing analysis failed: {}", e.getMessage());
        }

        // Fallback: try common patterns
        return tryFallbackSelectors(page, originalSelector, attempt);
    }

    private String tryFallbackSelectors(Page page, String original, int attempt) {
        String[] fallbacks;
        if (original.startsWith("#")) {
            String id = original.substring(1);
            fallbacks = new String[]{
                    "[id='" + id + "']",
                    "[data-testid='" + id + "']",
                    "[aria-label*='" + id + "']"
            };
        } else if (original.startsWith(".")) {
            String cls = original.substring(1);
            fallbacks = new String[]{
                    "[class*='" + cls + "']",
                    "[data-testid*='" + cls + "']"
            };
        } else {
            fallbacks = new String[]{
                    "[type='" + original + "']",
                    "button:has-text('" + original + "')",
                    "a:has-text('" + original + "')"
            };
        }

        if (attempt < fallbacks.length) {
            String candidate = fallbacks[attempt];
            if (page.locator(candidate).count() > 0) {
                return candidate;
            }
        }

        return null;
    }

    private String extractJson(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
        else if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
        return cleaned.trim();
    }

    private void ensureBrowser() {
        if (pageHolder.get() != null) return;
        try {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch();
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            contextHolder.set(context);
            pageHolder.set(page);
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch browser: " + e.getMessage());
        }
    }

    public void closeBrowser() {
        Page page = pageHolder.get();
        BrowserContext ctx = contextHolder.get();
        if (page != null) page.close();
        if (ctx != null) ctx.close();
        pageHolder.remove();
        contextHolder.remove();
    }
}
