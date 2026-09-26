package com.testpilot.testcase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestCaseServiceTest {

    @InjectMocks
    private TestCaseService testCaseService;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private TestScenarioRepository scenarioRepository;

    private TestCase testCase;

    @BeforeEach
    void setUp() {
        testCase = TestCase.builder()
                .id(1L)
                .projectId(1L)
                .scenarioId(1L)
                .caseCode("TC_LOGIN_001")
                .title("Valid Login")
                .description("Login with correct username and password")
                .precondition("User exists in database")
                .steps("1. Send POST /api/auth/login with valid credentials")
                .expectedResult("{\"status\": 200}")
                .testData("{\"username\": \"testuser\", \"password\": \"Test@123\"}")
                .testType("API")
                .strategy("EQUIVALENCE_PARTITION")
                .priority(1)
                .automated(true)
                .aiGenerated(true)
                .build();
    }

    @Test
    void shouldCreateTestCase() {
        when(testCaseRepository.save(any())).thenReturn(testCase);

        TestCase result = testCaseService.createTestCase(testCase);

        assertNotNull(result);
        assertEquals("TC_LOGIN_001", result.getCaseCode());
    }

    @Test
    void shouldGetProjectTestCases() {
        List<TestCase> testCases = Arrays.asList(testCase);
        when(testCaseRepository.findByProjectIdOrderByCreatedAtDesc(1L)).thenReturn(testCases);

        List<TestCase> result = testCaseService.getProjectTestCases(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetTestCaseById() {
        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));

        TestCase result = testCaseService.getTestCase(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldUpdateTestCase() {
        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(testCaseRepository.save(any())).thenReturn(testCase);

        TestCase updated = TestCase.builder()
                .title("Updated Login Test")
                .build();

        TestCase result = testCaseService.updateTestCase(1L, updated);

        assertNotNull(result);
    }

    @Test
    void shouldDeleteTestCase() {
        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));

        assertDoesNotThrow(() -> testCaseService.deleteTestCase(1L));
    }
}
