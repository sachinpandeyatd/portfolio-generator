import axios from "axios";


const API_BASE_URL = process.local.frontend.env.API_BASE_URL;

export const uploadResume = async (file) => {
    const formData = new FormData();
    formData.append('resumeFile', file);

    try{
        const response = await axios.post(`${API_BASE_URL}/resume/upload`, formData{
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    }catch (error) {
        console.error("Error uploading resume: ", error.response ? error.response.data : error.message);
        throw error.response ? new Error(error.response.data.message || 'Failed to upload resume') : error;
    }
}