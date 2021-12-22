package net.benjaminguzman.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GQLEnum extends GQLDataType {
	/**
	 * Possible values for the enum
	 */
	@NotNull
	private List<EnumValue> values = new ArrayList<>();

	/**
	 * @see GQLDataType#GQLDataType(String, String)
	 */
	public GQLEnum(@NotNull String name, @Nullable String comment) {
		super(name, comment);
	}

	/**
	 * @see GQLDataType#GQLDataType(String)
	 */
	public GQLEnum(@NotNull String name) {
		super(name);
	}

	/**
	 * Parses a graphql enum
	 *
	 * @param str     the string from which the enum will be parsed
	 * @param comment comment related to the enum
	 * @return a {@link GQLEnum} object
	 * @throws InvalidGQLSyntax if the string has invalid GraphQL syntax
	 */
	public static GQLEnum parse(@NotNull String str, @Nullable String comment) throws InvalidGQLSyntax {
		int cursorIdx = GQL.ignoreWhitespaces(str, 0);
		cursorIdx += GQLKeyword.ENUM.toString().length(); // exclude "enum" from processing

		// parse name
		// it should be enclosed by "enum" and "{"
		int openingBraceIdx = str.indexOf('{', cursorIdx);
		if (openingBraceIdx == -1)
			throw new InvalidGQLSyntax(GQLEnum.class, str, "'{' is missing");
		String name = str.substring(cursorIdx, openingBraceIdx).strip();
		cursorIdx = openingBraceIdx + 1;

		GQLEnum gqlEnum = new GQLEnum(name, comment);

		int closingBraceIdx = str.lastIndexOf('}'); // do it backwards for efficiency
		if (closingBraceIdx == -1)
			throw new InvalidGQLSyntax(GQLEnum.class, str, "'}' is missing");

		// parse enum values
		String valueComment;
		int lineEndIdx;
		while (cursorIdx < closingBraceIdx) {
			cursorIdx = GQL.ignoreWhitespaces(str, cursorIdx);
			if (str.startsWith(GQL.COMMENT_DELIMITER, cursorIdx)) { // parse comment
				int commentEndIdx = str.indexOf(GQL.COMMENT_DELIMITER, cursorIdx + 3);
				valueComment = str.substring(cursorIdx + 3, commentEndIdx); // +3 to skip the
				// delimiter """

				// move the cursor to the next non whitespace, starting from the end of the comment
				cursorIdx = GQL.ignoreWhitespaces(str, commentEndIdx + 3);
			} else
				valueComment = null;

			lineEndIdx = GQL.lineEndIdx(str, cursorIdx);
			gqlEnum.values.add(new EnumValue(str.substring(cursorIdx, lineEndIdx).strip(), valueComment));
			cursorIdx = lineEndIdx + 1;
		}

		return gqlEnum;
	}

	@NotNull
	public List<EnumValue> getValues() {
		return values;
	}

	public GQLEnum setValues(@NotNull List<EnumValue> values) {
		this.values = values;
		return this;
	}

	@Override
	public @Nullable GQLKeyword getKeyword() {
		return GQLKeyword.ENUM;
	}

	public String toString(int indentSize, char indentChar) {
		StringBuilder builder = this.toStringTemplateHelper();
		// builder already has """comment""" enum name

		builder.append(" {\n")
			.append(values.stream().map(EnumValue::toString).collect(Collectors.joining("\n\n")))
			.append("\n}");

		return builder.toString();
	}

	@Override
	public String toString() {
		return toString(GQL.DEFAULT_INDENTATION_SIZE, GQL.DEFAULT_INDENTATION_CHAR);
	}

	public static class EnumValue extends GQLDataType {
		public EnumValue(@NotNull String name, @Nullable String comment) {
			super(name, comment);
		}

		public String toString(int indentSize, char indentChar) {
			StringBuilder builder = new StringBuilder();
			String indent = this.indentationHelper(indentSize, indentChar);

			if (comment != null)
				builder.append(indent).append(GQL.COMMENT_DELIMITER)
					.append(comment)
					.append(GQL.COMMENT_DELIMITER).append('\n');

			builder.append(indent).append(name);

			return builder.toString();
		}

		@Override
		public String toString() {
			return toString(GQL.DEFAULT_INDENTATION_SIZE, GQL.DEFAULT_INDENTATION_CHAR);
		}

		@Override
		public @Nullable GQLKeyword getKeyword() {
			return null;
		}

		@Override
		public int hashCode() {
			return ("enum" + name).hashCode();
		}
	}
}
