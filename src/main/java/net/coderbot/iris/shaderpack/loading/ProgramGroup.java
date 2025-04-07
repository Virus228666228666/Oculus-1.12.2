package net.coderbot.iris.shaderpack.loading;

import lombok.Getter;

@Getter
public enum ProgramGroup {
	Shadow("shadow"),
	ShadowComposite("shadowcomp"),
	Prepare("prepare"),
	Gbuffers("gbuffers"),
	Deferred("deferred"),
	Composite("composite"),
	Final("final")
	;

	private final String baseName;

	ProgramGroup(String baseName) {
		this.baseName = baseName;
	}

}
