package aro.ki.pnj.service;

import aro.ki.pnj.model.ChainOfThoughtResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Chain of Thought responses from the AI model
 * Expected format:
 * <thinking>Internal thought process here...</thinking>
 * <response>Actual spoken response here...</response>
 */
@ApplicationScoped
public class ChainOfThoughtParser {

    private static final Logger log = LoggerFactory.getLogger(ChainOfThoughtParser.class);

    private static final Pattern THINKING_PATTERN = Pattern.compile(
        "<thinking>\\s*(.*?)\\s*</thinking>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern RESPONSE_PATTERN = Pattern.compile(
        "<response>\\s*(.*?)\\s*</response>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /**
     * Parse a CoT response from the AI
     * @param rawResponse The raw response from the AI model
     * @return Parsed thinking and response, or fallback if parsing fails
     */
    public ChainOfThoughtResponse parse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            log.warn("Empty or null response received");
            return ChainOfThoughtResponse.fallback("");
        }

        try {
            String thinking = extractTag(rawResponse, THINKING_PATTERN);
            String response = extractTag(rawResponse, RESPONSE_PATTERN);

            if (response == null || response.trim().isEmpty()) {
                log.warn("No <response> tag found in AI output. Using raw response as fallback.");
                return ChainOfThoughtResponse.fallback(rawResponse.trim());
            }

            log.debug("CoT parsed - Thinking: {} chars, Response: {} chars",
                thinking != null ? thinking.length() : 0,
                response.length());

            return new ChainOfThoughtResponse(thinking, response);

        } catch (Exception e) {
            log.error("Error parsing CoT response, using fallback: {}", e.getMessage());
            return ChainOfThoughtResponse.fallback(rawResponse.trim());
        }
    }

    private String extractTag(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Build a system message that instructs the AI to use CoT format
     */
    public String buildCoTInstructions() {
        return """

            IMPORTANT: Structure your response using Chain of Thought reasoning:
            1. First, write your internal thoughts in <thinking> tags
            2. Then, write your spoken response in <response> tags

            Example:
            <thinking>
            This adventurer seems friendly but I should remain cautious.
            They're asking about the old ruins - that could be dangerous.
            I'll share what I know but warn them of the risks.
            </thinking>
            <response>
            Ah, the old ruins you say? Aye, I know them well. Dark place it is,
            full of dangers. If you're set on going, take plenty of torches
            and watch your step.
            </response>

            Always use this format for ALL your responses.
            """;
    }
}
