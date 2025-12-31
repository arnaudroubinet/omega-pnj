package aro.ki.pnj.model;

/**
 * Represents a parsed Chain of Thought response from the AI
 */
public class ChainOfThoughtResponse {

    public final String thinking;  // Internal thought process
    public final String response;  // Actual spoken response

    public ChainOfThoughtResponse(String thinking, String response) {
        this.thinking = thinking;
        this.response = response;
    }

    public static ChainOfThoughtResponse fallback(String rawResponse) {
        // If parsing fails, use the raw response as both
        return new ChainOfThoughtResponse(null, rawResponse);
    }
}
