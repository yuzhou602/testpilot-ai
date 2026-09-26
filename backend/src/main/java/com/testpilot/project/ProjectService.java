package com.testpilot.project;

import com.testpilot.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final TestProjectRepository projectRepository;

    public TestProject createProject(TestProject project) {
        return projectRepository.save(project);
    }

    public List<TestProject> getUserProjects(Long userId) {
        return projectRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public TestProject getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
    }

    public TestProject updateProject(Long id, TestProject updated) {
        TestProject project = getProject(id);
        project.setName(updated.getName());
        project.setDescription(updated.getDescription());
        project.setBaseUrl(updated.getBaseUrl());
        project.setOpenApiUrl(updated.getOpenApiUrl());
        project.setOpenApiSpec(updated.getOpenApiSpec());
        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
