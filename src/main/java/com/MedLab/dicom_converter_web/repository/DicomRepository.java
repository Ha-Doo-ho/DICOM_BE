package com.MedLab.dicom_converter_web.repository;

import com.MedLab.dicom_converter_web.domain.DicomEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DicomRepository extends JpaRepository<DicomEntity, Long> {
}

