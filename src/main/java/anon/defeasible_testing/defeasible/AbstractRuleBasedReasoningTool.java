package anon.defeasible_testing.defeasible;

import anon.defeasible_testing.core.Approach;

public abstract class AbstractRuleBasedReasoningTool implements Approach {
	public abstract String getKBString();
	
	public abstract String getAnswer(Object answer);
}
