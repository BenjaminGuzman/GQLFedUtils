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

public enum GQLKeyword {
	SCALAR("scalar"),
	ENUM("enum"),
	INPUT("input"),
	TYPE("type"),
	DIRECTIVE("directive"),
	SCHEMA("schema");

	@NotNull
	private final String gqlKeyword;

	GQLKeyword(@NotNull String gqlKeyword) {
		this.gqlKeyword = gqlKeyword;
	}

	@Override
	public String toString() {
		return gqlKeyword;
	}
}
