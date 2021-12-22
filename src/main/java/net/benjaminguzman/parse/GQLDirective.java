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
