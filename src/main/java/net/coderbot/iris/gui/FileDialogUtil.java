package net.coderbot.iris.gui;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
//import org.lwjgl.system.MemoryStack;
//import org.lwjgl.util.tinyfd.TinyFileDialogs;

/**
 * Class used to make interfacing with @link TinyFileDialogs easier and asynchronous.
 */
public final class FileDialogUtil {
	private static final ExecutorService FILE_DIALOG_EXECUTOR = Executors.newSingleThreadExecutor();

	private FileDialogUtil() {}

	/**
	 * Opens an asynchronous file select dialog window.
	 *
	 * @param dialog Whether to open a "save" dialog or an "open" dialog
	 * @param title The title of the dialog window
	 * @param origin The path that the window should start at
	 * @param filterLabel A label used to describe what file extensions are allowed and their purpose
	 * @param filters The file extension filters used by the dialog, each formatted as {@code "*.extension"}
	 * @return a {@link CompletableFuture} which is completed once a file is selected or the dialog is cancelled.
	 */
	public static CompletableFuture<Optional<Path>> fileSelectDialog(DialogType dialog, String title, @Nullable Path origin, @Nullable String filterLabel, String ... filters) {
		CompletableFuture<Optional<Path>> future = new CompletableFuture<>();

		FILE_DIALOG_EXECUTOR.submit(() -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle(title);

			if (origin != null) {
				fileChooser.setCurrentDirectory(origin.toFile());
			}

			if (filterLabel != null && filters.length > 0) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter(filterLabel, filters);
				fileChooser.setFileFilter(filter);
			}

			int result;
			if (dialog == DialogType.SAVE) {
				result = fileChooser.showSaveDialog(null);
			} else {
				result = fileChooser.showOpenDialog(null);
			}

			if (result == JFileChooser.APPROVE_OPTION) {
				future.complete(Optional.of(fileChooser.getSelectedFile().toPath()));
			} else {
				future.complete(Optional.empty());
			}
		});

		return future;
	}

	public enum DialogType {
		SAVE, OPEN
	}
}
