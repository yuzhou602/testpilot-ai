package com.testpilot.requirement;

import com.testpilot.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final RequirementRuleRepository ruleRepository;

    public Requirement createRequirement(Requirement requirement) {
        return requirementRepository.save(requirement);
    }

    public List<Requirement> getProjectRequirements(Long projectId) {
        return requirementRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public Requirement getRequirement(Long id) {
        return requirementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Requirement not found"));
    }

    public Requirement updateRequirement(Long id, Requirement updated) {
        Requirement req = getRequirement(id);
        req.setTitle(updated.getTitle());
        req.setDescription(updated.getDescription());
        req.setStatus(updated.getStatus());
        req.setRiskLevel(updated.getRiskLevel());
        return requirementRepository.save(req);
    }

    public void deleteRequirement(Long id) {
        requirementRepository.deleteById(id);
    }

    public double calculateCoverage(Long requirementId) {
        List<RequirementRule> rules = ruleRepository.findByRequirementId(requirementId);
        if (rules.isEmpty()) return 0.0;
        long covered = rules.stream().filter(RequirementRule::getCovered).count();
        return (double) covered / rules.size() * 100;
    }
}
