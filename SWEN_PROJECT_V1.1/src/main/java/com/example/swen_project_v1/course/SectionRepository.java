package com.example.swen_project_v1.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByCourseId(Long courseId);

    Optional<Section> findByCrn(String crn);

    boolean existsByCrn(String crn);

    @Query("SELECT s FROM Section s JOIN s.days d WHERE d = :day AND s.room = :room")
    List<Section> findByDaysContainingAndRoom(@Param("day") DayOfWeek day, @Param("room") String room);

    @Query("SELECT s FROM Section s JOIN s.days d WHERE d = :day AND s.professor = :professor")
    List<Section> findByDaysContainingAndProfessor(@Param("day") DayOfWeek day, @Param("professor") String professor);

    @Query("SELECT DISTINCT s FROM Section s JOIN s.course c LEFT JOIN s.days d " +
            "WHERE (:query IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:levelPrefix IS NULL OR c.code LIKE CONCAT('%', :levelPrefix, '__')) " +
            "AND (:professor IS NULL OR LOWER(s.professor) LIKE LOWER(CONCAT('%', :professor, '%'))) " +
            "AND (:mode IS NULL OR s.deliveryMode = :mode) " +
            "AND (:day IS NULL OR d = :day)")
    List<Section> searchCatalog(
            @Param("query") String query,
            @Param("levelPrefix") String levelPrefix,
            @Param("professor") String professor,
            @Param("mode") DeliveryMode mode,
            @Param("day") DayOfWeek day);
}