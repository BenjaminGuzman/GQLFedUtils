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
		int lineEndIdx = GQL.lineEndIdx(str, cursorIdx);
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
