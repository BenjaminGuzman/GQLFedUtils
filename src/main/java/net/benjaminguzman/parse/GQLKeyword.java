package net.benjaminguzman.parse;

import org.jetbrains.annotations.NotNull;

public enum GQLKeyword {
	SCALAR("scalar"),
	ENUM("enum"),
	INPUT("input"),
	TYPE("type");

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
