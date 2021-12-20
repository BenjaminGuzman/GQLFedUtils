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
