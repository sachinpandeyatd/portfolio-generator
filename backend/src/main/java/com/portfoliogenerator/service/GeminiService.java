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
					"You are an expert web developer and UI/UX designer tasked with creating a visually engaging and professional personal portfolio website from resume text. " +
							"Your output MUST be a single, complete, self-contained HTML file. " +
							"ALL CSS must be embedded within `<style>` tags in the `<head>`. " +
							"ALL JavaScript (if any is absolutely essential, otherwise avoid it for simplicity) must be embedded within `<script>` tags, ideally before the closing `</body>` tag. " +
							"Absolutely NO external file links, NO CDN links for CSS frameworks, JavaScript libraries, icon packs, or web fonts. Use only web-safe fonts (e.g., Arial, Helvetica, Verdana, 'Segoe UI', sans-serif).\n\n" +

							"Overall Visual Style:\n" +
							"*   **Main Background:** Instead of a plain white or single color, consider a very subtle linear gradient for the `body` background (e.g., from a very light gray like `#f8f9fa` to a slightly different light gray like `#e9ecef`, or two very close light pastel shades). Alternatively, use a solid, very light off-white or light gray that isn't stark white for the main page background to provide a softer feel.\n" +
							"*   **Header Background:** The header section (containing name, title, contact info) should have a distinct and more prominent background. A solid, professional color (like a deep blue, teal, or charcoal gray as seen in many modern resumes) or a subtle gradient within such a color would be effective. Ensure high contrast for text on this header background.\n" +
							"*   **Section Distinction:** Consider visually separating main content sections (Summary, Skills, Experience, etc.). This could be achieved by: \n" +
							"    *   Giving each main section a 'card' appearance with a white or very light background, subtle `box-shadow`, and rounded corners, placed on top of the main body background.\n" +
							"    *   Or, alternating very light background colors for consecutive sections (e.g., Section 1 on white, Section 2 on `#f7f7f7`, Section 3 on white).\n" +
							"*   **Accent Color:** Choose one or two accent colors (e.g., a vibrant blue, green, or orange) and use them consistently for links, section heading underlines, skill tags, or important highlights. This should complement the header color if different.\n" +
							"*   **Shadows & Depth:** Use subtle `box-shadow` on 'card' elements or containers to create a sense of depth and lift them off the page. Avoid overly strong or dark shadows.\n\n" +

							"Structure the portfolio with the following sections, if information is available in the resume:\n" +
							"1.  **Header/Hero Section:** (As described above for background) Prominently display Full Name (large font) and Professional Title. Include Location, Phone, Email, LinkedIn, GitHub links (using text/Unicode characters like (L), (GH) or simple text labels if icons are too complex to embed, ensure `mailto:` and `target='_blank'` for links).\n" +
							"2.  **Summary/About Me:** Clear, readable paragraph.\n" +
							"3.  **Skills:** If categorized, present them under these categories. Display individual skills as styled 'pills' or 'tags' (rounded borders, light background color, padding, perhaps the accent color for text or border). Arrange these tags neatly, possibly allowing them to wrap if there are many.\n" +
							"4.  **Experience:** For each role: Job Title, Company Name & Location, Dates. Use bullet points for responsibilities/achievements. Consider each experience item as a 'card' or visually distinct block.\n" +
							"5.  **Projects:** For each project: Project Name, Description. List technologies if mentioned. Similar 'card' or block styling as Experience items.\n" +
							"6.  **Education:** For each qualification: Degree, Institution, Dates. Clear and concise presentation.\n\n" +

							"CSS Best Practices:\n" +
							"*   Ensure the design is fully responsive using techniques like flexbox, grid, and media queries.\n" +
							"*   Use clear typography with good contrast. Define base font size, heading sizes (h1, h2, h3), and line heights.\n" +
							"*   Use padding and margins generously for a clean, uncluttered, and breathable layout, especially within and around sections and cards.\n" +
							"*   Section headings should be prominent (larger font, accent color, perhaps a bottom border or underline with the accent color).\n\n" +

							"Input Resume Text:\n" +
							"```\n" +
							"%s\n" +
							"```\n\n" +

							"CRITICAL REMINDER: Output ONLY the HTML code starting with `<!DOCTYPE html>` and ending with `</html>`. " +
							"Do NOT include any surrounding text, explanations, comments outside the HTML structure, or markdown code block markers like ```html or ``` before or after the HTML code itself. The entire output must be parseable as a valid HTML document.",
					resumeText
			);

			System.out.println("----Sending the prompt to Gemini----");
			System.out.println(prompt);
			System.out.println("----End of Prompt-----");


			GenerateContentResponse response;
			try {
				response = model.generateContent(prompt);
			} catch (Exception e) {
				System.err.println("Error calling Gemini API: " + e.getMessage());
				e.printStackTrace();
				throw new IOException("Failed to generate content from Gemini: " + e.getMessage(), e);
			}

			if (response == null || response.getCandidatesList().isEmpty()) {
				System.err.println("Gemini response was null or empty.");
				throw new IOException("Received no response or empty candidates list from Gemini.");
			}

			String rawGeneratedText = ResponseHandler.getText(response);

			System.out.println("--- Raw response from Gemini ---");
			System.out.println(rawGeneratedText.substring(0, Math.min(rawGeneratedText.length(), 500)) + "..."); // Log raw response
			System.out.println("--- End of raw Gemini response ---");

			String cleanedHtml = cleanGeminiResponse(rawGeneratedText);

			if (cleanedHtml == null || !cleanedHtml.trim().toLowerCase().startsWith("<!doctype html>")) {
				System.err.println("Gemini did not return valid HTML after cleaning. Cleaned response preview:\n" +
						(cleanedHtml != null ? cleanedHtml.substring(0, Math.min(cleanedHtml.length(), 200)) + "..." : "null"));
				throw new IOException("Gemini did not return valid HTML after cleaning. Check logs for the raw and cleaned response.");
			}

			System.out.println("--- Cleaned HTML from Gemini (first 200 chars) ---");
			System.out.println(cleanedHtml.substring(0, Math.min(cleanedHtml.length(), 200)) + "...");
			System.out.println("--- End of Cleaned Gemini HTML ---");

			return cleanedHtml;
		}
	}


	private String cleanGeminiResponse(String rawText) {
		if (rawText == null) {
			return null;
		}
		String text = rawText.trim();

		if (text.startsWith("```html") && text.endsWith("```")) {
			text = text.substring("```html".length(), text.length() - "```".length()).trim();
		} else if (text.startsWith("```") && text.endsWith("```")) {
			int firstNewline = text.indexOf('\n');
			if (firstNewline != -1 && firstNewline < 15) {
				text = text.substring(firstNewline + 1, text.length() - "```".length()).trim();
			} else {
				text = text.substring("```".length(), text.length() - "```".length()).trim();
			}
		}
		return text;
	}
}
