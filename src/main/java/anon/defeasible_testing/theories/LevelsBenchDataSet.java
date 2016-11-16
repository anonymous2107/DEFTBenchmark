package anon.defeasible_testing.theories;

import java.util.Iterator;

import anon.defeasible_testing.defeasible.AbstractKBStructureGenerator;
import anon.defeasible_testing.defeasible.AbstractKBStructureGeneratorIterable;
import anon.defeasible_testing.defeasible.DefeasibleBenchDataSet;
import anon.defeasible_testing.defeasible.KBStructure;
import fr.lirmm.graphik.DEFT.io.DlgpDEFTParser;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.NegativeConstraint;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Rule;

public class LevelsBenchDataSet extends DefeasibleBenchDataSet {
	private static final String NAME = "LevelsBench";
	
	private int[] sizes;
	private Iterable<? extends Object> KBGeneratorIterable;
	
	public LevelsBenchDataSet(int[] sizes) {
		this.sizes = sizes;
		this.KBGeneratorIterable = new LevelsKBStructureGeneratorIterable(sizes);
	}
	
	@Override
	protected int[] getSizes() {
		return this.sizes;
	}
	
	class LevelsKBStructureGenerator extends AbstractKBStructureGenerator {

		public LevelsKBStructureGenerator(int[] sizes) {
			super(sizes);
		}
		
		
		@Override
		protected KBStructure generate(int n) {
			KBStructure kb = new KBStructure();
			// Generate the KB
			final String P = "p";
			final String Q = "q";
			final String S = "s";
			final String R = "r";
			String defeasible = "DEFT";
			
			String atomString;
			Atom atom;
			int size = (2*n) +2;
			// Add first atoms
			for(int j=0; j <= size; j++) {
					atomString = S + j + this.getTermsString("a") + ".";
					atom = DlgpDEFTParser.parseAtom(atomString);
					kb.addAtom(atom);
			}
			
			String label;
			String body;
			String head;
			String ruleString;
			String queryAtom;
			String attackAtom;
			
			Rule prefRightSideRule;
			// Add rules
			for(int i=0; i < size; i++) {

				label = "["+ defeasible + R + i + "] ";
				body = S + (i) + this.getTermsString("X");
				head = P + (i) + this.getTermsString("X");
			
				ruleString = label + head + " :- " + body + ".";
				Rule rule = DlgpDEFTParser.parseRule(ruleString);
				kb.addRule(rule);
				
				prefRightSideRule = rule;
				
				label = "[" + R + i + "] ";
				body = P + (i+1) + this.getTermsString("X");
				head = Q + (i) + this.getTermsString("X");
				
				ruleString = label + head + " :- " + body + ".";
				Rule ruleq = DlgpDEFTParser.parseRule(ruleString);
				kb.addRule(ruleq);
				
				// Add Negative Constraint
				queryAtom = P + i + this.getTermsString("X");
				attackAtom = Q + i + this.getTermsString("X");
				NegativeConstraint nc = DlgpDEFTParser.parseNegativeConstraint("! :- " + queryAtom + ", " + attackAtom + ".");
				kb.addNegativeConstraint(nc);
				
				//if((i % 2) != 0) { //if i is odd
					//Preference pref = new Preference(ruleq, prefRightSideRule);
					//kb.addPreference(pref);
				//}
			}
			// Add last strict rules
			label = "[" + R + size + "] ";
			body = S + size + this.getTermsString("X");
			head = P + size + this.getTermsString("X");

			ruleString = label + head + " :- " + body + ".";
			Rule ruleq = DlgpDEFTParser.parseRule(ruleString);
			kb.addRule(ruleq);
			
			
			// Add query
			Query query = DlgpDEFTParser.parseQuery("? :- " + P + "0" + this.getTermsString("a") + ".");
			kb.addQuery("Atom 0?", query);
			return kb;
		}
	}
	
	class LevelsKBStructureGeneratorIterable extends AbstractKBStructureGeneratorIterable {
		private int[] sizes;
		
		LevelsKBStructureGeneratorIterable(int[] sizes) {
			this.sizes = sizes;
		}

		@Override
		public int[] getSizes() {
			return this.sizes;
		}

		@Override
		public Iterator<KBStructure> iterator() {
			return new LevelsKBStructureGenerator(sizes);
		}
		
		
	}

	@Override
	protected Iterable<? extends Object> getKBGeneratorIterable() {
		return this.KBGeneratorIterable;
	}

	public String getName() {
		return NAME;
	}
}
