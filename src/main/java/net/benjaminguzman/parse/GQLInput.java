package net.benjaminguzman.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GQLInput extends GQLStruct {
	/**
	 * @see GQLDataType#GQLDataType(String, String)
	 */
	public GQLInput(@NotNull String name, @Nullable String comment) {
		super(name, comment);
	}

	/**
	 * @see GQLDataType#GQLDataType(String, String)
	 */
	public GQLInput(@NotNull String name) {
		super(name);
	}

	/**
	 * Parses a graphql input
	 *
	 * @param str     the string from which the input will be parsed
	 * @param comment comment related to the input
	 * @return a {@link GQLInput} object
	 * @throws InvalidGQLSyntax if the string has invalid GraphQL syntax
	 */
	@NotNull
	public static GQLInput parse(@NotNull String str, @Nullable String comment) throws InvalidGQLSyntax {
		int cursorIdx = GQL.ignoreWhitespaces(str, 0);
		cursorIdx += GQLKeyword.INPUT.toString().length(); // exclude "input" from processing

		// parse name
		// it should be enclosed by "type" and "{"
		int openingBraceIdx = str.indexOf('{', cursorIdx);
		if (openingBraceIdx == -1)
			throw new InvalidGQLSyntax(GQLInput.class, str, "'{' is missing");
		String name = str.substring(cursorIdx, openingBraceIdx).strip();

		GQLInput gqlInput = new GQLInput(name, comment);

		// parse fields inside the type
		int closingBraceIdx = str.indexOf('}', openingBraceIdx);
		if (closingBraceIdx == -1)
			throw new InvalidGQLSyntax(GQLInput.class, str, "'}' is missing");

		gqlInput.setFields(GQLInput.parseFields(str.substring(openingBraceIdx + 1, closingBraceIdx)));
		return gqlInput;
	}

	@Override
	public @Nullable GQLKeyword getKeyword() {
		return GQLKeyword.INPUT;
	}
}
