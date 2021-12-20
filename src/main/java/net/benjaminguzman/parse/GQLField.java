package net.benjaminguzman.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GQLField extends GQLDataType {
	/**
	 * Return data type. It is mandatory
	 */
	@NotNull
	private String returnType;

	/**
	 * Parameters for the field. It may be null if there are no params.
	 * <p>
	 * For now this is a string for sake of simplicity, but this may change later
	 */
	@Nullable
	private String params;

	/**
	 * @param name       identifier (name) of the field
	 * @param returnType return data type
	 * @param comment    comment associated to the field
	 * @see GQLDataType#GQLDataType(String, String)
	 */
	public GQLField(@NotNull String name, @NotNull String returnType, @Nullable String comment) {
		super(name, comment);
		this.returnType = returnType;
	}

	/**
	 * @param name       identifier (name) of the field
	 * @param returnType return data type
	 * @see GQLDataType#GQLDataType(String)
	 */
	public GQLField(@NotNull String name, @NotNull String returnType) {
		super(name);
		this.returnType = returnType;
	}

	/**
	 * Parse a graphql field
	 * <p>
	 * A field has the following structure:
	 * <p>
	 * {@code name(parameters): returnType}
	 * <p>
	 * or
	 * <p>
	 * {@code name: returnType}
	 *
	 * @param str     the string from which the field will be parsed
	 * @param comment comment for the field
	 * @return a {@link GQLField} object
	 * @throws InvalidGQLSyntax in case the string has an invalid graphql syntax
	 */
	public static GQLField parse(@NotNull String str, @Nullable String comment) throws InvalidGQLSyntax {
		int startIdx = GQL.ignoreWhitespaces(str, 0);

		// extract the name
		int endIdx = startIdx + 1;
		while (str.charAt(endIdx) != '(' && str.charAt(endIdx) != ':') ++endIdx;
		String name = str.substring(startIdx, endIdx).strip();

		// extract params (if any)
		String params = null;
		if (str.charAt(endIdx) == '(') { // should have params
			startIdx = endIdx + 1;
			endIdx = str.indexOf(')', startIdx);
			if (endIdx == -1)
				throw new InvalidGQLSyntax(GQLField.class, str, "')' is missing");
			params = str.substring(startIdx, endIdx).strip();
		}

		// extract return type
		while (str.charAt(endIdx) != ':') ++endIdx;
		startIdx = endIdx + 1;
		endIdx = GQL.lineEndIdx(str, startIdx);
		assert endIdx > -1;
		String returnType = str.substring(startIdx, endIdx).strip();

		return new GQLField(name, returnType, comment).setParams(params);
	}

	@NotNull
	public String getReturnType() {
		return returnType;
	}

	public GQLField setReturnType(@NotNull String returnType) {
		this.returnType = returnType;
		return this;
	}

	/**
	 * @return Parameters for the field. It may be null if there are no params.
	 * <p>
	 * For now this is a string for sake of simplicity, but this may change later
	 */
	@Nullable
	public String getParams() {
		return params;
	}

	/**
	 * @param params parameters for the field. It may be null if there are no params.
	 *               <p>
	 *               For now this is a string for sake of simplicity, but this may change later
	 */
	public GQLField setParams(@Nullable String params) {
		this.params = params;
		return this;
	}

	/**
	 * @return {@code null} because a graphql field is not identified by any special keyword
	 */
	@Override
	public @Nullable GQLKeyword getKeyword() {
		return null;
	}

	public String toString(int indentSize, char indentChar) {
		StringBuilder builder = new StringBuilder(64);
		String indent = indentationHelper(indentSize, indentChar);

		if (comment != null)
			builder.append(indent).append(GQL.COMMENT_DELIMITER)
				.append(comment) // the comment must have already some indentation
				.append(GQL.COMMENT_DELIMITER).append('\n');

		builder.append(indent).append(name);

		if (params != null)
			builder.append('(').append(params).append(')');

		builder.append(": ").append(returnType);

		return builder.toString();
	}

	@Override
	public String toString() {
		return toString(GQL.DEFAULT_INDENTATION_SIZE, GQL.DEFAULT_INDENTATION_CHAR);
	}
}
