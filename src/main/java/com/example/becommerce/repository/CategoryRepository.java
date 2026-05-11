package com.example.becommerce.repository;

import com.example.becommerce.entity.Category;
import com.example.becommerce.entity.enums.VisibilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByDeletedFalseOrderByPriorityDescCreatedAtDesc();

    List<Category> findByStatusAndDeletedFalseOrderByPriorityDescCreatedAtDesc(VisibilityStatus status);

    Optional<Category> findByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    long countByDeletedFalse();
}
