package net.benjaminguzman.purge;

import picocli.CommandLine;

@CommandLine.Command(
	name = "purge",
	description = "Purge a schema. This is done by removing all fields or operations whose associated " +
		"comments contain some specific text."

)
public class Purge implements Runnable {
	@CommandLine.Option(
		names = {"-c", "--config"},
		description = "Path to the config file",
		required = true
	)
	private PurgeConfig config;

	@CommandLine.Option(
		names = {"-h", "--help"},
		usageHelp = true,
		description = "Display a help message"
	)
	private boolean helpRequested;

	@Override
	public void run() {
		System.out.println("Running...");
	}
}
