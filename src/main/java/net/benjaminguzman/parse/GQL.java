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

package net.benjaminguzman.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Wrapper class to contain all parsed contents inside a graphql file
 */
public class GQL {
	public static final Logger LOGGER = Logger.getLogger(GQL.class.getName());

	/**
	 * GraphQL comment delimiter
	 */
	public static final String COMMENT_DELIMITER = "\"\"\"";

	public static final char DEFAULT_INDENTATION_CHAR = ' ';
	public static final int DEFAULT_INDENTATION_SIZE = 2;

	@NotNull
	private List<GQLDataType> dataTypes = new ArrayList<>();

	/**
	 * Same as {@link #dataTypes} but as a graph (adjacency list)
	 */
	@Nullable
	private Map<GQLDataType, List<GQLDataType>> adjList;

	/**
	 * Comments indicated with '#'
	 */
	@Nullable
	private String comments;

	/**
	 * Parse contents from the given file
	 *
	 * @param file the file whose contents will be parsed
	 * @return a {@link GQL} object
	 * @throws IOException      if there was an error when reading the file
	 * @throws InvalidGQLSyntax if the file has invalid graphql syntax
	 */
	public static GQL from(@NotNull Path file) throws IOException, InvalidGQLSyntax {
		LOGGER.fine("Parsing file: " + file);
		return from(Files.readString(file));
	}

	/**
	 * Parse contents from the given string
	 *
	 * @param str the string whose contents will be parsed
	 * @return a {@link GQL} object
	 * @throws InvalidGQLSyntax if the string has invalid GraphQL syntax
	 */
	public static GQL from(@NotNull String str) throws InvalidGQLSyntax {
		GQL gql = new GQL();
		StringBuilder commentsBuilder = new StringBuilder();
		String comment;

		int cursorIdx = 0; // current cursor in the string
		while ((cursorIdx = ignoreWhitespaces(str, cursorIdx)) < str.length()) {
			// by now we know the char at i is not a whitespace

			// parse comments
			if (str.startsWith(GQL.COMMENT_DELIMITER, cursorIdx)) {
				int commentEndIdx = str.indexOf(GQL.COMMENT_DELIMITER, cursorIdx + 3);
				comment = str.substring(cursorIdx + 3, commentEndIdx); // +3 to skip the delimiter """

				// move the cursor to the next non whitespace, starting from the end of the comment
				cursorIdx = ignoreWhitespaces(str, commentEndIdx + 3);
			} else
				comment = null;

			int endIdx = str.indexOf('}', cursorIdx);
			if (str.startsWith(GQLKeyword.TYPE.toString(), cursorIdx)) { // parse type
				if (endIdx == -1)
					throw new InvalidGQLSyntax(GQLInput.class, str, "There is a '}' missing");
				gql.dataTypes.add(GQLType.parse(str.substring(cursorIdx, endIdx + 1), comment));
			} else if (str.startsWith(GQLKeyword.INPUT.toString(), cursorIdx)) { // parse input
				if (endIdx == -1)
					throw new InvalidGQLSyntax(GQLInput.class, str, "There is a '}' missing");
				gql.dataTypes.add(GQLInput.parse(str.substring(cursorIdx, endIdx + 1), comment));
			} else if (str.startsWith(GQLKeyword.ENUM.toString(), cursorIdx)) { // parse enum
				if (endIdx == -1)
					throw new InvalidGQLSyntax(GQLEnum.class, str, "There is a '}' missing");
				gql.dataTypes.add(GQLEnum.parse(str.substring(cursorIdx, endIdx + 1), comment));
			} else if (str.startsWith(GQLKeyword.SCALAR.toString(), cursorIdx)) { // parse scalar
				endIdx = lineEndIdx(str, cursorIdx);
				gql.dataTypes.add(GQLScalar.parse(str.substring(cursorIdx, endIdx), comment));
			} else if (str.startsWith(GQLKeyword.DIRECTIVE.toString(), cursorIdx)) { // parse directive
				endIdx = lineEndIdx(str, cursorIdx);
				gql.dataTypes.add(GQLDirective.parse(str.substring(cursorIdx, endIdx), comment));
			} else if (str.startsWith(GQLKeyword.SCHEMA.toString(), cursorIdx)) { // parse schema
				if (endIdx == -1)
					throw new InvalidGQLSyntax(GQLSchema.class, str, "There is a '}' missing");
				gql.dataTypes.add(GQLSchema.parse(str.substring(cursorIdx, endIdx + 1), comment));
			} else if (str.charAt(cursorIdx) == '#') {
				endIdx = lineEndIdx(str, cursorIdx);
				commentsBuilder.append(str, cursorIdx, endIdx).append('\n');
			} else {
				endIdx = lineEndIdx(str, cursorIdx);
				LOGGER.warning("String \"" + str.substring(cursorIdx, endIdx)
					+ "\" was not recognized, it'll be ignored");
			}

			cursorIdx = endIdx + 1;
		}

		gql.comments = commentsBuilder.toString();
		return gql;
	}

