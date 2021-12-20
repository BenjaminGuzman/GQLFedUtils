package net.benjaminguzman.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GQLDirective extends GQLDataType {
	/**
	 * @see GQLDataType#GQLDataType(String, String)
	 */
	public GQLDirective(@NotNull String name, @Nullable String comment) {
		super(name, comment);
	}

	/**
	 * Parse a graphql directive
	 *
	 * @param str     the string from which the directive will be parsed
	 * @param comment comment for the directive
	 * @return a {@link GQLDirective} object
	 * @throws InvalidGQLSyntax in case the string has an invalid graphql syntax
	 */
	public static GQLDirective parse(@NotNull String str, @Nullable String comment) throws InvalidGQLSyntax {
		int startIdx = GQL.ignoreWhitespaces(str, 0);
		startIdx += GQLKeyword.DIRECTIVE.toString().length(); // ignore "directive"
		startIdx = GQL.ignoreWhitespaces(str, startIdx);

		int endIdx = GQL.lineEndIdx(str, startIdx);
		return new GQLDirective(str.substring(startIdx, endIdx), comment);
	}

	@Override
	public String toString() {
		return toStringTemplateHelper().toString();
	}

	@Override
	public @Nullable GQLKeyword getKeyword() {
		return GQLKeyword.DIRECTIVE;
	}
}
