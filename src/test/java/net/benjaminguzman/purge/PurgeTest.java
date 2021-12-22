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

package net.benjaminguzman.purge;

import net.benjaminguzman.GQLFedUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PurgeTest {
	@AfterEach
	void afterEach() throws IOException {
		deleteFiles();
	}

	@BeforeEach
	void beforeEach() throws IOException {
		deleteFiles();
	}

	static void deleteFiles() throws IOException {
		Files.deleteIfExists(Path.of("src/test/resources/purge/purge.actual.graphql"));
		Files.deleteIfExists(Path.of("src/test/resources/purge/purge2.actual.graphql"));
		Files.deleteIfExists(Path.of("src/test/resources/purge/dir/1.actual.graphql"));
		Files.deleteIfExists(Path.of("src/test/resources/purge/dir/2.actual.graphql"));
	}

	@DisplayName("Testing CLI parsing --suffix, --config, input files, exclude files")
	@Test
	void testCliParsing() {
		GQLFedUtils app = new GQLFedUtils();
		CommandLine cmd = new CommandLine(app);

		StringWriter sw = new StringWriter();
		cmd.setOut(new PrintWriter(sw));

		int exitCode = cmd.execute(
			"purge",
			"--suffix", ".actual",
			"--config", "src/test/resources/purge/purge.ok.yaml",
			"--exclude", "node_modules",
			"--exclude", "src/test/resources/purge/purge.expected.graphql",
			"src/test/resources/purge/purge.graphql"
		);
		Purge purger = cmd.getSubcommands().get("purge").getCommand();

		// check configuration loaded successfully
		PurgeConfig config = purger.getConfig();

		assertFalse(purger.hasConfirmedOverwrite());
		assertEquals(".actual", purger.getOutSuffix());
		assertEquals(List.of("@GK", "@GateKeep", "@GKeep"), config.getKeepPatterns());
		assertEquals(List.of("KEEP ME!", ".KEEP ME", "TEST"), config.getSecondKeepPatterns());

		assertEquals(List.of(Path.of("src/test/resources/purge/purge.graphql")), purger.getInputFiles());

		assertEquals(List.of(
			Path.of("node_modules"), Path.of("src/test/resources/purge/purge.expected.graphql")
		), purger.getExcludeFiles());

		assertEquals(0, exitCode);
	}

	@DisplayName("Testing successful purge single file")
	@Test
	void singleSimpleFile() throws IOException {
		GQLFedUtils app = new GQLFedUtils();
		CommandLine cmd = new CommandLine(app);

		StringWriter sw = new StringWriter();
		cmd.setOut(new PrintWriter(sw));

		int exitCode = cmd.execute(
			"purge",
			"--suffix", ".actual",
			"--config", "src/test/resources/purge/purge.ok.yaml",
			"src/test/resources/purge/purge.graphql"
		);

		// check purge.actual.graphql has been created and contains the same as purge.expected.graphql
		String expected = Files.readString(Path.of("src/test/resources/purge/purge.expected.graphql"));
		String actual = Files.readString(Path.of("src/test/resources/purge/purge.actual.graphql"));
		assertEquals(expected, actual);

		assertEquals(0, exitCode);
	}

	@DisplayName("Testing successful purge single file 2")
	@Test
	void singleFile() throws IOException {
		GQLFedUtils app = new GQLFedUtils();
		CommandLine cmd = new CommandLine(app);

		StringWriter sw = new StringWriter();
		cmd.setOut(new PrintWriter(sw));

		int exitCode = cmd.execute(
			"purge",
			"--suffix", ".actual",
			"--config", "src/test/resources/purge/purge2.yaml",
			"src/test/resources/purge/purge2.graphql"
		);

		// check purge.actual.graphql has been created and contains the same as purge.expected.graphql
		String expected = Files.readString(Path.of("src/test/resources/purge/purge2.expected.graphql"));
		String actual = Files.readString(Path.of("src/test/resources/purge/purge2.actual.graphql"));
		assertEquals(expected, actual);

		assertEquals(0, exitCode);
	}

	@DisplayName("Testing successful purge multiple files and exclusions")
	@Test
	void run2() throws IOException {
		GQLFedUtils app = new GQLFedUtils();
		CommandLine cmd = new CommandLine(app);

		StringWriter sw = new StringWriter();
		cmd.setOut(new PrintWriter(sw));

		int exitCode = cmd.execute(
			"purge",
			"--suffix", ".actual",
			"--config", "src/test/resources/purge/purge.ok.yaml",
			"--exclude", "src/test/resources/purge/dir/1.expected.graphql",
			"--exclude", "src/test/resources/purge/dir/1.actual.graphql",
			"--exclude", "src/test/resources/purge/dir/2.expected.graphql",
			"--exclude", "src/test/resources/purge/dir/2.actual.graphql",
			"src/test/resources/purge/purge.graphql", "src/test/resources/purge/dir"
		);

		// check purge.actual.graphql has been created and contains the same as purge.expected.graphql
		assertEquals(
			Files.readString(Path.of("src/test/resources/purge/purge.expected.graphql")),
			Files.readString(Path.of("src/test/resources/purge/purge.actual.graphql"))
		);

		// check files within directory are OK
		assertEquals(
			Files.readString(Path.of("src/test/resources/purge/dir/1.expected.graphql")),
			Files.readString(Path.of("src/test/resources/purge/dir/1.actual.graphql"))
		);
		assertEquals(
			Files.readString(Path.of("src/test/resources/purge/dir/2.expected.graphql")),
			Files.readString(Path.of("src/test/resources/purge/dir/2.actual.graphql"))
		);

		// check exclusion worked
		assertFalse(Files.exists(Path.of("src/test/resources/purge/dir/1.expected.actual.graphql")));
		assertFalse(Files.exists(Path.of("src/test/resources/purge/dir/2.expected.actual.graphql")));

		assertEquals(0, exitCode);
	}
}