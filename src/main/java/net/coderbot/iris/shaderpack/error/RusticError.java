package net.coderbot.iris.shaderpack.error;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public class RusticError {
	private final String severity;
	private final String message;
	private final String detailMessage;
	private final String file;
	private final int lineNumber;
	private final String badLine;

	public RusticError(String severity, String message, String detailMessage, String file, int lineNumber, String badLine) {
		this.severity = severity;
		this.message = message;
		this.detailMessage = detailMessage;
		this.file = file;
		this.lineNumber = lineNumber;
		this.badLine = badLine;
	}

    @Override
	public String toString() {
		return severity + ": " + message + "\n"
			+ " --> " + file + ":" + lineNumber + "\n"
			+ "  |\n"
			+ "  | " + badLine + "\n"
			+ "  | " + StringUtils.repeat('^', badLine.length()) + " " + detailMessage + "\n"
			+ "  |";
	}
}
