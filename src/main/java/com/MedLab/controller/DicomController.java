package com.MedLab.controller;

import lombok.Getter;
import org.apache.coyote.Response;
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
import java.util.Map;

@RestController
@RequestMapping("/api/dicom")
public class DicomController {


    //파일 업로드(POST)
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("id", 1);
            response.put("message","Upload Success");
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }

    }

    //변환 실행(POST)
    @PostMapping("/convert/{id}")
    public ResponseEntity<?> convertFile(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        response.put("id",id);
        response.put("status","PROCESSING");
        response.put("message","Conversion Started");
        return ResponseEntity.ok(response);
    }

    //전체 이력 조회 (GET)
    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        return ResponseEntity.ok("History List Placeholder");
    }

    //상세 조회(GET)
    @GetMapping("/history/{id}")
    public ResponseEntity<?> getDetail(@PathVariable int id) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("fileName", "test_image.dcm");
        response.put("status", "SUCCESS");

        //front-end 다운로드 버튼이 호출할 API 주소를 미리 알려줌.
        response.put("downloadUrl", "/api/dicom/download/" + id);
        return ResponseEntity.ok(response);
    }

    //파일 다운로드 엔드포인트
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable int id) {
        Path path = Paths.get("/storage/png/test_result.png");
        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachmentl filename=\"converted_image.png\"")
                .body(resource);
    }

}
