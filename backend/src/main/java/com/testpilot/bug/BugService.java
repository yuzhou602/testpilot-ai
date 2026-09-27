package com.testpilot.bug;

import com.testpilot.common.enums.BugSeverity;
import com.testpilot.common.enums.BugStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public void deleteBug(Long id) {
        bugRepository.delete(getBug(id));
    }

    public Map<String, Object> getBugStats(Long projectId) {
        List<TestBug> bugs = getProjectBugs(projectId);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", bugs.size());
        stats.put("open", bugs.stream().filter(bug -> bug.getStatus() != BugStatus.CLOSED && bug.getStatus() != BugStatus.REJECTED).count());
        stats.put("confirmed", bugs.stream().filter(bug -> Boolean.TRUE.equals(bug.getConfirmed())).count());
        stats.put("critical", bugs.stream().filter(bug -> bug.getSeverity() == BugSeverity.BLOCKER || bug.getSeverity() == BugSeverity.CRITICAL).count());
        return stats;
    }
}
