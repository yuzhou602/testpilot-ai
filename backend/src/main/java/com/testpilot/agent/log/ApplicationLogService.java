package com.testpilot.agent.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationLogService {

    private final ApplicationLogRepository repository;

    public ApplicationLog save(ApplicationLog log) {
        return repository.save(log);
    }

    public List<ApplicationLog> findByProjectAndLevel(Long projectId, String level, int sinceMinutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(sinceMinutes);
        return repository.findByProjectAndLevel(projectId, level, since);
    }

    public List<ApplicationLog> findByProjectAndKeyword(Long projectId, String keyword) {
        return repository.findByProjectAndKeyword(projectId, keyword);
    }

    public List<ApplicationLog> findByProjectAndLevelAndKeyword(Long projectId, String level, String keyword, int sinceMinutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(sinceMinutes);
        return repository.findByProjectAndLevelAndKeyword(projectId, level, keyword, since);
    }
}
