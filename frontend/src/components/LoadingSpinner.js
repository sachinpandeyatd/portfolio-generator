import React from "react";
import './LoadingSpinner.css';

const LoadingSpinner = () => {
    return (
        <div className="spinner-container">
            <div className="loading-spinner"></div>
            <p>Generating your portfolio...please wait a while</p>
        </div>
    )
}

export default LoadingSpinner;