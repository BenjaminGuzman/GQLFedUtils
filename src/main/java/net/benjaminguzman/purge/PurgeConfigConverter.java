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

package net.benjaminguzman.purge;

import net.benjaminguzman.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import picocli.CommandLine;

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
