package com.portfoliogenerator.controller;

import com.portfoliogenerator.dto.PortfolioResponse;
import com.portfoliogenerator.exception.FileStorageException;
import com.portfoliogenerator.exception.ResourceNotFoundException;
import com.portfoliogenerator.service.PortfolioService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

	@GetMapping("/{portfolioId}")
	public ResponseEntity<String> viewPortfolio(@PathVariable String portfolioId){
		try{
			Resource resource = portfolioService.loadPortfolioFile(portfolioId, portfolioId + ".html");
			String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(content);
		}catch (ResourceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found", e);
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read portfolio file", e);
		}
	}
}
