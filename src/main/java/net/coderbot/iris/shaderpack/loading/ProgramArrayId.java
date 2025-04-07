package net.coderbot.iris.shaderpack.loading;

import lombok.Getter;

@Getter
public enum ProgramArrayId {
	ShadowComposite(ProgramGroup.ShadowComposite, 99),
	Prepare(ProgramGroup.Prepare, 99),
	Deferred(ProgramGroup.Deferred, 99),
	Composite(ProgramGroup.Composite, 99),
	;

	private final ProgramGroup group;
	private final int numPrograms;

	ProgramArrayId(ProgramGroup group, int numPrograms) {
		this.group = group;
		this.numPrograms = numPrograms;
	}

    public String getSourcePrefix() {
		return group.getBaseName();
	}

}
