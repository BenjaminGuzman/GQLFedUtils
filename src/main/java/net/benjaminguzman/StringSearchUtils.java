package net.benjaminguzman;

import org.jetbrains.annotations.NotNull;

/**
 * CLASS NOT USED
 * <p>
 * indexOf is more efficient
 * <p>
 * https://stackoverflow.com/questions/9741188/java-indexof-function-more-efficient-than-rabin-karp-search-efficiency
 * -of-text
 */
public class StringSearchUtils {
	public static int rabinKarp(@NotNull String str, @NotNull String pattern) {
		int patternLen = pattern.length(); // = |pattern| = n
		if (pattern.length() > str.length() || patternLen == 0)
			return -1;

		int patternHash = pattern.hashCode();
		int windowHash = str.substring(0, pattern.length()).hashCode();

		int pow31NMinus1 = pow31(patternLen - 1);

		// this index is exclusive, ie the last window starts at endWindowIdx - 1 and ends at content.length()
		int endWindowIdx = str.length() - patternLen;
		for (int i = 0; i < endWindowIdx; ++i) {
			// if both hashes match, check string equality between the window and the pattern
			if (windowHash == patternHash && str.substring(i, i + patternLen).equals(pattern)) {
				// at this moment the window hash comes from the substring s[i...i+n-1]
				// so the match starts at i
				return i;
			}

			// compute the hash for the next window, ie s[i+1...i+n-1 + 1] = s[i+1...i+n]

			// Note: 31 is the Java's default constant for computing hash codes
			// if h_i is the current hash, h = s[i] * 31^(n-1) + s[i+1] * 31^(n-2) + ... + s[i+n-1]*31^0
			// if h_i+1 is the next hash, h_i+1 = s[i+1] * 31^(n-1) + s[i+2] * 31^(n-2) + ... + s[i+n]*31^0
			// then the following relation holds:
			// h_i+1 = (h_i - s[i] * 31^(n-1)) * 31 + s[i+n] * 31^0

			// Proof:
			// h_i+1 = (h_i - s[i] * 31^(n-1)) * 31 + s[i+n] * 31^0
			//       = ((s[i] * 31^(n-1) + s[i+1] * 31^(n-2) + ... + s[i+n-1]*31^0) - s[i] * 31^(n-1)) * 31
			//          + s[i+n] * 31^0
			//       = (s[i+1] * 31^(n-2) + ... + s[i+n-1]*31^0) * 31 + s[i+n] * 31^0
			//       = (s[i+1] * 31^(n-1) + ... + s[i+n-1]*31^1) + s[i+n] * 31^0
			//       = s[i+1] * 31^(n-1) + ... + s[i+n-1]*31^1 + s[i+n] * 31^0
			//       = h_i
			//       q.e.d.
			windowHash = (windowHash - str.charAt(i) * pow31NMinus1) * 31 + str.charAt(i + patternLen);

			// in essence this hash function is equivalent to Java's hashCode()
			// Check the definition from java doc:
			// From the Java original contract for String.hashCode(): Returns a hash code for this string.
			// The hash code for a String object is computed as
			// s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
			// using int arithmetic, where s[i] is the ith character of the string, n is the length of the
			// string, and ^ indicates exponentiation. (The hash value of the empty string is zero.)
			// Returns:
			// a hash code value for this object.
		}
		if (windowHash == patternHash && str.substring(endWindowIdx, endWindowIdx + patternLen).equals(pattern))
			return endWindowIdx;

		return -1;
	}

	public static int pow31(int power) {
		int result = 1;
		for (int i = power - 1; i >= 0; --i)
			result *= 31; // implementation of hashCode() doesn't care about overflow

		return result;
	}
}
