package net.benjaminguzman.purge;

import net.benjaminguzman.parse.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
	name = "purge",
	description = "Purge a schema. This is done by removing all fields/operations/types whose associated " +
		"comments contain a specific text.",
	mixinStandardHelpOptions = true,
	version = "gqlfedutils purge 0.1"
)
public class Purge implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(Purge.class.getName());

	@CommandLine.Parameters(
		paramLabel = "FILE",
		description = "Input files. If a directory is given instead of a file, all files with 'graphql' " +
			"extension inside it will be processed. Maximum depth when traversing the directory is 5",
		arity = "1..*"
	)
	@NotNull
	private final List<File> inputFiles = Collections.emptyList();

	@CommandLine.Option(
		names = {"-e", "--exclude"},
		description = "List of files to exclude from processing. " +
			"Exclusion files have more precedence than input files, i.e. if you provide the same file as" +
			" " +
			"input and exclusion, it'll be excluded"
	)
	@NotNull
	private final List<File> excludeFiles = Collections.emptyList();

	@CommandLine.Option(
		names = {"-c", "--config"},
		description = "Path to the config file. The configuration should be a YAML-formatted file with " +
			"the following schema:\n" +
			"keepPatterns: string[]\n" +
			"secondKeepPatterns: string[]\n\n" +
			"Description for each field is:\n" +
			"keepPatterns: All fields/operations/types whose comments do not include any of these " +
			"patterns" +
			" " +
			"will be removed.\n" +
			"secondKeepPatterns: All fields/operations/types whose comments do not include any of these " +
			"patterns in the same line the first pattern was found will be removed",
		required = true,
		converter = PurgeConfigConverter.class
	)
	private PurgeConfig config;

	@CommandLine.Option(
		names = {"-s", "--suffix"},
		description = "Output file(s) will contain that suffix.\n" +
			"WARNING: if not given, input file will be overwritten. You'll be asked for confirmation\n" +
			"The suffix is added between the file name and extension, e.g. " +
			"if input file is 'file.graphql' and '-purged' is the suffix, then the output file will be " +
			"'file-purged.graphql'"
	)
	@Nullable
	private String outSuffix;

	@CommandLine.Option(
		names = {"--overwrite"},
		description = "If --suffix is not given, you'll be asked for confirmation to overwrite input files. " +
			"Set this flag to confirm your decision in advance so you won't be asked later"
	)
	private boolean hasConfirmedOverwrite;

	public Purge() {
	}

	@Override
	public void run() {
		// if no suffix, input files will be overwritten. Warn the user
		if (outSuffix == null || outSuffix.isEmpty())
			if (!handleNoSuffix())
				return;

		if (outSuffix != null && hasConfirmedOverwrite)
			LOGGER.warning("--suffix and --overwrite can't be used together, --overwrite is ignored");

		// process all input files with exclusions
		inputFiles.stream()
			.peek(file -> {
				if (!file.exists())
					LOGGER.warning(file + " doesn't exist, skipping.");
			})
			.filter(File::exists) // just work with files that do exist
			.flatMap(file -> { // "unpack" directories
				if (file.isFile())
					return Stream.of(file.toPath());

				// if file is directory, traverse it and consider exclusions
				try {
					return Files.walk(file.toPath(), 5)
						.filter(p -> p.toFile().isFile() && excludeFiles.stream().noneMatch(
							// this is a naive criteria to exclude files,
							// but it is ok for now
							exclusion -> p.toString().contains(exclusion.toString())
						));
				} catch (IOException e) {
					LOGGER.warning("Error while processing " + file + ". " + e.getMessage());
					return Stream.empty();
				}
			})
			.forEach(this::handleSingleFile);
	}

	/**
	 * Method to warn the user input files are going to be overwritten
	 * <p>
	 * If needed, this will ask the user if he/she wants to proceed
	 *
	 * @return true if the user responded 'Y' or {@link #hasConfirmedOverwrite} is true, false otherwise
	 */
	private boolean handleNoSuffix() {
		if (hasConfirmedOverwrite) {
			LOGGER.info("Input files will be overwritten");
			return true;
		}

		System.out.print("🚨 Warning: Not providing a suffix will overwrite input files. " +
			"Would you like to proceed (Y/n)? ");

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			if (!"Y".equals(reader.readLine().strip()))
				return false;
		} catch (IOException ignored) {
		}

		String msg = CommandLine.Help.Ansi.AUTO.string(
			"Proceeding... You may want to use @|bold--overwrite|@ next time to save time 😉"
		);
		System.out.println(msg);
		return true;
	}

	/**
	 * Purges and saves a single file
	 * <p>
	 * The output file is determined with {@link #outSuffix} and {@link #renameFile(String)}
	 *
	 * @param file the file to be purged
	 */
	private void handleSingleFile(@NotNull Path file) {
		LOGGER.info("Processing " + file);

		// parse file
		GQL abstractSyntaxGraph;
		try {
			abstractSyntaxGraph = GQL.from(file);
		} catch (IOException e) {
			LOGGER.severe("Error while reading file " + file.toAbsolutePath() + ". " + e.getMessage());
			return;
		} catch (InvalidGQLSyntax e) {
			LOGGER.severe("Couldn't parse file " + file.toAbsolutePath() + ". " + e.getMessage());
			return;
		}

		// purge its contents
		List<String> firstPatterns = config.getKeepPatterns();
		List<String> secondPatterns = config.getSecondKeepPatterns();
		Predicate<String> shouldBeKept = (String comment) -> firstPatterns.stream().anyMatch(pattern -> {
			// find first pattern
			int firstMatchIdx = comment.indexOf(pattern);
			if (firstMatchIdx == -1)
				return false;

			// find any second pattern after the first match, but within the same line
			int lineEndIdx = comment.indexOf('\n', firstMatchIdx + 1);
			lineEndIdx = lineEndIdx == -1 ? comment.length() : lineEndIdx;
			String lineOfInterest = comment.substring(firstMatchIdx, lineEndIdx);
			return secondPatterns.stream().anyMatch(lineOfInterest::contains);
		});
		Consumer<GQLDataType> removePattern = dataType -> {
			// remove the "keep me" comment
			String comment = dataType.getComment();
			assert comment != null;
			firstPatterns.forEach(pattern -> {
				int firstMatchIdx = comment.indexOf(pattern);
				if (firstMatchIdx == -1)
					return;

				int lineStartIdx = comment.lastIndexOf('\n', firstMatchIdx) + 1;
				int lineEndIdx = comment.indexOf('\n', lineStartIdx);
				dataType.setComment(
					comment.substring(0, lineStartIdx) + comment.substring(lineEndIdx + 1)
				);
			});
		};

		List<GQLDataType> purgedGraph = abstractSyntaxGraph.getDataTypes()
			.stream()
			.filter(dataType -> dataType.getComment() != null) // data types without comment are removed
			.filter(dataType -> shouldBeKept.test(dataType.getComment()))
			.peek(removePattern)
			.collect(Collectors.toList());

		// by now the graph has not been completely purged.
		// We still need to purge fields and enum values (above we only removed data types)
		purgedGraph.forEach(dataType -> { // now it's time to process fields. level 2
			if (!(dataType instanceof GQLStruct))
				return;

			// doing this is ok since GQLStruct just returns a reference to the list
			((GQLStruct) dataType).getFields().removeIf(
				field -> field.getComment() == null || !shouldBeKept.test(field.getComment())
			);
			((GQLStruct) dataType).getFields().forEach(removePattern);
		});
		purgedGraph.forEach(dataType -> { // now it's time to process enum values. level 2
			if (!(dataType instanceof GQLEnum))
				return;

			// doing this is ok since GQLStruct just returns a reference to the list
			((GQLEnum) dataType).getValues().removeIf(
				val -> val.getComment() == null || !shouldBeKept.test(val.getComment())
			);
			((GQLEnum) dataType).getValues().forEach(removePattern);
		});

		abstractSyntaxGraph.setDataTypes(purgedGraph);

		// Now that the graph is completely purged, we just need to reconstruct the graphql file
		Path outFile = file;
		if (outSuffix != null)
			outFile = Path.of(
				file.toAbsolutePath().getParent().toString(),
				renameFile(file.getFileName().toString())
			);
		LOGGER.info("Saving file: " + file);
		try {
			Files.writeString(outFile, abstractSyntaxGraph + "\n");
		} catch (IOException e) {
			LOGGER.severe("Error while trying to save file " + outFile.toAbsolutePath() + ". "
				+ e.getMessage());
		}
	}

	/**
	 * Replaces the name "*.graphql" with "*{@link #outSuffix}.graphql"
	 * <p>
	 * If the file name doesn't have ".graphql", {@link #outSuffix} is added at the end of the string
	 *
	 * @param name original file name
	 * @return a modified string
	 */
	@NotNull
	private String renameFile(@NotNull String name) {
		boolean containsGraphql = name.contains(".graphql");
		if (containsGraphql)
			return name.replace(".graphql", outSuffix + ".graphql");

		return name + outSuffix;
	}

	/**
	 * Call from testing code only
	 */
	@TestOnly
	public PurgeConfig getConfig() {
		return config;
	}

	/**
	 * Call from testing code only
	 */
	@TestOnly
	public @Nullable String getOutSuffix() {
		return outSuffix;
	}

	/**
	 * Call from testing code only
	 */
	@TestOnly
	public boolean hasConfirmedOverwrite() {
		return hasConfirmedOverwrite;
	}

	/**
	 * Call from testing code only
	 */
	@TestOnly
	public @NotNull List<File> getInputFiles() {
		return inputFiles;
	}

	/**
	 * Call from testing code only
	 */
	@TestOnly
	public @NotNull List<File> getExcludeFiles() {
		return excludeFiles;
	}
}
