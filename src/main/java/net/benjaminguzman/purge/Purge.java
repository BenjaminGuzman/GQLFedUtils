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
import net.benjaminguzman.parse.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import picocli.CommandLine;

import java.io.IOException;
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
	description = "Purge a schema. This is done by removing all types/inputs/enums/fields whose associated " +
		"comments contain a specific text.",
	mixinStandardHelpOptions = true,
	version = "gqlfedutils purge 0.1"
)
public class Purge implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(Purge.class.getName());

	@CommandLine.Option(
		names = {"-e", "--exclude"},
		description = "List of files to exclude from processing. " +
			"Exclusion files have more precedence than input files, i.e. if you provide the same file as" +
			" input and exclusion, it'll be excluded"
	)
	@NotNull
	private final List<Path> excludeFiles = Collections.emptyList();

	@CommandLine.Option(
		names = {"-c", "--config"},
		description = "Path to the config file. The configuration should be a YAML-formatted file with " +
			"the following schema:\n" +
			"keepPatterns: string[]\n" +
			"secondKeepPatterns: string[]\n\n" +
			"Description for each field is:\n" +
			"keepPatterns: All types/inputs/enums/fields whose comments do not include any of these " +
			"patterns will be removed.\n" +
			"secondKeepPatterns: All types/inputs/enums/fields whose comments do not include any of " +
			"these patterns in the same line the first pattern was found will be removed",
		required = true,
		converter = PurgeConfigConverter.class
	)
	private PurgeConfig config;

	@CommandLine.Option(
		names = {"-s", "--suffix"},
		description = "Output file(s) will contain that suffix.\n" +
			"WARNING: if not given, input file will be overwritten. You'll be asked for confirmation\n" +
			"The suffix is added between the file name and 'graphql' extension (if present), e.g. " +
			"if input file is 'file.graphql' and '-purged' is the suffix, then the output file will be " +
			"'file-purged.graphql'"
	)
	@Nullable
	private String outSuffix;

	@CommandLine.Option(
		names = {"--overwrite"},
		description = "If --suffix is not given, you'll be asked for confirmation to overwrite input files. " +
			"Set this flag to confirm your decision in advance, so you won't be asked later"
	)
	private boolean hasConfirmedOverwrite;

	@CommandLine.Parameters(
		paramLabel = "FILE",
		description = "Input files. If a directory is given instead of a file, " +
			"all files inside it will be processed. Maximum depth when traversing the directory is 5",
		arity = "1..*"
	)
	@NotNull
	private final List<Path> inputFiles = Collections.emptyList();

	public Purge() {
	}

	@Override
	public void run() {
		// if no suffix, input files will be overwritten. Warn the user
		if (outSuffix == null || outSuffix.isEmpty())
			if (!handleNoSuffix())
				return;

		if (outSuffix != null && hasConfirmedOverwrite) {
			LOGGER.warning("--suffix and --overwrite can't be used together, --overwrite is ignored");
			hasConfirmedOverwrite = true;
		}

		// process all input files with exclusions
		inputFiles.stream()
			.peek(file -> {
				if (!Files.exists(file))
					LOGGER.warning(file + " doesn't exist, skipping.");
			})
			.filter(Files::exists) // just work with files that do exist
			.flatMap(file -> { // "unpack" directories
				if (Files.isRegularFile(file))
					return Stream.of(file);

				// if file is directory, traverse it and consider exclusions
				try {
					return Files.walk(file, 5)
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

		if (!GQLFedUtils.shouldProceed("Not providing a suffix will overwrite input files"))
			return false;

		System.out.println("Proceeding... You may want to use --overwrite next time 😉");
		return true;
	}

	/**
	 * Purges and saves a single file
	 * <p>
	 * The output file is determined with {@link #outSuffix} and {@link #outputFileWSuffix(Path)}
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
			LOGGER.severe("😭 Error while reading file " + file.toAbsolutePath() + ". " + e.getMessage());
			return;
		} catch (InvalidGQLSyntax e) {
			LOGGER.severe("😭 Couldn't parse file " + file.toAbsolutePath() + ". " + e.getMessage());
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
			int lineEndIdx = GQL.lineEndIdx(comment, firstMatchIdx + 1);
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

				// this right even if lastIndexOf returns -1
				int lineStartIdx = comment.lastIndexOf('\n', firstMatchIdx) + 1;
				int lineEndIdx = Math.min(GQL.lineEndIdx(comment, lineStartIdx) + 1, comment.length());
				dataType.setComment(
					comment.substring(0, lineStartIdx) + comment.substring(lineEndIdx)
				);
			});
		};

		List<GQLDataType> purgedGraph = abstractSyntaxGraph.getDataTypes()
			.stream()
			.peek(dataType -> {
				// gql directive and schema are always kept
				if (dataType.getKeyword() == null)
					return;

				switch (dataType.getKeyword()) {
					case TYPE:
						// Query and Mutation types should always be kept
						String typeName = dataType.getName();
						if (!typeName.equals("Query") && !typeName.equals("Mutation"))
							return;
						// yes, don't use break here. We need to keep Query and Mutation types
						// TODO but don't add keep comment if it already has it
						//  usually this isn't the case so won't add more code
					case DIRECTIVE:
					case SCHEMA:
					case SCALAR:
						String keepComment = firstPatterns.get(0);
						if (!secondPatterns.isEmpty())
							keepComment += " " + secondPatterns.get(0);

						if (dataType.getComment() != null)
							dataType.setComment(keepComment + "\n" + dataType.getComment());
						else
							dataType.setComment(keepComment + "\n");
				}
			})
			.filter(dataType -> dataType.getComment() != null && shouldBeKept.test(dataType.getComment()))
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
			outFile = outputFileWSuffix(file);
		if (!hasConfirmedOverwrite)
			outFile = GQLFedUtils.askAltOut(outFile);
		LOGGER.info("Saving output in " + outFile);
		try {
			Files.writeString(outFile, abstractSyntaxGraph + "\n");
		} catch (IOException e) {
			LOGGER.severe("😭 Error while trying to save file " + outFile.toAbsolutePath() + ". "
				+ e.getMessage());
			LOGGER.info("But, here is the output:\n" + abstractSyntaxGraph);
		}
	}

	/**
	 * Replaces the name "*.graphql" with "*{@link #outSuffix}.graphql"
	 * <p>
	 * If the file name doesn't have ".graphql", {@link #outSuffix} is added at the end of the string
	 * <p>
	 * For example if the original file name is "dir/graph.graphql" and {@link #outSuffix} is ".out",
	 * Then the returned path name will be "dir/graph.out.graphql"
	 *
	 * @param path original path
	 * @return an instance of {@link Path} with name as specified above
	 */
	@NotNull
	private Path outputFileWSuffix(@NotNull Path path) {
		String pathStr = path.toString();
		if (pathStr.endsWith(".graphql"))
			return Path.of(pathStr.substring(0, pathStr.lastIndexOf(".graphql")) + outSuffix + ".graphql");

		return Path.of(path + outSuffix);
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
	public @NotNull List<Path> getInputFiles() {
		return inputFiles;
	}

	/**
	 * Call from testing code only
	 */
	@TestOnly
	public @NotNull List<Path> getExcludeFiles() {
		return excludeFiles;
	}
}
