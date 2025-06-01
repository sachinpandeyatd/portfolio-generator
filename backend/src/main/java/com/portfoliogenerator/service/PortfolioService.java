package com.portfoliogenerator.service;

import com.portfoliogenerator.dto.PortfolioResponse;
import com.portfoliogenerator.exception.FileStorageException;
import com.portfoliogenerator.exception.ResourceNotFoundException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
	private final GeminiService geminiService;

	public PortfolioService(@Value("${app.portfolio.storage-path}") String storagePath, @Value("${app.portfolio.base-url}") String baseUrl, IdGeneratorService idGeneratorService, GeminiService geminiService){
		this.portfolioStorageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
		this.appBaseUrl = baseUrl;
		this.idGeneratorService = idGeneratorService;
		this.geminiService = geminiService;

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

		boolean isPdf = originalFilename.endsWith(".pdf");
		boolean isDocx = originalFilename.endsWith(".docx");

		if (!isPdf && !isDocx) {
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
			if (isPdf) {
				System.out.println("Parsing PDF: " + originalFilename);
				try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
					PDFTextStripper pdfStripper = new PDFTextStripper();
					resumeText = pdfStripper.getText(document);
				}
			} else if (isDocx) {
				System.out.println("Parsing DOCX: " + originalFilename);
				try (XWPFDocument document = new XWPFDocument(inputStream);
					 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
					resumeText = extractor.getText();
				}
			} else {
				throw new FileStorageException("Unsupported file type for parsing: " + originalFilename);
			}
		}catch (Exception e){
			throw new FileStorageException("Failed to parse resume file: " + originalFilename, e);
		}

		System.out.println("---- Parsed Resume Text for " + portfolioId + " ----");
		System.out.println(resumeText.substring(0, Math.min(resumeText.length(), 500)) + "...");
		System.out.println("--------------------------------------");

		String generatedHtmlContent;

		try{
			System.out.println("PortfolioService: Calling GeminiService to generate HTML for ID: " + portfolioId);
			System.out.println("Resume Text======= \n" + resumeText);
			generatedHtmlContent = geminiService.genratePortfolioHtml(resumeText);
		}catch (IOException e){
			try {
				Files.deleteIfExists(portfolioDirectory.resolve(portfolioId + ".html"));
				Files.deleteIfExists(portfolioDirectory);
			}catch (IOException ex){
				System.err.println("Failed to cleanup directory " + portfolioDirectory + " after AI error: " + ex.getMessage());
			}
			throw new FileStorageException("Failed to generate portfolio HTML using AI: " + e.getMessage(), e);
		}

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

	private String escapeHtml(String text) {
		return text.replace("&", "&")
				.replace("<", "<")
				.replace(">", ">")
				.replace("\\", "")
                .replace("'", "'");
    }

	public Resource loadPortfolioFile(String portfolioId, String filename) {
		try{
			Path portfolioDir = this.portfolioStorageLocation.resolve(portfolioId).normalize();
			Path filePath = portfolioDir.resolve(filename).normalize();

			if(!filePath.startsWith(portfolioDir)){
				throw new FileStorageException("Can not access files outside of portfolio directory.");
			}

			Resource resource = new UrlResource((filePath.toUri()));

			if(resource.exists() && resource.isReadable()){
				return resource;
			}else {
				throw new ResourceNotFoundException("File not found or not readable: " + filename + " in portfolio " + portfolioId);
			}
		}catch (MalformedURLException e){
			throw new ResourceNotFoundException("File not found: " + filename + " --- "+ e);
		}
	}
}
