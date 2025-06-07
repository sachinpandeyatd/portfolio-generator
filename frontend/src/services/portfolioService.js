import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const AUTH_API_KEY = import.meta.env.VITE_BACKEND_API_KEY;
console.log(AUTH_API_KEY);

export const uploadResume = async (file) => {
    const formData = new FormData();
    formData.append('resumeFile', file);

    try{
        const response = await axios.post(`${API_BASE_URL}/resume/upload`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
                'X-API-Key': AUTH_API_KEY
            },
        });
        return response.data;
    }catch (error) {
        console.error("Error uploading resume: ", error.response ? error.response.data : error.message);
        throw error.response ? new Error(error.response.data.message || 'Failed to upload resume') : error;
    }
}