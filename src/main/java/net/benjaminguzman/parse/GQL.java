package net.benjaminguzman.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	public static final int DEFAULT_INDENTATION_SIZE = 4;

	@NotNull
	private List<GQLDataType> dataTypes = new ArrayList<>();

	/**
	 * Miscellaneous text that couldn't be parsed
	 */
	@Nullable
	private String misc;

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
		StringBuilder miscTextBuilder = new StringBuilder();
		String comment;

		int cursorIdx = 0; // current cursor in the string
		while (cursorIdx < str.length()) {
			cursorIdx = ignoreWhitespaces(str, cursorIdx);
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
			if (str.startsWith(GQLKeyword.ENUM.toString(), cursorIdx)) { // parse enum
				if (endIdx == -1)
					throw new InvalidGQLSyntax(GQLEnum.class, str, "There is a '}' missing");
				gql.dataTypes.add(
					GQLEnum.parse(str.substring(cursorIdx, endIdx + 1), comment)
				);
			} else if (str.startsWith(GQLKeyword.INPUT.toString(), cursorIdx)) { // parse input
				if (endIdx == -1)
					throw new InvalidGQLSyntax(GQLInput.class, str, "There is a '}' missing");
				gql.dataTypes.add(
					GQLInput.parse(str.substring(cursorIdx, endIdx + 1), comment)
				);
			} else if (str.startsWith(GQLKeyword.SCALAR.toString(), cursorIdx)) { // parse scalar
				endIdx = str.indexOf('\n', cursorIdx);
				if (endIdx == -1)
					endIdx = str.length();
				gql.dataTypes.add(
					GQLScalar.parse(str.substring(cursorIdx, endIdx), comment)
				);
			} else if (str.startsWith(GQLKeyword.TYPE.toString(), cursorIdx)) { // parse type
				if (endIdx == -1)
					throw new InvalidGQLSyntax(GQLInput.class, str, "There is a '}' missing");
				gql.dataTypes.add(
					GQLType.parse(str.substring(cursorIdx, endIdx + 1), comment)
				);
			} else {
				// move cursor to next keyword
				// text from i to the index of the next keyword will be miscellaneous text

				// not the most efficient way of doing it, but it should work
				int commentIdx = str.indexOf(GQL.COMMENT_DELIMITER, cursorIdx);
				int enumIdx = str.indexOf(GQLKeyword.ENUM.toString(), cursorIdx);
				int inputIdx = str.indexOf(GQLKeyword.INPUT.toString(), cursorIdx);
				int scalarIdx = str.indexOf(GQLKeyword.SCALAR.toString(), cursorIdx);
				int typeIdx = str.indexOf(GQLKeyword.TYPE.toString(), cursorIdx);

				OptionalInt nextKeywordIdx = IntStream.of(
						commentIdx,
						enumIdx,
						inputIdx,
						scalarIdx,
						typeIdx
					)
					.filter(idx -> idx > -1)
					.min();

				if (nextKeywordIdx.isEmpty()) // we're probably at the end of the file
					break;

				endIdx = nextKeywordIdx.getAsInt();
				miscTextBuilder.append(str, cursorIdx, endIdx);
			}

			cursorIdx = endIdx + 1;
		}

		gql.misc = miscTextBuilder.toString();
		return gql;
	}

	/**
	 * Move the iterator to the next index in the string that is not a whitespace
	 *
	 * @param i   the current value of the iterator
	 * @param str the string
	 * @return the next index. {@code Character.isWhitespace(str.charAt(nextIdx))} should be false
	 */
	protected static int ignoreWhitespaces(@NotNull String str, int i) {
		for (; i < str.length() && Character.isWhitespace(str.charAt(i)); ++i) ;
		return i;
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
		return dataTypes.stream()
			.map(Object::toString)
			.collect(Collectors.joining("\n\n"));
	}
}
