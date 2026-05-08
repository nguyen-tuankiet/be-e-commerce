package com.example.becommerce.repository;

import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByPhoneAndDeletedFalse(String phone);

    /** Login by email OR phone */
    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.phone = :identifier) AND u.deleted = false")
    Optional<User> findByEmailOrPhone(@Param("identifier") String identifier);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByPhoneAndDeletedFalse(String phone);

    Optional<User> findByIdAndDeletedFalse(Long id);

    /** Find next sequence number for generating code like USR-001 */
    @Query("SELECT COUNT(u) FROM User u")
    long countAll();

    Page<User> findByRoleAndStatusAndDistrictContainingIgnoreCaseAndFullNameContainingIgnoreCaseAndDeletedFalse(
            Role role, UserStatus status, String district, String keyword, Pageable pageable);
}
