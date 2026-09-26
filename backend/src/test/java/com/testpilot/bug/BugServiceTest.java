package com.testpilot.bug;

import com.testpilot.common.enums.BugSeverity;
import com.testpilot.common.enums.BugStatus;
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
class BugServiceTest {

    @InjectMocks
    private BugService bugService;

    @Mock
    private TestBugRepository bugRepository;

    private TestBug testBug;

    @BeforeEach
    void setUp() {
        testBug = TestBug.builder()
                .id(1L)
                .projectId(1L)
                .executionId(1L)
                .title("SQL Injection Vulnerability")
                .description("SQL injection found in login endpoint")
                .severity(BugSeverity.CRITICAL)
                .status(BugStatus.OPEN)
                .module("Authentication")
                .confidence(0.95)
                .aiGenerated(true)
                .confirmed(false)
                .build();
    }

    @Test
    void shouldCreateBug() {
        when(bugRepository.save(any())).thenReturn(testBug);

        TestBug result = bugService.createBug(testBug);

        assertNotNull(result);
        assertEquals("SQL Injection Vulnerability", result.getTitle());
    }

    @Test
    void shouldGetProjectBugs() {
        List<TestBug> bugs = Arrays.asList(testBug);
        when(bugRepository.findByProjectIdOrderByCreatedAtDesc(1L)).thenReturn(bugs);

        List<TestBug> result = bugService.getProjectBugs(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetBugById() {
        when(bugRepository.findById(1L)).thenReturn(Optional.of(testBug));

        TestBug result = bugService.getBug(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldUpdateBug() {
        when(bugRepository.findById(1L)).thenReturn(Optional.of(testBug));
        when(bugRepository.save(any())).thenReturn(testBug);

        TestBug updated = TestBug.builder()
                .status(BugStatus.CONFIRMED)
                .build();

        TestBug result = bugService.updateBug(1L, updated);

        assertNotNull(result);
    }

    @Test
    void shouldDeleteBug() {
        when(bugRepository.findById(1L)).thenReturn(Optional.of(testBug));

        assertDoesNotThrow(() -> bugService.deleteBug(1L));
    }

    @Test
    void shouldGetBugStats() {
        when(bugRepository.countByProjectId(1L)).thenReturn(10L);
        when(bugRepository.countByProjectIdAndSeverity(1L, BugSeverity.CRITICAL)).thenReturn(2L);

        var stats = bugService.getBugStats(1L);

        assertNotNull(stats);
    }
}
