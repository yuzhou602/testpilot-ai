package com.testpilot.project;

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
class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private TestProjectRepository projectRepository;

    private TestProject testProject;

    @BeforeEach
    void setUp() {
        testProject = TestProject.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .userId(1L)
                .baseUrl("http://localhost:3001")
                .active(true)
                .build();
    }

    @Test
    void shouldCreateProject() {
        when(projectRepository.save(any())).thenReturn(testProject);

        TestProject result = projectService.createProject(testProject);

        assertNotNull(result);
        assertEquals("Test Project", result.getName());
    }

    @Test
    void shouldGetProjectById() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        TestProject result = projectService.getProject(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldGetUserProjects() {
        List<TestProject> projects = Arrays.asList(testProject);
        when(projectRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(projects);

        List<TestProject> result = projectService.getUserProjects(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldUpdateProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any())).thenReturn(testProject);

        TestProject updatedProject = TestProject.builder()
                .name("Updated Project")
                .build();

        TestProject result = projectService.updateProject(1L, updatedProject);

        assertNotNull(result);
    }

    @Test
    void shouldDeleteProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        assertDoesNotThrow(() -> projectService.deleteProject(1L));
    }
}
