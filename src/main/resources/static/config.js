export const BASE_URL = ""; //주소 입력

export const API_endpoints = {
    UPLOAD: `${BASE_URL}/api/dicom/upload`,
    HISTORY: `${BASE_URL}/api/dicom/history`,

    CONVERT: (id) => `${BASE_URL}/api/dicom/convert/${id}`,
    DOWNLOAD: (id) => `${BASE_URL}/api/dicom/download/${id}`,
    HISTORY_DETAIL: (id) => `${BASE_URL}/api/dicom/history/${id}`
};