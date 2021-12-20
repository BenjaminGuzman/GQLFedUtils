package net.benjaminguzman.purge;

import net.benjaminguzman.GQLFedUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PurgeTest {
	@AfterAll
	static void afterEach() throws IOException {
		Files.deleteIfExists(Path.of("src/test/resources/purge.actual.graphql"));
		Files.deleteIfExists(Path.of("src/test/resources/purge2.actual.graphql"));
		Files.deleteIfExists(Path.of("src/test/resources/dir/1.actual.graphql"));
		Files.deleteIfExists(Path.of("src/test/resources/dir/2.actual.graphql"));
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
			"--config", "src/test/resources/purge.ok.yaml",
			"--exclude", "node_modules",
			"--exclude", "src/test/resources/purge.expected.graphql",
			"src/test/resources/purge.graphql"
		);
		Purge purger = cmd.getSubcommands().get("purge").getCommand();

		// check configuration loaded successfully
		PurgeConfig config = purger.getConfig();

		assertFalse(purger.hasConfirmedOverwrite());
		assertEquals(".actual", purger.getOutSuffix());
		assertEquals(List.of("@GK", "@GateKeep", "@GKeep"), config.getKeepPatterns());
		assertEquals(List.of("KEEP ME!", ".KEEP ME", "TEST"), config.getSecondKeepPatterns());

		assertEquals(List.of(new File("src/test/resources/purge.graphql")), purger.getInputFiles());

		assertEquals(List.of(
			new File("node_modules"), new File("src/test/resources/purge.expected.graphql")
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
			"--config", "src/test/resources/purge.ok.yaml",
			"src/test/resources/purge.graphql"
		);

		// check purged.actual.graphql has been created and contains the same as purge.expected.graphql
		byte[] expected = Files.readAllBytes(Path.of("src/test/resources/purge.expected.graphql"));
		byte[] actual = Files.readAllBytes(Path.of("src/test/resources/purge.actual.graphql"));
		assertArrayEquals(expected, actual);

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
			"--config", "src/test/resources/purge2.yaml",
			"src/test/resources/purge2.graphql"
		);

		// check purged.actual.graphql has been created and contains the same as purge.expected.graphql
		byte[] expected = Files.readAllBytes(Path.of("src/test/resources/purge2.expected.graphql"));
		byte[] actual = Files.readAllBytes(Path.of("src/test/resources/purge2.actual.graphql"));
		assertArrayEquals(expected, actual);

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
			"--config", "src/test/resources/purge.ok.yaml",
			"--exclude", "src/test/resources/dir/1.expected.graphql",
			"--exclude", "src/test/resources/dir/1.actual.graphql",
			"--exclude", "src/test/resources/dir/2.expected.graphql",
			"--exclude", "src/test/resources/dir/2.actual.graphql",
			"src/test/resources/purge.graphql", "src/test/resources/dir"
		);

		// check purged.actual.graphql has been created and contains the same as purge.expected.graphql
		assertArrayEquals(
			Files.readAllBytes(Path.of("src/test/resources/purge.expected.graphql")),
			Files.readAllBytes(Path.of("src/test/resources/purge.actual.graphql"))
		);

		// check files within directory are OK
		assertArrayEquals(
			Files.readAllBytes(Path.of("src/test/resources/dir/1.expected.graphql")),
			Files.readAllBytes(Path.of("src/test/resources/dir/1.actual.graphql"))
		);
		assertArrayEquals(
			Files.readAllBytes(Path.of("src/test/resources/dir/2.expected.graphql")),
			Files.readAllBytes(Path.of("src/test/resources/dir/2.actual.graphql"))
		);

		// check exclusion worked
		assertFalse(Files.exists(Path.of("src/test/resources/dir/1.expected.actual.graphql")));
		assertFalse(Files.exists(Path.of("src/test/resources/dir/2.expected.actual.graphql")));

		// TODO test miscellaneous text
		// TODO add graphql to dot code conversion
		assertEquals(0, exitCode);
	}
}