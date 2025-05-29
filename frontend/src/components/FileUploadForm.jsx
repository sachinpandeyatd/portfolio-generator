import React, {useState} from "react";

const FileUploadForm = ({onUploadSuccess, onUploadStart, onUploadError}) => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [fileError, setFileError] = useState('');

    const handleFileChange = (event) => {
        const file = event.target.files[0];

        if(file){
            if(file.type === "application/pdf" || file.type === "application/vnd.openxmlformats-officedocument.wordprocessingml.document"){
                setSelectedFile(file);
                setFileError('');
            }else{
                setSelectedFile(null);
                setFileError('Invalid file type. Please upload a PDF or DOCX file.');
            }
        }
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if(!selectedFile){
            setFileError('Please select a file to upload.');
            return;
        }

        onUploadStart();
        setFileError('');

        try{
            onUploadSuccess(selectedFile);
        }catch(error){
            const errorMessage = error.message || 'An unexpected error occured.';
            setFileError(errorMessage);
            onUploadError(errorMessage);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="upload-form">
            <h2>Upload Your Resume</h2>
            <p>Upload your resume (PDF or DOCX) to generate your personal portfolio website.</p>

            <div>
                <input id="resumeFile" type="file" accept=".pdf, .docx" onChange={handleFileChange} />
                <label htmlFor="resumeFile" className="file-input-label">
                    {selectedFile ? selectedFile.name : "Choose File"}
                </label>
            </div>

            {fileError && <p className="error-message">{fileError}</p>}
            <button type="submit" disabled={!selectedFile}>Generate Portfolio</button>
        </form>
    );
};

export default FileUploadForm;