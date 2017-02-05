package it.albertus.router.client.engine;

import java.util.logging.Formatter;

public class FileHandlerBuilder {

	private final String pattern;
	private final int limit;
	private final int count;
	private final boolean append;
	private final Formatter formatter;

	public FileHandlerBuilder(final String pattern, final int limit, final int count, final boolean append, final Formatter formatter) {
		this.pattern = pattern;
		this.limit = limit;
		this.count = count;
		this.append = append;
		this.formatter = formatter;
	}

	public String getPattern() {
		return pattern;
	}

	public int getLimit() {
		return limit;
	}

	public int getCount() {
		return count;
	}

	public boolean isAppend() {
		return append;
	}

	public Formatter getFormatter() {
		return formatter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (append ? 1231 : 1237);
		result = prime * result + count;
		result = prime * result + limit;
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof FileHandlerBuilder)) {
			return false;
		}
		FileHandlerBuilder other = (FileHandlerBuilder) obj;
		if (append != other.append) {
			return false;
		}
		if (count != other.count) {
			return false;
		}
		if (limit != other.limit) {
			return false;
		}
		if (pattern == null) {
			if (other.pattern != null) {
				return false;
			}
		}
		else if (!pattern.equals(other.pattern)) {
			return false;
		}
		return true;
	}

}
