# 1. 자바 21 환경 (가벼운 리눅스 기반)
FROM openjdk:21-slim

# 2. 파이썬 3 및 라이브러리 설치 (하이브리드 엔진용)
RUN apt-get update && apt-get install -y python3 python3-pip
RUN pip3 install pydicom Pillow numpy --break-system-packages

# 3. 작업 폴더 설정
WORKDIR /app

# 4. 방금 빌드한 JAR 파일을 컨테이너 안으로 복사
# (파일 이름이 다르면 실제 이름으로 수정하거나 *.jar를 사용하세요)
COPY build/libs/dicom-converter-web-0.0.1-SNAPSHOT.jar app.jar

# 5. 파이썬 스크립트와 저장소 폴더 복사
COPY scripts/ /app/scripts/
RUN mkdir /app/storage

# 6. 서버 실행
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]