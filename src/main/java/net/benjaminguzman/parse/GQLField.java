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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GQLField extends GQLDataType {
	/**
	 * Return data type. It is mandatory
	 */
	@NotNull
	private String returnType;

	/**
	 * Parameters for the field
	 */
	@NotNull
	private List<GQLFieldParam> params = Collections.emptyList();

	/**
	 * Original parameters string. {@link #params} is just the result of parsing this string
	 * <p>
	 * The original string is kept just to keep the original format when converting this object to string
	 * <p>
	 * It may be null if there are no params
	 */
	@Nullable
	private String paramsStr;

	/**
	 * {@link GQLStruct} that contains this field
	 */
	@NotNull
	private final GQLStruct parentStruct;

	/**
	 * @param name       identifier (name) of the field
	 * @param returnType return data type
	 * @param comment    comment associated to the field
	 * @see GQLDataType#GQLDataType(String, String)
	 */
	public GQLField(@NotNull String name, @NotNull String returnType, @Nullable String comment,
	                @NotNull GQLStruct parentStruct) {
		super(name, comment);
		this.returnType = returnType;
		this.parentStruct = parentStruct;
	}

	/**
	 * Just calls {@link #getReturnType(boolean)} with false
	 *
	 * @return the return type (not cleaned)
	 */
	@NotNull
	public String getReturnType() {
		return getReturnType(false);
	}

	public GQLField setReturnType(@NotNull String returnType) {
		this.returnType = returnType;
		return this;
	}

	/**
	 * Get the return type
	 *
	 * @param clean if true, modifiers like required (!), array ([]), default ( = x), or anything after
	 *              the first whitespace (like @join__field and stuff) are removed
	 * @return the return type
	 */
	@NotNull
	public String getReturnType(boolean clean) {
		if (!clean)
			return returnType;

		String typeClean = returnType;

		// remove anything after the first whitespace
		int firstSpaceIdx = 0;
		for (;
		     firstSpaceIdx < returnType.length() && !Character.isWhitespace(returnType.charAt(firstSpaceIdx));
		     ++firstSpaceIdx
		)
			;
		if (firstSpaceIdx < returnType.length())
			typeClean = typeClean.substring(0, firstSpaceIdx);

		// remove default value
		int equalIdx = returnType.indexOf('=');
		if (equalIdx > -1)
			typeClean = typeClean.substring(equalIdx);

		return typeClean.replace("!", "") // remove required modifier !
			.replace("[", "") // remove array modifier [
			.replace("]", ""); // remove array modifier ]
	}

	/**
	 * @return Parameters for the field. It may be null if there are no params.
	 */
	@NotNull
	public List<GQLFieldParam> getParams() {
		return params;
	}

	/**
	 * @param paramsStr parameters for the field. It may be null if there are no params.
	 */
	public GQLField setParams(@Nullable String paramsStr) throws InvalidGQLSyntax {
		if (paramsStr == null)
			return this;
		this.paramsStr = paramsStr;
		params = GQLFieldParam.parseParams(paramsStr, this);
		return this;
	}

	/**
	 * @return true if this field has parameters
	 */
	public boolean hasParams() {
		return !params.isEmpty();
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

		if (paramsStr != null && !params.isEmpty())
			builder.append('(').append(paramsStr).append(')');

		builder.append(": ").append(returnType);

		return builder.toString();
	}

	@Override
	public String toString() {
		return toString(GQL.DEFAULT_INDENTATION_SIZE, GQL.DEFAULT_INDENTATION_CHAR);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		GQLField field = (GQLField) o;
		return name.equals(field.name)
			&& returnType.equals(field.returnType)
			&& parentStruct.equals(field.parentStruct)
			&& Objects.equals(paramsStr, field.paramsStr);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, returnType, parentStruct, paramsStr);
	}

	public static class GQLFieldParam extends GQLDataType {
		/**
		 * GraphQL type for this parameter
		 */
		@NotNull
		private final String type;

		@NotNull
		private final GQLField parentField;

		public GQLFieldParam(@NotNull String name, @Nullable String comment, @NotNull String type,
		                     @NotNull GQLField parentField) {
			super(name, comment);
			this.type = type;
			this.parentField = parentField;
		}

		/**
		 * Parse parameters from the given string
		 *
		 * @param str         string containing the parameters to be parsed
		 * @param parentField {@link GQLField} that contains this parameter
		 * @return a list of {@link GQLFieldParam}
		 */
		public static List<GQLFieldParam> parseParams(@NotNull String str, @NotNull GQLField parentField) throws InvalidGQLSyntax {
			List<GQLFieldParam> params = new ArrayList<>();

			int cursorIdx = 0;
			String comment;
			while ((cursorIdx = GQL.ignoreWhitespaces(str, cursorIdx)) < str.length()) {
				// parse comments
				if (str.startsWith(GQL.COMMENT_DELIMITER, cursorIdx)) {
					int commentEndIdx = str.indexOf(GQL.COMMENT_DELIMITER, cursorIdx + 3);
					comment = str.substring(cursorIdx + 3, commentEndIdx); // +3 to skip """

					// move the cursor to the next non whitespace
					cursorIdx = GQL.ignoreWhitespaces(str, commentEndIdx + 3);
				} else
					comment = null;

				// parse name of the param
				int semicolonIdx = str.indexOf(':', cursorIdx);
				if (semicolonIdx == -1)
					throw new InvalidGQLSyntax(GQLFieldParam.class, str, "':' is missing");
				String name = str.substring(cursorIdx, semicolonIdx).strip();
				cursorIdx = semicolonIdx + 1;

				// parse its type
				cursorIdx = GQL.ignoreWhitespaces(str, cursorIdx);
				int endTypeIdx = cursorIdx + 1;
				// the param ends at a whitespace
				for (;
				     endTypeIdx < str.length() && !Character.isWhitespace(str.charAt(endTypeIdx));
				     ++endTypeIdx
				)
					;
				String type = str.substring(cursorIdx, endTypeIdx).strip();

				params.add(new GQLFieldParam(name, comment, type, parentField));
				cursorIdx = endTypeIdx; // endTypeIdx should be a whitespace or EOL
			}

			return params;
		}

		/**
		 * Get the type of the parameter
		 *
		 * @param clean if true, modifiers like required (!), array ([]), default ( = x) are removed
		 * @return the type
		 */
		public String getType(boolean clean) {
			if (!clean)
				return type;

			String typeClean = type;

			// remove default value
			int equalIdx = type.indexOf('=');
			if (equalIdx > -1)
				typeClean = typeClean.substring(equalIdx);

			return typeClean.replace("!", "") // remove required modifier !
				.replace("[", "") // remove array modifier [
				.replace("]", ""); // remove array modifier ]
		}

		/**
		 * @return A GraphQL specific keyword to tell the GraphQL data type.
		 * <p>
		 * It may be null in the case the data is a field
		 */
		@Override
		public @Nullable GQLKeyword getKeyword() {
			return null;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			GQLFieldParam that = (GQLFieldParam) o;
			return type.equals(that.type) && parentField.equals(that.parentField) && name.equals(that.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(parentField, name, type);
		}
	}
}
