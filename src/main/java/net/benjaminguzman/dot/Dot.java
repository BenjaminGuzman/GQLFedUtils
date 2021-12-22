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
import net.benjaminguzman.parse.*;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@CommandLine.Command(
	name = "dot",
	description = "Transpile a GraphQL schema to dot code (graphviz)",
	mixinStandardHelpOptions = true,
	version = "gqlfedutils dot 0.1"
)
public class Dot implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(Dot.class.getName());
	private static final String INDENT = "    ";
	private static final String TWICE_INDENT = INDENT + INDENT;

	/**
	 * Cache for {@link System#lineSeparator()}
	 */
	private static final String LINE_SEP = System.lineSeparator();

	/**
	 * String builder used to store the generated dot code
	 */
	private final StringBuilder strBuilder = new StringBuilder(1024);

	@CommandLine.Option(
		names = {"-o", "--output"},
		description = "Output file in which generated dot code will be saved.",
		required = true
	)
	private Path outputFile;

	@CommandLine.Parameters(
		paramLabel = "FILE",
		description = "Input file.",
		arity = "1"
	)
	private Path inputFile;

	@CommandLine.Option(
		names = {"-p", "--params"},
		description = "Set this flag to include parameters in the dot output." +
			"This may clutter the graph. Try it and see how it looks like."
	)
	private boolean includeParams;

	@CommandLine.Option(
		names = {"--enum-values"},
		description = "Set this flag to include enum values in the dot output." +
			"This may clutter the graph. Try it and see how it looks like."
	)
	private boolean includeEnumValues;

	/**
	 * true if the graph has at least one {@link GQLInput}
	 */
	private boolean hasInputs;

	/**
	 * true if the graph has at least one {@link GQLType}
	 */
	private boolean hasTypes;

	/**
	 * true if the graph has at least one {@link GQLEnum}
	 */
	private boolean hasEnums;

	/**
	 * true if the graph has at least one {@link GQLField}
	 * (which implies {@link #hasTypes} or {@link #hasInputs} is also true)
	 */
	private boolean hasFields;

	@Override
	public void run() {
		if (inputFile.equals(outputFile))
			if (!GQLFedUtils.shouldProceed("Input and output files are the same"))
				return;

		LOGGER.info("Processing " + inputFile);
		Map<GQLDataType, List<GQLDataType>> adjList;
		try {
			adjList = GQL.from(inputFile).getGraph();
		} catch (IOException e) {
			LOGGER.severe("😭 Error while reading file " + inputFile.toAbsolutePath() + ". " + e.getMessage());
			return;
		} catch (InvalidGQLSyntax e) {
			LOGGER.severe("😭 Couldn't parse file " + inputFile.toAbsolutePath() + ". " + e.getMessage());
			return;
		}

		// open an undirected graph
		strBuilder.append("graph {").append(LINE_SEP);

		// add each data type converted to dot code into the builder
		adjList.keySet().forEach(this::convertDataType);

		// connect data types
		strBuilder.append(LINE_SEP).append(INDENT)
			.append("# Connections between types")
			.append(LINE_SEP);
		adjList.entrySet().stream()
			// just type, input and enum can be connected
			.filter(entry -> entry.getKey() instanceof GQLStruct || entry.getKey() instanceof GQLEnum)
			.forEach(entry -> {
				GQLDataType src = entry.getKey();
				String srcId = dotId(src);
				entry.getValue().forEach(dest -> strBuilder.append(INDENT)
					.append(srcId)
					.append(" -- ")
					.append(dotId(dest))
					.append(';')
					.append(LINE_SEP)
				);
			});

		// add legend
		// this is commented out because sfdp ignores completely the subgraph and looks really weird
		// this.addLegend();

		// close undirected graph
		strBuilder.append("}").append(LINE_SEP);

		// write output
		LOGGER.info("Saving output in " + outputFile);
		try {
			Files.writeString(outputFile, strBuilder.toString());
		} catch (IOException e) {
			LOGGER.severe(
				"😭 Couldn't save output to file " + outputFile.toAbsolutePath() + ". " + e.getMessage()
			);
			LOGGER.info("But, here is the generated dot code:\n" + strBuilder);
		}
	}

	/**
	 * Converts the data type to dot code
	 * <p>
	 * This method writes directly to {@link #strBuilder}
	 *
	 * @param dataType the data type to be converted. If it is not a {@link net.benjaminguzman.parse.GQLEnum},
	 *                 {@link net.benjaminguzman.parse.GQLType}, or
	 *                 {@link net.benjaminguzman.parse.GQLInput}
	 *                 it is ignored and not added to {@link #strBuilder}
	 */
	private void convertDataType(@NotNull GQLDataType dataType) {
		if (dataType.getKeyword() == null)
			return;
		switch (dataType.getKeyword()) {
			case DIRECTIVE:
			case SCALAR:
			case SCHEMA:
				return; // these are ignored
			case ENUM:
				hasEnums = true;
				convertEnum((GQLEnum) dataType);
				break;
			case INPUT:
				hasInputs = true;
				convertStruct((GQLStruct) dataType);
				break;
			case TYPE:
				hasTypes = true;
				convertStruct((GQLStruct) dataType);
				break;
		}
		strBuilder.append(LINE_SEP);
	}

	private void convertEnum(@NotNull GQLEnum gqlEnum) {
		strBuilder.append(INDENT).append("# enum: ").append(gqlEnum.getName()).append(LINE_SEP);

		// add <name>_<hashCode>[color=mediumpurple1, style=filled, label=<name>];
		String enumId = dotId(gqlEnum);
		strBuilder.append(INDENT)
			.append(enumId)
			.append("[color=mediumpurple1, style=filled, label=\"").append(gqlEnum.getName())
			.append("\"];")
			.append(LINE_SEP);

		if (!includeEnumValues)
			return;

		// add enum values
		for (GQLEnum.EnumValue value : gqlEnum.getValues())
			strBuilder.append(INDENT)
				.append(dotId(value))
				.append("[label=\"").append(value.getName()).append("\"];")
				.append(LINE_SEP);

		// connect values with enum
		for (GQLEnum.EnumValue value : gqlEnum.getValues())
			strBuilder.append(INDENT)
				.append(enumId)
				.append(" -- ")
				.append(dotId(value))
				.append(';')
				.append(LINE_SEP);
	}

	private void convertStruct(@NotNull GQLStruct struct) {
		assert struct.getKeyword() != null;
		strBuilder.append(INDENT)
			.append("# ").append(struct.getKeyword().toString()).append(": ").append(struct.alphaName())
			.append(LINE_SEP);

		// add <name>_<hashCode>[color=color, style=filled, label=<name>];
		String structId = dotId(struct);
		String color = "red";
		switch (struct.getKeyword()) {
			case INPUT:
				color = "lightskyblue";
				break;
			case TYPE:
				color = "greenyellow";
		}
		strBuilder.append(INDENT)
			.append(structId)
			.append("[")
			.append("color=").append(color).append(", style=filled, label=\"").append(struct.alphaName())
			.append("\"];")
			.append(LINE_SEP);

		// add fields
		for (GQLField field : struct.getFields()) {
			String fieldId = dotId(field);
			strBuilder.append(INDENT)
				.append(fieldId)
				.append("[color=snow2, style=filled, label=\"").append(field.alphaName()).append(
					"\"];")
				.append(LINE_SEP);

			// add field params
			if (!field.hasParams() || !includeParams)
				continue;

			hasFields = true;
			for (GQLField.GQLFieldParam param : field.getParams())
				strBuilder.append(INDENT)
					.append(dotId(param))
					.append("[label=\"").append(param.alphaName()).append("\"];")
					.append(LINE_SEP);

			// connect params with field
			for (GQLField.GQLFieldParam param : field.getParams())
				strBuilder.append(INDENT)
					.append(fieldId)
					.append(" -- ")
					.append(dotId(param))
					.append(';')
					.append(LINE_SEP);
		}

		// connect fields with struct
		for (GQLField field : struct.getFields())
			strBuilder.append(INDENT)
				.append(structId)
				.append(" -- ")
				.append(dotId(field))
				.append(';')
				.append(LINE_SEP);
	}

	/**
	 * Adds dot code to show a legend in the graph
	 */
	private void addLegend() {
		if (!(hasEnums || hasInputs || hasTypes || hasFields))
			return;

		strBuilder.append(LINE_SEP).append(INDENT).append("subgraph cluster_01 {").append(LINE_SEP)
			.append(TWICE_INDENT).append("rank=sink;").append(LINE_SEP)
			.append(TWICE_INDENT).append("label=\"Legend\";").append(LINE_SEP);

		// see https://graphviz.org/doc/info/colors.html
		if (hasEnums)
			strBuilder.append(TWICE_INDENT)
				.append("__enum_legend[color=mediumpurple1, style=filled, label=enum];")
				.append(LINE_SEP);
		if (hasTypes)
			strBuilder.append(TWICE_INDENT)
				.append("__type_legend[color=greenyellow, style=filled, label=type];")
				.append(LINE_SEP);
		if (hasEnums)
			strBuilder.append(TWICE_INDENT)
				.append("__input_legend[color=lightskyblue, style=filled, label=input];")
				.append(LINE_SEP);
		if (hasFields)
			strBuilder.append(TWICE_INDENT)
				.append("__field_legend[color=snow2, style=filled, label=field];")
				.append(LINE_SEP);

		strBuilder.append(INDENT).append("}").append(LINE_SEP);
	}

	/**
	 * Get a dot id for a specific data type
	 *
	 * @param dataType the data type for which an id will be obtained
	 * @return the id
	 */
	private String dotId(@NotNull GQLDataType dataType) {
		return dataType.alphaName() + "_" + Math.abs(dataType.hashCode());
	}
}
