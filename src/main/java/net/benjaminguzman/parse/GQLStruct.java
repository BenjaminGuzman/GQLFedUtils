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
import java.util.stream.Collectors;

/**
 * Abstract class to represent either a GraphQL {@code type} or {@code input}
 */
public abstract class GQLStruct extends GQLDataType {
	/**
	 * Fields inside the graphql struct (type or input)
	 */
	@NotNull
	protected List<GQLField> fields = Collections.emptyList();

	/**
	 * @see GQLDataType#GQLDataType(String, String)
	 */
	public GQLStruct(@NotNull String name, @Nullable String comment) {
		super(name, comment);
	}

	/**
	 * @see GQLDataType#GQLDataType(String, String)
	 */
	public GQLStruct(@NotNull String name) {
		super(name);
	}

	/**
	 * Parse fields from the given string
	 * <p>
	 * The given string should have the following structure:
	 * <p>
	 * {@code comments field comment field comment field...}
	 * <p>
	 * Every space can be a finite number of whitespaces.
	 * <p>
	 * Comments are delimited by """
	 *
	 * @param str    string from which fields will be parsed
	 * @param struct reference to the struct containing the fields that will be parsed
	 * @return a list of parsed fields
	 * @throws InvalidGQLSyntax if some field couldn't be parsed because it is invalid
	 */
	@NotNull
	public static List<GQLField> parseFields(@NotNull String str, @NotNull GQLStruct struct) throws InvalidGQLSyntax {
		String comment;
		List<GQLField> fields = new ArrayList<>();
		int cursorIdx = 0;
		int endIdx;

		while ((cursorIdx = GQL.ignoreWhitespaces(str, cursorIdx)) < str.length()) {
			// extract comments
			if (str.startsWith(GQL.COMMENT_DELIMITER, cursorIdx)) {
				int commentEndIdx = str.indexOf(GQL.COMMENT_DELIMITER, cursorIdx + 3);
				comment = str.substring(cursorIdx + 3, commentEndIdx); // +3 to skip the delimiter """

				// move the cursor to the next non whitespace, starting from the end of the comment
				cursorIdx = GQL.ignoreWhitespaces(str, commentEndIdx + 3);
			} else
				comment = null;

			// extract name
			endIdx = cursorIdx + 1;
			while (str.charAt(endIdx) != '(' && str.charAt(endIdx) != ':') ++endIdx;
			String name = str.substring(cursorIdx, endIdx).strip();

			// extract params (if any)
			String params = null;
			if (str.charAt(endIdx) == '(') { // should have params
				cursorIdx = endIdx + 1;
				endIdx = str.indexOf(')', cursorIdx);
				if (endIdx == -1)
					throw new InvalidGQLSyntax(GQLField.class, str, "')' is missing");
				params = str.substring(cursorIdx, endIdx);
			}

			// extract return type
			while (str.charAt(endIdx) != ':') ++endIdx;
			cursorIdx = endIdx + 1;
			endIdx = GQL.lineEndIdx(str, cursorIdx);
			assert endIdx > -1;
			String returnType = str.substring(cursorIdx, endIdx).strip();

			fields.add(new GQLField(name, returnType, comment, struct).setParams(params));
			cursorIdx = endIdx + 1;
		}

		return fields;
	}

	/**
	 * Fields inside the graphql struct (type or input)
	 */
	@NotNull
	public List<GQLField> getFields() {
		return fields;
	}

	/**
	 * @param fields Fields inside the graphql struct (type or input).
	 *               The list can be empty but that's not recommended
	 */
	public GQLStruct setFields(@NotNull List<GQLField> fields) {
		this.fields = fields;
		return this;
	}

	public String toString(int indentSize, char indentChar) {
		StringBuilder builder = this.toStringTemplateHelper();
		// builder already has """comment""" input/type name

		builder.append(" {\n");

		String fieldsString = fields.stream()
			.map(field -> field.toString(indentSize, indentChar))
			.collect(Collectors.joining("\n\n"));

		builder.append(fieldsString).append("\n}");

		return builder.toString();
	}

	@Override
	public String toString() {
		return toString(GQL.DEFAULT_INDENTATION_SIZE, GQL.DEFAULT_INDENTATION_CHAR);
	}
}
