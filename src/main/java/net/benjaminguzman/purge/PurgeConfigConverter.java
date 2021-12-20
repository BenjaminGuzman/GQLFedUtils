package net.benjaminguzman.purge;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import picocli.CommandLine;

import javax.naming.ConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class PurgeConfigConverter implements CommandLine.ITypeConverter<PurgeConfig> {
	private static final Logger LOGGER = Logger.getLogger(PurgeConfigConverter.class.getName());

	/**
	 * Converts the specified command line argument value to some domain object.
	 * More specifically, this reads the given file and parses it to a {@link PurgeConfig} object
	 *
	 * @param fileStr the command line argument
	 * @return the resulting domain object
	 * @throws IOException            if there was an error when reading the file
	 * @throws ConfigurationException if the file has an invalid configuration
	 */
	@Override
	public PurgeConfig convert(@NotNull String fileStr) throws IOException, ConfigurationException {
		// read and parse the yaml file
		try (BufferedReader bufferedReader = Files.newBufferedReader(Path.of(fileStr))) {
			PurgeConfig config = new Yaml(new Constructor(PurgeConfig.class)).load(bufferedReader);
			if (config.getKeepPatterns().isEmpty())
				throw new ConfigurationException(
					"Configuration file should contain a key named 'keepPatterns'"
				);

			LOGGER.config("Loaded configuration from " + fileStr + ": " + config);
			return config;
		}
	}
}
