package com.MedLab.dicom_converter_web.service;

import com.MedLab.dicom_converter_web.domain.DicomEntity;
import com.MedLab.dicom_converter_web.repository.DicomRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DicomService {
    private final DicomRepository dicomRepository; // Constructor Dependency Injection

   @Value("${dicom.python.executable}")
   private String pythonExe;

   @Value("${dicom.python.script-path}")
   private String pythonScriptPath;

   @Value("${dicom.storage.location}")
   private String uploadDir;

    // 파일을 저장하고 DB에 기록
    @Transactional // @Transactional이 붙은 메서드 내 모든 작업은 성공 시 commit, 예외 발생 시 rollback 처리되어, 데이터가 일부만 반영되는 것을 방지합니다.
    public Long uploadAndSaveDicom(MultipartFile file) throws IOException {
        String projectRoot = System.getProperty("user.dir");
        Path storageFolder = Paths.get(projectRoot, uploadDir).toAbsolutePath();

        File targetDir = storageFolder.toFile();
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String savedName = String.valueOf(UUID.randomUUID()) + "_" + originalFilename;
        Path savePath = storageFolder.resolve(savedName); // + 또는 , 가 있는데 +는 둘을 붙여버리고 , 는 OS에 맞는 구분자를 넣어준다.

        file.transferTo(savePath.toFile());

        System.out.println("실제 저장 위치: " + savePath.toString());

        // 초기값 설정 (DICOM이 아닐 경우 대비)
        String pName = "Unknown", pId ="Unknown", sDate = "", mod = "", sUid = "";

        // DICOM 데이터 추출
        try (DicomInputStream dis = new DicomInputStream(savePath.toFile())) {
            Attributes attributes = dis.readDataset(-1,-1);
            pName = attributes.getString(Tag.PatientName, "Unknown");
            pId = attributes.getString(Tag.PatientID, "Unknown");
            sDate = attributes.getString(Tag.StudyDate, "");
            mod = attributes.getString(Tag.Modality, "");
            sUid = attributes.getString(Tag.StudyInstanceUID, "");
        } catch (Exception e) {
            System.out.println("DICOM 파일이 아니거나 정보를 읽을 수 없음" + e.getMessage());
        }




        //DicomEntity 객체 생성 및 save() 호출
        DicomEntity dicomEntity = new DicomEntity();
        dicomEntity.setFilePath(savePath.toString());


        dicomEntity.setPatientName(pName);
        dicomEntity.setPatientId(pId);
        dicomEntity.setStudyDate(sDate);
        dicomEntity.setModality(mod);
        dicomEntity.setStudyUid(sUid);
        dicomEntity.setUploadDate(LocalDateTime.now()); // 현재 시간 저장

        // JPA 의 save는 저장된 후 DB의 ID가 채워진 객체를 반환
        DicomEntity savedEntity = dicomRepository.save(dicomEntity);

        // 실제 DB에 저장된 ID 반환
        return savedEntity.getId();
    }

    @Transactional
    public void convertDicomToPng(Long id) throws Exception {
        // 1. DB에서 파일 경로 가져오기
        DicomEntity entity = dicomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 파일을 찾을 수 없습니다. ID: " + id));
        String dicomPath = entity.getFilePath();
        String pngPath = dicomPath.replaceAll("(?i)\\.dcm$","") + ".png";
        File outputPngFile = new File(pngPath);

        try{
            tryJavaConversion(dicomPath, pngPath);
            log.info("자바 엔진 변환 성공: {}", id);
        } catch (Throwable e) {
            log.warn("자바 엔진 실패({}). 파이썬 엔진을 가동합니다.", e.getMessage());

            try {
                runPythonConversion(dicomPath, pngPath);
                log.info("파이썬 엔진 변환 성공: ID {}", id);
            } catch (Exception pyEx) {
                log.error("최종 변환 실패: {}", pyEx.getMessage());
                entity.setConversionStatus("FAILED");
                dicomRepository.save(entity);
                return;
            }
        }

        // 성공 시 DB 업데이트
        entity.setPngPath("/images/" + outputPngFile.getName()); // 브라우저 접근 경로 저장
        entity.setConversionStatus("SUCCESS"); // 변환 상태 성공 기록
        entity.setConvertedAt(LocalDateTime.now()); // 변환 시간 기록
        dicomRepository.save(entity);
    }

    private void tryJavaConversion(String dicomPath, String pngPath) throws Exception {
        ImageIO.scanForPlugins();
        nu.pattern.OpenCV.loadShared();

        File dicomFile = new File(dicomPath);

        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(dicomFile)){
            Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
            if (!iter.hasNext()) {
                throw new RuntimeException("DICOM Reader 없음");
            }

            ImageReader reader = iter.next();
            reader.setInput(imageInputStream,false);
            BufferedImage bufferedImage = reader.read(0);

            if (!ImageIO.write(bufferedImage,"png",new File(pngPath))) {
                throw new RuntimeException("PNG 쓰기 실패 ");
            }
            reader.dispose();
        }
    }
    private void runPythonConversion(String input, String output) throws Exception {

        // 동적 절대 경로 주입 (경로 불일치 해결)
        String projectRoot = System.getProperty("user.dir");
        // 메모리 상에 특정 파일이나 폴더의 **'주소(경로)를 가리키는 객체'**를 만드는 과정
        // 새로운 파일 / 폴더를 만드는게 아님!!!
        File scriptFile = new File(projectRoot, pythonScriptPath);

        if(!scriptFile.exists()) {
            throw new RuntimeException("파이썬 script 파일을 찾을 수 없음. 확인된 경로" + scriptFile.getAbsolutePath());
        }

        //ProcessBuilder는 단순히 실행만 하는 게 아니라, 파이썬이 일을 잘하고 있는지 **종료 코드(Exit Code)**를 확인하거나
        // 파이썬이 뱉는 에러 메시지를 자바가 실시간으로 읽어올 수 있게 해줍니다.
        ProcessBuilder pb = new ProcessBuilder(
                pythonExe,
                scriptFile.getAbsolutePath(),
                input,
                output
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(),"MS949"))){
            String line;
            while ((line = br.readLine()) != null) {
                log.info("[Python Log] {}", line); 
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) throw new RuntimeException("파이썬 프로세스 에러(Code: " + exitCode + ")");
    }
}
