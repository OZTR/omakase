/**
 * ADD LICENSE
 */
package com.salesforce.omakase;

/**
 * Error messages.
 * 
 * @author nmcwilliams
 */
@SuppressWarnings("javadoc")
public enum Message {
    MISSING_PSEUDO_NAME("expected to find a valid pseudo element or class name ([-_0-9a-zA-Z], cannot start with a number)"),
    EXPECTED_VALID_ID("expected to find a valid id name ([-_0-9a-zA-Z], cannot start with a number)"),
    UNPARSABLE_SELECTOR("Unable to parse remaining selector content (Check that the selector is valid and is allowed here)"),
    UNPARSABLE_VALUE("Unable to parse remaining declaration value"),
    EXPECTED_VALUE("Expected to parse a property value!"),
    EXPECTED_TO_FIND("Expected to find '%s'"),
    EXPECTED_CLOSING("Expected to find closing '%s'"),
    INVALID_HEX("Expected a hex color of length 3 or 6, but found '%s'"),
    EXPECTED_DECIMAL("Expected to find decimal value"),
    NAME_SELECTORS_NOT_ALLOWED("universal or type selector not allowed here"),

    ;

    private String message;

    Message(String message) {
        this.message = message;
    }

    /**
     * Gets the error message. If the message contains parameters for {@link String#format(String, Object...)} , use
     * {@link #message(String...)} instead.
     */
    public String message() {
        return message;
    }

    /**
     * Gets the error message, passing in the given arguments to {@link String#format(String, Object...)}.
     * 
     * @param parameters
     *            Arguments to {@link String#format(String, Object...)}.
     * @return The formatted message.
     */
    public String message(Object... parameters) {
        return String.format(message, parameters);
    }
}