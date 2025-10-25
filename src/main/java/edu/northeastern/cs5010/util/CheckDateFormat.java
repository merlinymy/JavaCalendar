package edu.northeastern.cs5010.util;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CheckDateFormat {
  public static boolean useRegex(final String input) {
    // Compile regular expression
    final Pattern pattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}", Pattern.CASE_INSENSITIVE);
    // Match regex against input
    final Matcher matcher = pattern.matcher(input);
    // Use results...
    return matcher.matches();
  }
}
