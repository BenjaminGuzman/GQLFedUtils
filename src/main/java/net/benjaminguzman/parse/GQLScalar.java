package net.benjaminguzman.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GQLScalar extends GQLDataType {
	/**
	 * @see GQLDataType#GQLDataType(String, String)
	 */
	public GQLScalar(@NotNull String name, @Nullable String comment) {
		super(name, comment);
	}

	/**
	 * @see GQLDataType#GQLDataType(String)
	 */
	public GQLScalar(@NotNull String name) {
		super(name);
	}

	/**
	 * Parses a graphql scalar
	 *
	 * @param str     the string from which the scalar will be parsed
	 * @param comment comment related to the scalar
	 * @return a {@link GQLScalar} object
	 * @throws InvalidGQLSyntax if the string has invalid GraphQL syntax
	 */
	@NotNull
	public static GQLScalar parse(@NotNull String str, @Nullable String comment) throws InvalidGQLSyntax {
		int cursorIdx = GQL.ignoreWhitespaces(str, 0);
		cursorIdx += GQLKeyword.SCALAR.toString().length(); // exclude "scalar" from processing

		// parse name
		int lineEndIdx = str.indexOf('\n', cursorIdx);
		if (lineEndIdx == -1)
			lineEndIdx = str.length();
		String name = str.substring(cursorIdx, lineEndIdx).strip();

		return new GQLScalar(name, comment);
	}

	@Override
	public @Nullable GQLKeyword getKeyword() {
		return GQLKeyword.SCALAR;
	}

	public String toString() {
		StringBuilder builder = this.toStringTemplateHelper();
		// builder already has """comment""" scalar name. No more operations are required

		return builder.toString();
	}
}
