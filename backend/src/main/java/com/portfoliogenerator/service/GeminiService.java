package com.portfoliogenerator.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeminiService {
	private final String projectId;
	private final String location;
	private final String modelName;

	public GeminiService(
			@Value("${gemini.project-id}") String projectId,
			@Value("${gemini.location}") String location,
			@Value("${gemini.model-name}") String modelName) {
		this.projectId = projectId;
		this.location = location;
		this.modelName = modelName;
	}

	public String genratePortfolioHtml(String resumeText) throws IOException{
		try(VertexAI vertexAI = new VertexAI(projectId, location)){
			GenerativeModel model = new GenerativeModel(modelName, vertexAI);

			String prompt = String.format(
							"You are an expert web developer. Based on the following resume text, generate a complete, single, self-contained HTML file for a personal portfolio website. " +
							"The HTML file must include all CSS within `<style>` tags and all JavaScript (if any, but prefer minimal or no JS for simplicity) within `<script>` tags. " +
							"Do NOT use any external CDN links for libraries like Bootstrap, jQuery, or font providers. All resources must be inline. " +
							"The portfolio should look modern and professional. Extract key information like name, contact details, summary/objective, skills, experience, education, and projects (if available) from the resume text and display them in well-structured sections. " +
							"Make the design responsive. If no specific name is found, use 'User Portfolio'. " +
							"Output ONLY the HTML code starting with `<!DOCTYPE html>` and ending with `</html>`. Do not include any explanatory text before or after the HTML code itself.\n\n" +
							"Resume Text:\n" +
							"```\n" +
							"%s\n" +
							"```\n\n" +
							"Generate the HTML code now:",
					resumeText
			);

			System.out.println("----Sending the prompt to Gemini----");
			System.out.println(prompt);
			System.out.println("----End of Prompt-----");


			GenerateContentResponse response;

			try {
				response = model.generateContent(prompt);
			}catch (Exception e){
				System.out.println("Error calling the Gemini API: " + e.getMessage());
				e.printStackTrace();
				throw new IOException("Failed to generate content from Gemini: " + e.getMessage(), e);
			}

			if (response == null || response.getCandidatesList().isEmpty()){
				System.err.println("Gemini response was null or empty.");
				throw new IOException("Received no response or empty candidates list from Gemini.");
			}

			String generatedHtml = ResponseHandler.getText(response);

			if (generatedHtml == null || !generatedHtml.trim().toLowerCase().startsWith("<!doctype html>")) {
				System.err.println("Gemini did not return valid HTML. Response received:\n" + generatedHtml);
				throw new IOException("Gemini did not return valid HTML. Check logs for the response.");
			}

			System.out.println("--- Received HTML from Gemini (first 200 chars) ---");
			System.out.println(generatedHtml.substring(0, Math.min(generatedHtml.length(), 200)) + "...");
			System.out.println("--- End of Gemini HTML ---");

			return generatedHtml;
		}
	}
}
