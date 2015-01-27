package com.topsoft.fernflower.eclipse.plugin.settings;

import com.topsoft.fernflower.eclipse.plugin.utils.LoggerUtil;

public class FernflowerSettings {

	private CmdOption[] allOptions = CmdOption.values();

	public String getOptionsLine() {
		StringBuilder optionsLine = new StringBuilder();
		for (CmdOption commandLineOption : getAllOptions()) {
			if (commandLineOption.toBeListed()) {
				optionsLine.append(commandLineOption.toString());
			}
		}
		String optionsLineString = optionsLine.toString();
		LoggerUtil.logDebug("Composed cmd options line: " + optionsLineString);
		return optionsLineString;
	}

	public CmdOption[] getAllOptions() {
		return allOptions;
	}

	public enum CmdOption {
		RBR(true, "hide bridge methods"), DEN(true, "decompile enumerations"), RSY(false,
				"hide synthetic class members"), RGN(true, "remove getClass() invocation",
				"when it is part of a qualified new statement"), DIN(true, "decompile inner classes"), BTO(true,
				"interpret int 1 as boolean true"), DC4(true, "collapse 1.4 class references"), NNS(true,
				"allow for not set synthetic attribute", "workaround to a compiler bug"), DAS(true,
				"decompile assertions"), UTO(true, "consider nameless types as java.lang.Object",
				"workaround to a compiler architecture flaw"), HES(true, "hide empty super invocation"), UDV(true,
				"reconstruct variable names from debug info", "if debug information is present"), HDC(true,
				"hide empty default constructor"), RER(true, "remove empty exception ranges"), DGS(false,
				"decompile generic signatures"), FDI(true, "deinline finally structures"), OCC(false,
				"ouput copyright comment"), ASC(false, "allow only ASCII characters in string literals",
				"All other characters will be encoded using Unicode escapes (JLS 3.3). Default encoding is UTF8"), NER(
				true, "assume return not throwing exceptions"), REN(false,
				"rename ambiguous classes and class elements");

		private boolean defaultValue;

		private boolean currentValue;

		private String optionDescription;

		private String additionalDescription;

		private CmdOption(boolean initialValue, String optionDescription) {
			this.defaultValue = initialValue;
			this.currentValue = initialValue;
			this.optionDescription = optionDescription;
		}

		private CmdOption(boolean initialValue, String optionDescription, String additionalDescription) {
			this(initialValue, optionDescription);
			this.additionalDescription = additionalDescription;
		}

		public void setCurrentValue(boolean currentValue) {
			this.currentValue = currentValue;
		}

		public boolean getCurrentValue() {
			return currentValue;
		}

		public String toString() {
			return String.format(" -%s=%s", name().toLowerCase(), (currentValue ? "1" : "0"));
		}

		public String getOptionDescription() {
			return optionDescription;
		}

		public String getAdditionalDescription() {
			return additionalDescription;
		}

		private boolean toBeListed() {
			return currentValue != defaultValue;
		}
	}
}
