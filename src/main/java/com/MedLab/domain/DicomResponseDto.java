package com.MedLab.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DicomResponseDto {
    private int id;
    private String fileName;
    private String status;
    private String message;

    public DicomResponseDto(int id, String fileName, String status, String message) {
        this.id = id;
        this.fileName = fileName;
        this.status = status;
        this.message = message;
    }
}
