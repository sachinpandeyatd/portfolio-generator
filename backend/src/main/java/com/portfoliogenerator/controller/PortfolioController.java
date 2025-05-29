package com.portfoliogenerator.controller;

import com.portfoliogenerator.dto.PortfolioResponse;
import com.portfoliogenerator.exception.FileStorageException;
import com.portfoliogenerator.service.PortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PortfolioController {
	private final PortfolioService portfolioService;

	public PortfolioController (PortfolioService portfolioService){
		this.portfolioService = portfolioService;
	}

	@PostMapping("/api/v1/resume/upload")
	public ResponseEntity<PortfolioResponse> uploadResumeAndGeneratePortfolio(@RequestParam("resumeFile")MultipartFile file){
		try{
			PortfolioResponse response = portfolioService.processResumeUpload(file);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException | FileStorageException e){
			System.out.println("Exception occurred: " + e.getMessage());
			return ResponseEntity.badRequest().body(new PortfolioResponse(null, null, null, e.getMessage()));
		}catch (Exception e){
			System.out.println("Unexpected error during upload: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PortfolioResponse(null, null, null, "An unexpected error occurred. Please retry."));
		}
	}

}
