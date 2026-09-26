package com.testpilot.requirement;

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
class RequirementServiceTest {

    @InjectMocks
    private RequirementService requirementService;

    @Mock
    private RequirementRepository requirementRepository;

    @Mock
    private RequirementRuleRepository ruleRepository;

    private Requirement testRequirement;

    @BeforeEach
    void setUp() {
        testRequirement = Requirement.builder()
                .id(1L)
                .projectId(1L)
                .title("User Login")
                .description("User authentication with username and password")
                .source("MANUAL")
                .status("ACTIVE")
                .riskLevel("HIGH")
                .coverageScore(85.0)
                .build();
    }

    @Test
    void shouldCreateRequirement() {
        when(requirementRepository.save(any())).thenReturn(testRequirement);

        Requirement result = requirementService.createRequirement(testRequirement);

        assertNotNull(result);
        assertEquals("User Login", result.getTitle());
    }

    @Test
    void shouldGetProjectRequirements() {
        List<Requirement> requirements = Arrays.asList(testRequirement);
        when(requirementRepository.findByProjectIdOrderByCreatedAtDesc(1L)).thenReturn(requirements);

        List<Requirement> result = requirementService.getProjectRequirements(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetRequirementById() {
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(testRequirement));

        Requirement result = requirementService.getRequirement(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldUpdateRequirement() {
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(testRequirement));
        when(requirementRepository.save(any())).thenReturn(testRequirement);

        Requirement updated = Requirement.builder()
                .title("Updated Login")
                .build();

        Requirement result = requirementService.updateRequirement(1L, updated);

        assertNotNull(result);
    }

    @Test
    void shouldDeleteRequirement() {
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(testRequirement));

        assertDoesNotThrow(() -> requirementService.deleteRequirement(1L));
    }
}
