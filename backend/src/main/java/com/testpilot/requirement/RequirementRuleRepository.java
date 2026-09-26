package com.testpilot.requirement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequirementRuleRepository extends JpaRepository<RequirementRule, Long> {
    List<RequirementRule> findByRequirementId(Long requirementId);
    long countByRequirementIdAndCovered(Long requirementId, Boolean covered);
}
