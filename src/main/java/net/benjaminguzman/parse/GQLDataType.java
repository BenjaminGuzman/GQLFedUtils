package net.benjaminguzman.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
	 * Just a cache for {@link #alphaName()}
	 * <p>
	 * It may be null if {@link #alphaName()} has not been called yet
	 */
	@Nullable
	protected String alphaName;

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
	 * It may be null in special cases
	 */
	@Nullable
	public abstract GQLKeyword getKeyword();

	@Nullable
	public String getComment() {
		return comment;
	}

	public GQLDataType setComment(@Nullable String comment) {
		// this conditional seems strange, but it is correct actually
		if (comment != null && comment.strip().isEmpty())
			this.comment = null;
		else
			this.comment = comment;
		return this;
	}

	/**
	 * Extracts only the alphanumeric part of the name, starting from index 0
	 *
	 * @return the substring of {@link #getName()} that consists only of alphanumeric characters
	 */
	@NotNull
	public String alphaName() {
		if (alphaName != null)
			return alphaName;

		int nameStartIdx = 0;
		int nameEndIdx = 0;
		for (; nameEndIdx < name.length() && Character.isLetterOrDigit(name.charAt(nameEndIdx)); ++nameEndIdx) ;
		return (alphaName = name.substring(nameStartIdx, nameEndIdx));
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GQLDataType other = (GQLDataType) o;
		return name.equals(other.name) && Objects.equals(getKeyword(), other.getKeyword());
	}

	/**
	 * All deriving classes that return {@code null} for {@link #getKeyword()} MUST override this method
	 */
	@Override
	public int hashCode() {
		if (getKeyword() == null)
			return name.hashCode();
		return Objects.hash(getKeyword(), name);
	}
}
