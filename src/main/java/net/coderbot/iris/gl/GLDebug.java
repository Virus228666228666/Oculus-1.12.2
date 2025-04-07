/*
 * Copyright LWJGL. All rights reserved. Modified by IMS for use in Iris (net.coderbot.iris.gl).
 * License terms: https://www.lwjgl.org/license
 */

package net.coderbot.iris.gl;

import java.io.PrintStream;
import java.nio.IntBuffer;
import java.util.function.Consumer;

import net.coderbot.iris.IrisLogging;
import org.lwjgl.opengl.*;

import net.coderbot.iris.Iris;

public final class GLDebug {
	/**
	 * Sets up debug callbacks
	 * @return 0 for failure, 1 for success, 2 for restart required.
	 */
	private static KHRDebugCallback callbackKHR;
	private static ARBDebugOutputCallback callbackARB;
	private static AMDDebugOutputCallback callbackAMD;

	public static int setupDebugMessageCallback() {
		return setupDebugMessageCallback(System.err);
	}

	private static void trace(Consumer<String> output) {
		/*
		 * We can not just use a fixed stacktrace element offset, because some methods
		 * are intercepted and some are not. So, check the package name.
		 */
		StackTraceElement[] elems = filterStackTrace(new Throwable(), 4).getStackTrace();
		for (StackTraceElement ste : elems) {
			output.accept(ste.toString());
		}
	}

	public static Throwable filterStackTrace(Throwable throwable, int offset) {
		StackTraceElement[] elems = throwable.getStackTrace();
		StackTraceElement[] filtered = new StackTraceElement[elems.length];
		int j = 0;
		for (int i = offset; i < elems.length; i++) {
			String className = elems[i].getClassName();
			if (className == null) {
				className = "";
			}
			filtered[j++] = elems[i];
		}
		StackTraceElement[] newElems = new StackTraceElement[j];
		System.arraycopy(filtered, 0, newElems, 0, j);
		throwable.setStackTrace(newElems);
		return throwable;
	}

	private static void printTrace(PrintStream stream) {
		trace(new Consumer<String>() {
			boolean first = true;

			public void accept(String str) {
				if (first) {
					printDetail(stream, "Stacktrace", str);
					first = false;
				} else {
					printDetailLine(stream, "Stacktrace", str);
				}
			}
		});
	}

