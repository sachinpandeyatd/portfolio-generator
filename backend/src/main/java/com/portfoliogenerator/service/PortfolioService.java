package com.portfoliogenerator.service;

import com.portfoliogenerator.dto.PortfolioResponse;
import com.portfoliogenerator.exception.FileStorageException;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Service
public class PortfolioService {

	private final Path portfolioStorageLocation;
	private final IdGeneratorService idGeneratorService;
	private final String appBaseUrl;
	private final Tika tika;

	public PortfolioService(@Value("${app.portfolio.storage-path}") String storagePath, @Value("${app.portfolio.base-url}") String baseUrl, IdGeneratorService idGeneratorService){
		this.portfolioStorageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
		this.appBaseUrl = baseUrl;
		this.idGeneratorService = idGeneratorService;
		this.tika = new Tika();

		try{
			Files.createDirectories(this.portfolioStorageLocation);
		}catch (Exception e){
			throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", e);
		}
	}

	public PortfolioResponse processResumeUpload(MultipartFile file) {
		String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

		if (originalFilename.contains("..")) {
			throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFilename);
		}
		if (!originalFilename.toLowerCase().endsWith(".pdf") && !originalFilename.toLowerCase().endsWith(".docx")) {
			throw new FileStorageException("Invalid file type. Only PDF and DOCX are allowed. Received: " + originalFilename);
		}

		String portfolioId;
		Path portfolioDirectory;

		do{
			portfolioId = idGeneratorService.generateUniqueId();
			portfolioDirectory = this.portfolioStorageLocation.resolve(portfolioId);
		}while (Files.exists(portfolioDirectory));

		try {
				Files.createDirectories(portfolioDirectory);
			}catch (IOException e){
				throw new FileStorageException("Could not create directory for portfolio ID: " + portfolioId, e);
			}

		String resumeText;
		try(InputStream inputStream = file.getInputStream()){
			resumeText = tika.parseToString(inputStream);
		}catch (Exception e){
			throw new FileStorageException("Failed to parse resume file: " + originalFilename, e);
		}

		System.out.println("---- Parsed Resume Text for " + portfolioId + " ----");
		System.out.println(resumeText.substring(0, Math.min(resumeText.length(), 500)) + "..."); // Print first 500 chars
		System.out.println("--------------------------------------");

		String generatedHtmlContent = generateMockPortfolioHtml(portfolioId, resumeText);

		Path targetLocation = portfolioDirectory.resolve(portfolioId + ".html");

		try{
			Files.writeString(targetLocation, generatedHtmlContent, StandardCharsets.UTF_8);
		}catch (Exception ex){
			try {
				Files.deleteIfExists(targetLocation);
				Files.deleteIfExists(portfolioDirectory);
			} catch (IOException e) {
				System.err.println("Failed to cleanup directory " + portfolioDirectory + ": " + e.getMessage());
			}
			throw new FileStorageException("Could not store generated HTML file " + portfolioId + ".html. Please try again!", ex);
		}

		String portfolioUrl = appBaseUrl + "/" + portfolioId;
		String downloadUrl = appBaseUrl + "/api/v1/portfolios/" + portfolioId + "/download";

		return new PortfolioResponse(portfolioId, portfolioUrl, downloadUrl, "Portfolio generated successfully.");
	}

	private String generateMockPortfolioHtml(String portfolioId, String resumeContent) {
		String name = "User";
		if (resumeContent.toLowerCase().contains("name:")) {
			try {
				name = resumeContent.substring(resumeContent.toLowerCase().indexOf("name:") + 5).split("\n")[0].trim();
			} catch (Exception ignored) {}
		}


		return "<!DOCTYPE html>\n" +
				"<html lang=\"en\">\n" +
				"<head>\n" +
				"    <meta charset=\"UTF-8\">\n" +
				"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
				"    <title>Portfolio - " + name + " (" + portfolioId + ")</title>\n" +
				"    <style>\n" +
				"        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; color: #333; }\n" +
				"        .container { max-width: 900px; margin: 20px auto; padding: 20px; background-color: #fff; box-shadow: 0 0 10px rgba(0,0,0,0.1); }\n" +
				"        header { background-color: #333; color: #fff; padding: 1em 0; text-align: center; }\n" +
				"        header h1 { margin: 0; }\n" +
				"        .content { padding: 20px; }\n" +
				"        h2 { color: #333; border-bottom: 2px solid #333; padding-bottom: 5px; }\n" +
				"        pre { background-color: #eee; padding: 10px; border-radius: 5px; white-space: pre-wrap; word-wrap: break-word; max-height: 400px; overflow-y: auto;}\n" +
				"        footer { text-align: center; margin-top: 20px; padding: 10px; font-size: 0.9em; color: #777; }\n" +
				"    </style>\n" +
				"</head>\n" +
				"<body>\n" +
				"    <header>\n" +
				"        <h1>Portfolio for " + name + "</h1>\n" +
				"        <p>ID: " + portfolioId + "</p>\n" +
				"    </header>\n" +
				"    <div class=\"container\">\n" +
				"        <div class=\"content\">\n" +
				"            <h2>Resume Data (Parsed)</h2>\n" +
				"            <p>This is a mock portfolio. The following is the text extracted from your resume:</p>\n" +
				"            <pre>" + escapeHtml(resumeContent) + "</pre>\n" +
				"            <h2>Next Steps</h2>\n" +
				"            <p>Integrate with a real AI (like Gemini) to generate a beautiful, structured portfolio based on this data.</p>\n" +
				"        </div>\n" +
				"    </div>\n" +
				"    <footer>\n" +
				"        <p>Generated by AI Portfolio Builder © " + java.time.Year.now().getValue() + "</p>\n" +
				"    </footer>\n" +
				"    <script>\n" +
				"       console.log('Portfolio ID: " + portfolioId + " loaded.');\n" +
				"       // Add any self-contained JS here if needed by the AI\n" +
				"    </script>\n" +
				"</body>\n" +
				"</html>";
	}

	private String escapeHtml(String text) {
		return text.replace("&", "&")
				.replace("<", "<")
				.replace(">", ">")
				.replace("\\", "")
                .replace("'", "'");
    }
}
