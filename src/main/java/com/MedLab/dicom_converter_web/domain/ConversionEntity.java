package com.MedLab.dicom_converter_web.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversion_info")
@Data

public class ConversionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 512)
    private String status; // PROCESSING, SUCCESS, FAIL
    private String pngPath; // 변환된 파일 경로
    private LocalDateTime conversionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dicom_id") // 외래키 설정. 외래키 컬럼명은 dicom_id이다.
    private DicomEntity dicom;
}
