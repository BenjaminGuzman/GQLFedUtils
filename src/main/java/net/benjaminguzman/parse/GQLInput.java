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

		gqlInput.setFields(GQLInput.parseFields(str.substring(openingBraceIdx + 1, closingBraceIdx),
			gqlInput));
		return gqlInput;
	}

	@Override
	public @Nullable GQLKeyword getKeyword() {
		return GQLKeyword.INPUT;
	}
}
