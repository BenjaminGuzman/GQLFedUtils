/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.dev>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.benjaminguzman;

import net.benjaminguzman.dot.Dot;
import net.benjaminguzman.purge.Purge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
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

	/**
	 * Asks for an alternative output file if the given one already exist
	 * <p>
	 * If the output file doesn't exist, this method will simply return it, and won't check if it is a valid path
	 * <p>
	 * This method will read from stdin and validate the alternative file don't exist
	 *
	 * @param output output file
	 * @return an alternative output file or the same output file given as argument
	 */
	@NotNull
	public static Path askAltOut(@NotNull Path output) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String response;
		while (Files.exists(output)) {
			System.out.print(
				"File " + output + " already exist. " +
					"Enter 'Y' if you want to overwrite it, or enter an alternative file path: "
			);
			try {
				response = reader.readLine().strip();
			} catch (IOException e) {
				LOGGER.severe("Couldn't read from stdin. " + e.getMessage());
				response = null;
			}
			if (response == null)
				continue;
			if ("Y".equals(response))
				return output;

			try {
				output = Path.of(response);
			} catch (InvalidPathException e) {
				System.out.println("Error. " + e.getMessage());
			}
		}
		return output;
	}
}
