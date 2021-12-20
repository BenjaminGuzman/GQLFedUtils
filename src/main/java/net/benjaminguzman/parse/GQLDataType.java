package net.benjaminguzman.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class to represent a GraphQL data type.
 * <p>
 * Data types can be:
 * <p>
 * - enum
 * <p>
 * - scalar
 * <p>
 * - type
 * <p>
 * - input
 * <p>
 * - fields (only for type and input)
 */
public abstract class GQLDataType {
	/**
	 * Comment for this data type
	 * <p>
	 * It is possible the data type doesn't have a comment associated
	 */
	@Nullable
	protected String comment;

	/**
	 * Name for the data type. It is mandatory
	 */
	@NotNull
	protected String name;

	/**
	 * @param name    The identifier (name) for the data type, NOT the keyword to tell specifically
	 *                which data type it is, i.e. NOT input, enum, scalar...
	 * @param comment Comment associated to the data type. It must not include opening and closing delimiters (""")
	 */
	public GQLDataType(@NotNull String name, @Nullable String comment) {
		this.comment = comment;
		this.name = name;
	}

	/**
	 * Calls {@link #GQLDataType(String, String)} with comment as {@code null}
	 *
	 * @param name name for the data type
	 * @see #GQLDataType(String, String)
	 */
	public GQLDataType(@NotNull String name) {
		this(name, null);
	}

	/**
	 * @return A GraphQL specific keyword to tell the GraphQL data type.
	 * <p>
	 * It may be null in the case the data is a field
	 */
	@Nullable
	public abstract GQLKeyword getKeyword();

	@Nullable
	public String getComment() {
		return comment;
	}

	public GQLDataType setComment(@Nullable String comment) {
		if (comment != null && comment.strip().isEmpty())
			this.comment = null;
		else
			this.comment = comment;
		return this;
	}

	@NotNull
	public String getName() {
		return name;
	}

	public GQLDataType setName(@NotNull String name) {
		this.name = name;
		return this;
	}

	/**
	 * Helper to construct the string in {@link #toString()}
	 * <p>
	 * This helper doesn't work for fields, because it creates a {@link StringBuilder} with the following:
	 * <p>
	 * {@code """comment"""\nkeyword name}
	 * <p>
	 * if there is no comment, it just creates
	 * <p>
	 * {@code keyword name}
	 * <p>
	 * {@code keyword} is the value returned by {@link #getKeyword()},
	 * {@code comment} is {@link #comment} and {@code name} is {@link #name}
	 */
	protected StringBuilder toStringTemplateHelper() {
		StringBuilder builder = new StringBuilder(64);

		if (comment != null)
			builder.append(GQL.COMMENT_DELIMITER)
				.append(comment)
				.append(GQL.COMMENT_DELIMITER).append('\n');

		builder.append(this.getKeyword())
			.append(' ')
			.append(name);

		return builder;
	}

	/**
	 * @param indentSize times to repeat {@code indentChar}
	 * @param indentChar indentation char
	 * @return a string with the {@code indentChar} repeated {@code indentSize} times
	 */
	protected String indentationHelper(int indentSize, char indentChar) {
		assert indentSize > 0;
		return String.valueOf(indentChar).repeat(indentSize);
	}
}
