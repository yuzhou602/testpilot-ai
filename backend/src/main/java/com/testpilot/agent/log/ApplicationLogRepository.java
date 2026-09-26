package com.testpilot.agent.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationLogRepository extends JpaRepository<ApplicationLog, Long> {

    @Query("SELECT l FROM ApplicationLog l WHERE l.projectId = :projectId AND l.level = :level AND l.createdAt > :since ORDER BY l.createdAt DESC")
    List<ApplicationLog> findByProjectAndLevel(@Param("projectId") Long projectId,
                                               @Param("level") String level,
                                               @Param("since") LocalDateTime since);

    @Query("SELECT l FROM ApplicationLog l WHERE l.projectId = :projectId AND l.message LIKE %:keyword% ORDER BY l.createdAt DESC")
    List<ApplicationLog> findByProjectAndKeyword(@Param("projectId") Long projectId,
                                                 @Param("keyword") String keyword);

    @Query("SELECT l FROM ApplicationLog l WHERE l.projectId = :projectId AND l.level = :level AND l.message LIKE %:keyword% AND l.createdAt > :since ORDER BY l.createdAt DESC")
    List<ApplicationLog> findByProjectAndLevelAndKeyword(@Param("projectId") Long projectId,
                                                         @Param("level") String level,
                                                         @Param("keyword") String keyword,
                                                         @Param("since") LocalDateTime since);
}
