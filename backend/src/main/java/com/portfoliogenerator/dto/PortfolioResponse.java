package com.portfoliogenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
	private String portfolioId;
	private String portfolioUrl;
	private String downloadUrl;
	private String message;
}
