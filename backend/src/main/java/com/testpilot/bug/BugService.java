package com.testpilot.bug;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BugService {

    private final TestBugRepository bugRepository;

    public TestBug createBug(TestBug bug) {
        return bugRepository.save(bug);
    }

    public List<TestBug> getProjectBugs(Long projectId) {
        return bugRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public TestBug getBug(Long id) {
        return bugRepository.findById(id).orElseThrow();
    }

    public TestBug updateBug(Long id, TestBug updated) {
        TestBug bug = getBug(id);
        bug.setTitle(updated.getTitle());
        bug.setDescription(updated.getDescription());
        bug.setSeverity(updated.getSeverity());
        bug.setStatus(updated.getStatus());
        bug.setConfirmed(updated.getConfirmed());
        return bugRepository.save(bug);
    }

    public long countByProject(Long projectId) {
        return bugRepository.countByProjectId(projectId);
    }
}
