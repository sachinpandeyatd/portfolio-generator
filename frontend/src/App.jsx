import React, { useState } from 'react';
import FileUploadForm from './components/FileUploadForm';
import LoadingSpinner from './components/LoadingSpinner';
import PortfolioLinks from './components/PortfolioLinks';
import {uploadResume} from './services/portfolioService'
import './App.css'

function App() {
  const [isLoading, setIsLoading] = useState(false);
  const [portfolioData, setPortfolioData] = useState(null);
  const [error, setError] = useState('');

  const handleUploadStart = () => {
    setIsLoading(true);
    setError('');
    setPortfolioData(null);
  };

  const handleUploadSuccess = async (file) => {
    try{
      const data = await uploadResume(file);
      setPortfolioData(data);
    }catch(error){
      setError(error.message || 'Failed to generate portfolio. Please try again.');
      setPortfolioData(null);
    }finally{
      setIsLoading(false);
    }
  };

  const handleUploadError = (errorMessage) => {
    setError(errorMessage);
    setIsLoading(false);
    setPortfolioData(null);
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>AI Portfolio Generator</h1>
      </header>
      <main>
        {!isLoading && !portfolioData && (
          <FileUploadForm onUploadStart={handleUploadStart} onUploadSuccess={handleUploadSuccess} onUploadError={handleUploadError} />
        )}

        {isLoading && <LoadingSpinner />}
        {error && <p className="error-message main-error">{error}</p>}
        {!isLoading && portfolioData && (
          <PortfolioLinks portfolioUrl={portfolioData.portfolioUrl}
            downloadUrl={portfolioData.downloadUrl} portfolioId={portfolioData.portfolioId} />
        )}
      </main>
      <footer><p>&copy; {new Date().getFullYear()} Portfolio Generator</p></footer>
    </div>
  );
}

export default App;
