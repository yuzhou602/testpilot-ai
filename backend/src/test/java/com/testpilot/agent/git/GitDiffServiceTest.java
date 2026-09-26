package com.testpilot.agent.git;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitDiffServiceTest {

    @InjectMocks
    private GitDiffService gitDiffService;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    private String testDiff;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        testDiff = """
                diff --git a/src/main/java/com/example/UserService.java b/src/main/java/com/example/UserService.java
                index 1234567..abcdefg 100644
                --- a/src/main/java/com/example/UserService.java
                +++ b/src/main/java/com/example/UserService.java
                @@ -10,6 +10,10 @@ public class UserService {
                     public User login(String username, String password) {
                -        return userRepository.findByUsername(username);
                +        User user = userRepository.findByUsername(username);
                +        if (user == null) {
                +            throw new UserNotFoundException("User not found");
                +        }
                +        return user;
                     }
                """;
    }

    @Test
    void shouldParseDiff() {
        List<GitDiffService.GitFileChange> changes = gitDiffService.parseDiff(testDiff);

        assertFalse(changes.isEmpty());
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).getAdditions() > 0);
    }

    @Test
    void shouldAnalyzeDiff() {
        when(chatClient.prompt()).thenReturn(chatClient);
        when(chatClient.system(any())).thenReturn(chatClient);
        when(chatClient.user(any())).thenReturn(chatClient);
        when(chatClient.call()).thenReturn(chatClient);
        when(chatClient.content()).thenReturn("""
                {
                    "summary": "Added null check for user login",
                    "affectedModules": ["UserService", "Authentication"],
                    "riskAreas": [
                        {
                            "area": "User Login",
                            "riskLevel": "HIGH",
                            "reason": "Changed login flow"
                        }
                    ],
                    "suggestedTests": [
                        {
                            "testName": "Test login with non-existent user",
                            "testType": "UNIT",
                            "description": "Verify UserNotFoundException is thrown",
                            "priority": "P0"
                        }
                    ],
                    "breakingChanges": []
                }
                """);

        GitDiffService.GitDiffAnalysis analysis = gitDiffService.analyzeDiff(testDiff);

        assertNotNull(analysis);
        assertNotNull(analysis.getSummary());
    }

    @Test
    void shouldGenerateRegressionTests() {
        GitDiffService.GitDiffAnalysis analysis = GitDiffService.GitDiffAnalysis.builder()
                .summary("Added null check")
                .affectedModules(List.of("UserService"))
                .riskAreas(List.of())
                .suggestedTests(List.of())
                .breakingChanges(List.of())
                .build();

        when(chatClient.prompt()).thenReturn(chatClient);
        when(chatClient.system(any())).thenReturn(chatClient);
        when(chatClient.user(any())).thenReturn(chatClient);
        when(chatClient.call()).thenReturn(chatClient);
        when(chatClient.content()).thenReturn("""
                {
                    "testCases": [
                        {
                            "name": "Test null user",
                            "description": "Verify exception thrown",
                            "steps": ["Call login with invalid user"],
                            "expectedResult": "UserNotFoundException",
                            "testType": "UNIT",
                            "priority": "P0"
                        }
                    ]
                }
                """);

        String result = gitDiffService.generateRegressionTests(analysis);

        assertNotNull(result);
        assertTrue(result.contains("testCases"));
    }
}
