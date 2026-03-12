package com.MedLab.dicom_converter_web.controller;
import com.MedLab.dicom_converter_web.domain.DicomEntity;
import com.MedLab.dicom_converter_web.repository.DicomRepository;
import com.MedLab.dicom_converter_web.service.DicomService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Response;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/dicom")
@RequiredArgsConstructor
public class DicomController {

    private final DicomService dicomService;
    private final DicomRepository dicomRepository;
    //파일 업로드(POST)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Long savedId = dicomService.uploadAndSaveDicom(file);

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedId);
            response.put("message","Upload Success");

            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }

    }

    //변환 실행(POST)
    @PostMapping("/convert/{id}")
    public ResponseEntity<?> convertFile(@PathVariable Long id) {
        try {
            dicomService.convertDicomToPng(id);

            Map<String, Object> response = new HashMap<>();
            response.put("id",id);
            response.put("status","SUCCESS");
            response.put("message","Conversion Started");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("FAILED" + e.getMessage());
        }
    }

    //전체 이력 조회 (GET)
    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        List<DicomEntity> history = dicomRepository.findAll();
        return ResponseEntity.ok(history);
    }

    //상세 조회(GET)
    @GetMapping("/history/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Long id) {
        return dicomRepository.findById(id)
                .map(ResponseEntity::ok)  //DB에 있으면 객체 그대로 반환
                .orElse(ResponseEntity.status(404).build());
    }

    //파일 다운로드 엔드포인트 .dcm대신 .png를 다운로드 하도록 수정
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            DicomEntity dicom = dicomRepository.findById(id)
                    .orElseThrow(()-> new RuntimeException("해당 ID의 파일을 찾을 수 없습니다." + id));


            String dcmPath = dicom.getFilePath();
            String pngPath = dcmPath.replaceAll("(?i)\\.dcm$", "") + ".png"; // 확장자 교체

            Path path = Paths.get(pngPath);
            Resource resource = new FileSystemResource(path);

            if (!resource.exists()) {
                return ResponseEntity.status(404).build();
            }

            String downloadFileName = dicom.getPatientName() + "_converted.png";

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG) // 타입을 png로 명시
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName +"\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }



    }

}
