package com.example.becommerce.repository;

import com.example.becommerce.entity.VerificationDocument;
import com.example.becommerce.entity.enums.DocType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, Long> {

    /**
     * Lấy tài liệu theo verification và type
     */
    Optional<VerificationDocument> findByVerificationIdAndDocType(Long verificationId, DocType docType);

    /**
     * Lấy danh sách tài liệu của verification
     */
    List<VerificationDocument> findByVerificationId(Long verificationId);

    /**
     * Kiểm tra đã có loại tài liệu này chưa
     */
    boolean existsByVerificationIdAndDocType(Long verificationId, DocType docType);
}
