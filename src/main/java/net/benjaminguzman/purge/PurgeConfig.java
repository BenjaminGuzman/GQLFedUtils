package net.benjaminguzman.purge;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class PurgeConfig {
	/**
	 * File from which this configuration was loaded
	 */
	@NotNull
	private final File loadedFrom;

	/**
	 * If one of these patterns is found in a field or operation, it'll be kept
	 * (depending on {@link #secondKeepPatterns})
	 *
	 * If none of these patterns is found in a field or operation, it'll be removed
	 *
	 * Examples of these patterns: {@code @GKeep}, {@code @GateKeep}, {@code @GK}...
	 */
	@NotNull
	private List<String> keepPatterns = Collections.emptyList();

	/**
	 * If any of {@link #keepPatterns} is found in a field or operation AND
	 * any of these second patterns is found in the same line the first pattern coincidence was found,
	 * the field or operation is kept
	 *
	 * If any of {@link #keepPatterns} is found in a field or operation BUT NONE of these patterns is found
	 * in the same line of the first pattern coincidence, the field or operation will be removed
	 */
	@NotNull
	private List<String> secondKeepPatterns = Collections.emptyList();

	public PurgeConfig(@NotNull File loadedFrom) {
		this.loadedFrom = loadedFrom;
	}

	public List<String> getKeepPatterns() {
		return keepPatterns;
	}

	public PurgeConfig setKeepPatterns(List<String> keepPatterns) {
		this.keepPatterns = keepPatterns;
		return this;
	}

	public List<String> getSecondKeepPatterns() {
		return secondKeepPatterns;
	}

	public PurgeConfig setSecondKeepPatterns(List<String> secondKeepPatterns) {
		this.secondKeepPatterns = secondKeepPatterns;
		return this;
	}

	public File getLoadedFrom() {
		return loadedFrom;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PurgeConfig that = (PurgeConfig) o;
		return loadedFrom.equals(that.loadedFrom);
		// files loaded from different locations are inherently distinct
	}

	@Override
	public int hashCode() {
		return loadedFrom.hashCode();
	}

	@Override
	public String toString() {
		return "PurgeConfig{" +
			"loadedFrom=" + loadedFrom +
			", keepPatterns=" + keepPatterns +
			", secondKeepPatterns=" + secondKeepPatterns +
			'}';
	}
}
