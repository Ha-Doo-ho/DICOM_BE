package com.MedLab.controller;

import lombok.Getter;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dicom")
public class DicomController {
//

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

    @GetMapping("/history/{id}")
    public ResponseEntity<?> getDetail(@PathVariable int id) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("fileName", "test_image.dcm");
        response.put("status", "SUCCESS");
        return ResponseEntity.ok(response);
    }

}
