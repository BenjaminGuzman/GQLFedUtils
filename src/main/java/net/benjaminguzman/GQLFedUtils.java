package net.benjaminguzman;

import net.benjaminguzman.purge.Purge;
import picocli.CommandLine;

@CommandLine.Command(
	name = "gqlfedutils",
	description = "gqlfedutils is a set of utilities to help you manage GraphQL Federated services.",
	subcommands = {
		Purge.class
	},
	version = "gqlfedutils v0.1",
	header = "Copyright (c) 2021. Benjamín Antonio Velasco Guzmán\n" +
		"This program comes with ABSOLUTELY NO WARRANTY.\n" +
		"This is free software, and you are welcome to redistribute it\n" +
		"under certain conditions.\n" +
		"License GPLv3: GNU GPL version 3 <http://gnu.org/licenses/gpl.html>\n",
	mixinStandardHelpOptions = true
)
public class GQLFedUtils {
	public static void main(String... args) {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%4$-7s] [%1$tF %1$tT] %5$s %n");
		new CommandLine(new GQLFedUtils()).execute(args);
	}
}
