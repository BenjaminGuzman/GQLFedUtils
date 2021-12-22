package net.benjaminguzman;

import net.benjaminguzman.dot.Dot;
import net.benjaminguzman.purge.Purge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

@CommandLine.Command(
	name = "gqlfedutils",
	description = "gqlfedutils is a set of utilities to help you manage GraphQL Federated services.",
	subcommands = {
		Purge.class,
		Dot.class
	},
	version = "gqlfedutils v0.1",
	header = "Copyright (c) 2021. Benjamín Antonio Velasco Guzmán\n" +
		"This program comes with ABSOLUTELY NO WARRANTY.\n" +
		"This is free software, and you are welcome to redistribute it\n" +
		"under certain conditions.\n" +
		"License GPLv3: GNU GPL version 3 <http://gnu.org/licenses/gpl.html>\n",
	mixinStandardHelpOptions = true
)
public class GQLFedUtils {
	private static final Logger LOGGER = Logger.getLogger(GQLFedUtils.class.getName());

	public static void main(String... args) {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%4$-7s] [%1$tF %1$tT] %5$s %n");
		new CommandLine(new GQLFedUtils()).execute(args);
	}

	/**
	 * Reads a single line from stdin.
	 * <p>
	 * If it fails, error will be logged, and return value is {@code null}
	 *
	 * @return the first line in stdin ({@link String#strip()} is applied). {@code null} if it failed
	 */
	@Nullable
	public static String readStdinLine() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			return reader.readLine().strip();
		} catch (IOException e) {
			LOGGER.severe("Couldn't read from stdin. " + e.getMessage());
		}
		return null;
	}

	/**
	 * Asks for user confirmation.
	 * <p>
	 * This method will output to stdout the following:
	 * <p>
	 * {@code 🚨 -message-. Would you like to proceed? (Y/n)? }
	 *
	 * @return true if, and only if, the user input was 'Y'
	 */
	public static boolean shouldProceed(@NotNull String message) {
		System.out.print("🚨 " + message + ". Would you like to proceed? (Y/n): ");
		return "Y".equals(readStdinLine());
	}
}
