package net.benjaminguzman.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GQLSchema extends GQLDataType {
	/**
	 * @see GQLDataType#GQLDataType(String, String)
	 */
	public GQLSchema(@NotNull String name, @Nullable String comment) {
		super(name, comment);
	}

	/**
	 * Parse a graphql schema
	 *
	 * @param str     the string from which the schema will be parsed
	 * @param comment comment for the schema
	 * @return a {@link GQLSchema} object
	 * @throws InvalidGQLSyntax in case the string has an invalid graphql syntax
	 */
	public static GQLSchema parse(@NotNull String str, @Nullable String comment) throws InvalidGQLSyntax {
		int startIdx = GQL.ignoreWhitespaces(str, 0);
		startIdx += GQLKeyword.SCHEMA.toString().length(); // ignore "schema"
		startIdx = GQL.ignoreWhitespaces(str, startIdx);

		int endIdx = str.indexOf('}', startIdx);
		if (endIdx == -1)
			throw new InvalidGQLSyntax(GQLSchema.class, str, "'}' is missing");
		return new GQLSchema(str.substring(startIdx, endIdx + 1), comment);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(64);

		if (comment != null)
			builder.append(GQL.COMMENT_DELIMITER)
				.append(comment)
				.append(GQL.COMMENT_DELIMITER).append('\n');

		builder.append(this.getKeyword())
			.append('\n')
			.append(name);

		return builder.toString();
	}

	@Override
	public @Nullable GQLKeyword getKeyword() {
		return GQLKeyword.SCHEMA;
	}
}
