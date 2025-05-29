import React from "react";

const PortfolioLinks = ({portfolioUrl, downloadUrl, portfolioId}) => {
    if(!portfolioUrl || !downloadUrl){
        return null;
    }

    return (
        <div className="portfolio-links">
            <h3>Hurray! Your Portfolio is Ready.</h3>
            <p>
                <strong>View your portfolio:</strong>{' '}
                <a href={portfolioUrl} target="_blank" rel="noopner noreferrer">
                    {portfolioUrl}
                </a>
            </p>
            <p>
                <strong>Download HTML:</strong>{'  '}
                <a href={downloadUrl} download={`${portfolioId.html}`}>Download {portfolioId}.HTML</a>
            </p>
            <p className="note">
                The "View" link will take you to your publicly accessible portfolio page.
                The "Download" button lets you save the self-contained HTML file.
            </p>
        </div>
    );
};

export default PortfolioLinks;