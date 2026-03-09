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
}