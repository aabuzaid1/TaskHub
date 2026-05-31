package util;

public class ValidationUtil {

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean isValidLength(String text, int minLength) {
        if (text == null) return false;
        return text.trim().length() >= minLength;
    }

    public static boolean validateUserInputs(String username, String password) {
        return !isEmpty(username) && !isEmpty(password) && username.length() >= 3 && password.length() >= 4;
    }
}
