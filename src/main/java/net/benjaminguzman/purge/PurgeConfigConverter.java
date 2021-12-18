package net.benjaminguzman.purge;

import picocli.CommandLine;

public class PurgeConfigConverter implements CommandLine.ITypeConverter<PurgeConfig> {
	/**
	 * Converts the specified command line argument value to some domain object.
	 *
	 * @param value the command line argument String value
	 * @return the resulting domain object
	 * @throws Exception               an exception detailing what went wrong during the conversion.
	 *                                 Any exception thrown from this method will be caught and shown to the end
	 *                                 user.
	 *                                 An example error message shown to the end user could look something like
	 *                                 this:
	 *                                 {@code Invalid value for option '--some-option': cannot convert
	 *                                 'xxxinvalidinput' to SomeType (java.lang.IllegalArgumentException: Invalid
	 *                                 format: must be 'x:y:z' but was 'xxxinvalidinput')}
	 * @throws CommandLine.TypeConversionException throw this exception to have more control over the error
	 *                                 message that is shown to the end user when type conversion fails.
	 *                                 An example message shown to the user could look like this:
	 *                                 {@code Invalid value for option '--some-option': Invalid format: must be
	 *                                 'x:y:z' but was 'xxxinvalidinput'}
	 */
	@Override
	public PurgeConfig convert(String value) throws Exception {
		// TODO load file and create PurgeConfig
		return null;
	}
}