	public static int setupDebugMessageCallback(PrintStream stream) {
		ContextCapabilities caps = GLContext.getCapabilities();
		if (caps.GL_KHR_debug) {
			callbackKHR = new KHRDebugCallback((source, type, id, severity, message) -> {
				stream.println("[LWJGL] KHR_debug message");
				stream.println("ID: " + String.format("0x%X", id));
				stream.println("Source: " + getDebugSource(source));
				stream.println("Type: " + getDebugType(type));
				stream.println("Severity: " + getDebugSeverity(severity));
				stream.println("Message: " + message);
			});
			KHRDebug.glDebugMessageControl(4352, 4352, GL43.GL_DEBUG_SEVERITY_HIGH, (IntBuffer)null, true);
			KHRDebug.glDebugMessageControl(4352, 4352, GL43.GL_DEBUG_SEVERITY_MEDIUM, (IntBuffer)null, false);
			KHRDebug.glDebugMessageControl(4352, 4352, GL43.GL_DEBUG_SEVERITY_LOW, (IntBuffer)null, false);
			KHRDebug.glDebugMessageControl(4352, 4352, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer)null, false);
			KHRDebug.glDebugMessageCallback(callbackKHR);
			if (caps.OpenGL30 && (GL11.glGetInteger(33310) & 2) == 0) {
				Iris.logger.warn("[GL] Warning: A non-debug context may not produce any debug output.");
				GL11.glEnable(37600);
				return 2;
			}
			return 1;
		} else if (caps.GL_ARB_debug_output) {
			callbackARB = new ARBDebugOutputCallback((source, type, id, severity, message) -> {
				stream.println("[LWJGL] ARB_debug_output message");
				stream.println("ID: " + String.format("0x%X", id));
				stream.println("Source: " + getSourceARB(source));
				stream.println("Type: " + getTypeARB(type));
				stream.println("Severity: " + getSeverityARB(severity));
				stream.println("Message: " + message);
            });
			ARBDebugOutput.glDebugMessageControlARB(4352, 4352, GL43.GL_DEBUG_SEVERITY_HIGH, (IntBuffer)null, true);
			ARBDebugOutput.glDebugMessageControlARB(4352, 4352, GL43.GL_DEBUG_SEVERITY_MEDIUM, (IntBuffer)null, false);
			ARBDebugOutput.glDebugMessageControlARB(4352, 4352, GL43.GL_DEBUG_SEVERITY_LOW, (IntBuffer)null, false);
			ARBDebugOutput.glDebugMessageControlARB(4352, 4352, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer)null, false);
			ARBDebugOutput.glDebugMessageCallbackARB(callbackARB);
			return 1;
		} else if (caps.GL_AMD_debug_output) {
			callbackAMD = new AMDDebugOutputCallback((id, category, severity, message) -> {
				stream.println("[LWJGL] AMD_debug_output message");
				stream.println("ID: " + String.format("0x%X", id));
				stream.println("Category: " + getCategoryAMD(category));
				stream.println("Severity: " + getSeverityAMD(severity));
				stream.println("Message: " + message);
            });
			AMDDebugOutput.glDebugMessageEnableAMD(0, GL43.GL_DEBUG_SEVERITY_HIGH, (IntBuffer)null, true);
			AMDDebugOutput.glDebugMessageEnableAMD(0, GL43.GL_DEBUG_SEVERITY_MEDIUM, (IntBuffer)null, false);
			AMDDebugOutput.glDebugMessageEnableAMD(0, GL43.GL_DEBUG_SEVERITY_LOW, (IntBuffer)null, false);
			AMDDebugOutput.glDebugMessageEnableAMD(0, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer)null, false);
			AMDDebugOutput.glDebugMessageCallbackAMD(callbackAMD);
			return 1;
		} else {
			Iris.logger.info("[GL] No debug output implementation is available, cannot return debug info.");
			return 0;
		}
	}

	public static int disableDebugMessages() {
		ContextCapabilities caps = GLContext.getCapabilities();
		if (caps.OpenGL43) {
			GL43.glDebugMessageCallback(null);
			return 1;
		} else if (caps.GL_KHR_debug) {
			KHRDebug.glDebugMessageCallback(null);
			if (caps.OpenGL30 && (GL11.glGetInteger(33310) & 2) == 0) {
				GL11.glDisable(37600);
			}
			return 1;
		} else if (caps.GL_ARB_debug_output) {
			ARBDebugOutput.glDebugMessageCallbackARB(null);
			return 1;
		} else if (caps.GL_AMD_debug_output) {
			AMDDebugOutput.glDebugMessageCallbackAMD(null);
			return 1;
		} else {
			Iris.logger.info("[GL] No debug output implementation is available, cannot disable debug info.");
			return 0;
		}
	}

	private static void printDetail(PrintStream stream, String type, String message) {
		stream.printf("\t%s: %s\n", type, message);
	}

	private static void printDetailLine(PrintStream stream, String type, String message) {
		stream.append("    ");
		for (int i = 0; i < type.length(); i++) {
			stream.append(" ");
		}
		stream.append(message).append("\n");
	}

	private static String getDebugSource(int source) {
		switch(source) {
			case 33350:
				return "API";
			case 33351:
				return "WINDOW SYSTEM";
			case 33352:
				return "SHADER COMPILER";
			case 33353:
				return "THIRD PARTY";
			case 33354:
				return "APPLICATION";
			case 33355:
				return "OTHER";
			default:
				return String.format("%s [0x%X]", "Unknown", source);
		}
	}

	private static String getDebugType(int type) {
		switch(type) {
			case 33356:
				return "ERROR";
			case 33357:
				return "DEPRECATED BEHAVIOR";
			case 33358:
				return "UNDEFINED BEHAVIOR";
			case 33359:
				return "PORTABILITY";
			case 33360:
				return "PERFORMANCE";
			case 33361:
				return "OTHER";
			case 33384:
				return "MARKER";
			default:
				return String.format("%s [0x%X]", "Unknown", type);
		}
	}

	private static String getDebugSeverity(int severity) {
		switch(severity) {
			case 33387:
				return "NOTIFICATION";
			case 37190:
				return "HIGH";
			case 37191:
				return "MEDIUM";
			case 37192:
				return "LOW";
			default:
				return String.format("%s [0x%X]", "Unknown", severity);
		}
	}

	private static String getSourceARB(int source) {
		switch(source) {
			case 33350:
				return "API";
			case 33351:
				return "WINDOW SYSTEM";
			case 33352:
				return "SHADER COMPILER";
			case 33353:
				return "THIRD PARTY";
			case 33354:
				return "APPLICATION";
			case 33355:
				return "OTHER";
			default:
				return String.format("%s [0x%X]", "Unknown", source);
		}
	}

	private static String getTypeARB(int type) {
		switch(type) {
			case 33356:
				return "ERROR";
			case 33357:
				return "DEPRECATED BEHAVIOR";
			case 33358:
				return "UNDEFINED BEHAVIOR";
			case 33359:
				return "PORTABILITY";
			case 33360:
				return "PERFORMANCE";
			case 33361:
				return "OTHER";
			default:
				return String.format("%s [0x%X]", "Unknown", type);
		}
	}

	private static String getSeverityARB(int severity) {
		switch(severity) {
			case 37190:
				return "HIGH";
			case 37191:
				return "MEDIUM";
			case 37192:
				return "LOW";
			default:
				return String.format("%s [0x%X]", "Unknown", severity);
		}
	}

	private static String getCategoryAMD(int category) {
		switch(category) {
			case 37193:
				return "API ERROR";
			case 37194:
				return "WINDOW SYSTEM";
			case 37195:
				return "DEPRECATION";
			case 37196:
				return "UNDEFINED BEHAVIOR";
			case 37197:
				return "PERFORMANCE";
			case 37198:
				return "SHADER COMPILER";
			case 37199:
				return "APPLICATION";
			case 37200:
				return "OTHER";
			default:
				return String.format("%s [0x%X]", "Unknown", category);
		}
	}

	private static String getSeverityAMD(int severity) {
		switch(severity) {
			case 37190:
				return "HIGH";
			case 37191:
				return "MEDIUM";
			case 37192:
				return "LOW";
			default:
				return String.format("%s [0x%X]", "Unknown", severity);
		}
	}

	private static DebugState debugState;

	private static interface DebugState {
		void nameObject(int id, int object, String name);
		void pushGroup(int id, String name);
		void popGroup();
	}

	private static class KHRDebugState implements DebugState {
		private boolean hasGroup;

		@Override
		public void nameObject(int id, int object, String name) {
			KHRDebug.glObjectLabel(id, object, name);
		}

		@Override
		public void pushGroup(int id, String name) {
			KHRDebug.glPushDebugGroup(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, id, name);
			hasGroup = true;
		}

		@Override
		public void popGroup() {
			if (hasGroup) {
				KHRDebug.glPopDebugGroup();
				hasGroup = false;
			}
		}
	}

	private static class UnsupportedDebugState implements DebugState {
		@Override
		public void nameObject(int id, int object, String name) {
		}

		@Override
		public void pushGroup(int id, String name) {
		}

		@Override
		public void popGroup() {
		}
	}

	public static void initRenderer() {
		if (GLContext.getCapabilities().GL_KHR_debug || GLContext.getCapabilities().OpenGL43) {
			debugState = new KHRDebugState();
		} else {
			debugState = new UnsupportedDebugState();
		}
	}

	public static void nameObject(int id, int object, String name) {
		debugState.nameObject(id, object, name);
	}
}