	/**
	 * Move the cursor to the next index in the string that is not a whitespace
	 *
	 * @param i   current value of the cursor
	 * @param str the string
	 * @return the next index. {@code Character.isWhitespace(str.charAt(nextIdx))} should be false
	 */
	public static int ignoreWhitespaces(@NotNull String str, int i) {
		for (; i < str.length() && Character.isWhitespace(str.charAt(i)); ++i) ;
		return i;
	}

	/**
	 * Move the cursor to the next line feed or the end of the string
	 *
	 * @param str the string
	 * @param i   current value of the cursor
	 * @return the index for the next line feed or EOF
	 */
	public static int lineEndIdx(@NotNull String str, int i) {
		i = str.indexOf('\n', i);
		return i > -1 ? i : str.length();
	}

	/**
	 * Resolves name references in types and inputs to construct a graph
	 * <p>
	 * Calling multiple times this method has no performance overhead since internally
	 * the constructed graph is cached
	 *
	 * @return the graph as an adjacency list
	 */
	public Map<GQLDataType, List<GQLDataType>> getGraph() {
		if (dataTypes.isEmpty())
			return Collections.emptyMap();

		// if there are no nodes, or the graph has been constructed do nothing
		if (adjList != null) // we've already computed the graph
			return adjList;

		// initialize vertices
		adjList = new HashMap<>();
		for (GQLDataType dataType : dataTypes)
			adjList.put(dataType, Collections.emptyList());

		// map to allow fast access to each gql data type by its name
		Map<String, GQLDataType> mapNameType = new HashMap<>();
		for (GQLDataType dataType : dataTypes)
			mapNameType.put(dataType.alphaName(), dataType);

		// add edges
		dataTypes.stream()
			// input and type are the only ones that can have reference to other vertices
			.filter(dataType -> dataType instanceof GQLStruct)
			.map(dataType -> (GQLStruct) dataType)
			.forEach(struct -> { // add edges
				// get all referenced types in parameters
				// use set to prevent duplicate references
				Set<GQLDataType> referencedVertices = struct.getFields()
					.stream()
					.map(GQLField::getParams)
					.flatMap(List::stream)
					.map(fieldParam -> fieldParam.getType(true))
					.map(mapNameType::get)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

				// get all referenced types by return type
				struct.getFields()
					.stream()
					.map(field -> field.getReturnType(true))
					.map(mapNameType::get)
					.filter(Objects::nonNull)
					.forEach(referencedVertices::add);

				adjList.put(struct, new ArrayList<>(referencedVertices));
			});

		return adjList;
	}

	/**
	 * @return the data types stored in this object
	 */
	@NotNull
	public List<GQLDataType> getDataTypes() {
		return dataTypes;
	}

	/**
	 * @return the data types stored in this object
	 */
	public GQL setDataTypes(@NotNull List<GQLDataType> dataTypes) {
		this.dataTypes = dataTypes;
		return this;
	}

	@Override
	public String toString() {
		String dataTypesStr = dataTypes.stream()
			.map(Object::toString)
			.collect(Collectors.joining("\n\n"));

		if (comments == null || comments.isBlank())
			return dataTypesStr;

		return comments + "\n" + dataTypesStr;
	}
}
