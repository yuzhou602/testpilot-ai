package com.testpilot.testcase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCaseService {

    private final TestScenarioRepository scenarioRepository;
    private final TestCaseRepository testCaseRepository;

    public TestScenario createScenario(TestScenario scenario) {
        return scenarioRepository.save(scenario);
    }

    public List<TestScenario> getProjectScenarios(Long projectId) {
        return scenarioRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public TestCase createTestCase(TestCase testCase) {
        return testCaseRepository.save(testCase);
    }

    public List<TestCase> getScenarioTestCases(Long scenarioId) {
        return testCaseRepository.findByScenarioId(scenarioId);
    }

    public List<TestCase> getProjectTestCases(Long projectId) {
        return testCaseRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public TestCase updateTestCase(Long id, TestCase updated) {
        TestCase tc = testCaseRepository.findById(id).orElseThrow();
        tc.setTitle(updated.getTitle());
        tc.setDescription(updated.getDescription());
        tc.setPrecondition(updated.getPrecondition());
        tc.setSteps(updated.getSteps());
        tc.setExpectedResult(updated.getExpectedResult());
        tc.setTestData(updated.getTestData());
        tc.setPriority(updated.getPriority());
        return testCaseRepository.save(tc);
    }
}
