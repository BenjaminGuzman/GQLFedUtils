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

package net.benjaminguzman.dot;

import net.benjaminguzman.GQLFedUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DotTest {
	@AfterAll
	static void afterEach() throws IOException {
		Files.deleteIfExists(Path.of("src/test/resources/dot/dot.actual.dot"));
		Files.deleteIfExists(Path.of("src/test/resources/dot/dot2.actual.dot"));
	}

	@DisplayName("Testing dot transpile (simple)")
	@Test
	void run1() throws IOException {
		GQLFedUtils app = new GQLFedUtils();
		CommandLine cmd = new CommandLine(app);

		StringWriter sw = new StringWriter();
		cmd.setOut(new PrintWriter(sw));

		int exitCode = cmd.execute(
			"dot",
			"--output", "src/test/resources/dot/dot.actual.dot",
			"src/test/resources/dot/dot.graphql"
		);

		// check dot.actual.dot has been created and contains the same as dot.expected.dot
//		String expected = Files.readString(Path.of("src/test/resources/dot/dot.expected.dot"));
//		String actual = Files.readString(Path.of("src/test/resources/dot/dot.actual.dot"));
//		assertEquals(expected, actual);

		assertEquals(0, exitCode);
	}

	@DisplayName("Testing dot transpile (complex)")
	@Test
	void run2() throws IOException {
		GQLFedUtils app = new GQLFedUtils();
		CommandLine cmd = new CommandLine(app);

		StringWriter sw = new StringWriter();
		cmd.setOut(new PrintWriter(sw));

		int exitCode = cmd.execute(
			"dot",
			"--output", "src/test/resources/dot/dot2.actual.dot",
			"src/test/resources/dot/dot2.graphql"
		);

		// check dot.actual.dot has been created and contains the same as dot.expected.dot
//		String expected = Files.readString(Path.of("src/test/resources/dot/dot2.expected.dot"));
//		String actual = Files.readString(Path.of("src/test/resources/dot/dot2.actual.dot"));
//		assertEquals(expected, actual);

		assertEquals(0, exitCode);
	}
}