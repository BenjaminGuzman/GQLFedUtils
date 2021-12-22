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

public class InvalidGQLSyntax extends Exception {
	public InvalidGQLSyntax(@NotNull Class<? extends GQLDataType> gqlDataType, @NotNull String invalidStr,
	                        @NotNull String message) {
		super("\"" + invalidStr + "\" could not be parsed into " + gqlDataType.getSimpleName() + ". " + message);
	}

	public InvalidGQLSyntax(String message) {
		super(message);
	}
}
