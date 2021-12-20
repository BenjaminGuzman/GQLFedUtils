package net.benjaminguzman.purge;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PurgeConfig {
	/**
	 * If one of these patterns is found in a field/operation/type, it'll be kept
	 * (depending on {@link #secondKeepPatterns})
	 * <p>
	 * If none of these patterns is found in a field/operation/type, it'll be removed
	 * <p>
	 * Examples of these patterns: {@code @GKeep}, {@code @GateKeep}, {@code @GK}...
	 */
	@NotNull
	private List<String> keepPatterns;

	/**
	 * If any of {@link #keepPatterns} is found in a field/operation/type AND
	 * any of these second patterns is found in the same line the first pattern coincidence was found,
	 * the field/operation/type is kept
	 * <p>
	 * If any of {@link #keepPatterns} is found in a field/operation/type BUT NONE of these patterns is found
	 * in the same line of the first pattern coincidence, the field/operation/type will be removed
	 */
	@NotNull
	private List<String> secondKeepPatterns;

	public PurgeConfig(@NotNull List<String> keepPatterns, @NotNull List<String> secondKeepPatterns) {
		this.keepPatterns = keepPatterns;
		this.secondKeepPatterns = secondKeepPatterns;
	}

	public PurgeConfig(@NotNull List<String> keepPatterns) {
		this(keepPatterns, Collections.emptyList());
	}

	/**
	 * No args constructor INTENDED TO BE USED ONLY by SnakeYAML
	 * <p>
	 * Prefer using other constructor
	 *
	 * @see #PurgeConfig(List)
	 * @see #PurgeConfig(List, List)
	 */
	public PurgeConfig() {
		this(Collections.emptyList());
	}

	public @NotNull List<String> getKeepPatterns() {
		return keepPatterns;
	}

	public void setKeepPatterns(@NotNull List<String> keepPatterns) {
		this.keepPatterns = keepPatterns;
	}

	public @NotNull List<String> getSecondKeepPatterns() {
		return secondKeepPatterns;
	}

	public void setSecondKeepPatterns(@NotNull List<String> secondKeepPatterns) {
		this.secondKeepPatterns = secondKeepPatterns;
	}

	@Override
	public String toString() {
		return "PurgeConfig{" +
			"keepPatterns=" + keepPatterns +
			", secondKeepPatterns=" + secondKeepPatterns +
			'}';
	}
}
