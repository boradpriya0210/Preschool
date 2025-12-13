package com.preschool.preschool.repository;

import com.preschool.preschool.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByClassName(String className);

    List<Announcement> findAllByOrderByDateDesc();

    List<Announcement> findByClassNameOrderByDateDesc(String className);
}
