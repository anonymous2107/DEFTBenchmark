package anon.defeasible_testing.theories;

import java.util.Iterator;

import anon.defeasible_testing.defeasible.AbstractKBStructureGenerator;
import anon.defeasible_testing.defeasible.AbstractKBStructureGeneratorIterable;
import anon.defeasible_testing.defeasible.DefeasibleBenchDataSet;
import anon.defeasible_testing.defeasible.KBStructure;
import fr.lirmm.graphik.DEFT.core.Preference;
import fr.lirmm.graphik.DEFT.io.DlgpDEFTParser;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.NegativeConstraint;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Rule;

public class ChainPrefBenchDataSet extends DefeasibleBenchDataSet {
	private static final String NAME = "ChainPrefBench";
	
	private int[] sizes;
	private Iterable<? extends Object> KBGeneratorIterable;
	
	public ChainPrefBenchDataSet(int[] sizes, int nbrAtoms, int nbrTerms) {
		this.sizes = sizes;
		this.KBGeneratorIterable = new ChainKBStructureGeneratorIterable(sizes, nbrAtoms, nbrTerms);
	}
	
	public ChainPrefBenchDataSet(int[] sizes) {
		this(sizes, 1, 1);
	}
	
	@Override
	protected int[] getSizes() {
		return this.sizes;
	}
	
	class ChainKBStructureGenerator extends AbstractKBStructureGenerator {

		public ChainKBStructureGenerator(int[] sizes) {
			super(sizes);
		}
		
		public ChainKBStructureGenerator(int[] sizes, int nbrAtoms, int nbrTerms) {
			super(sizes, nbrAtoms, nbrTerms);
		}
		
		@Override
		protected KBStructure generate(int n) {
			KBStructure kb = new KBStructure();
			// Generate the KB
			final String P = "p";
			final String Q = "q";
			final String R = "r";
			String defeasible = "DEFT";
			String predicate = P;
			
			Rule prefLeftSideRule = null;
			Rule prefRightSideRule = null;
			
			String forDelPpref = "";
			for(int k=0; k < 2; k++) {
				// Add first atoms
				for(int j=0; j < this.getNbrAtoms(); j++) {
					String atomString = predicate + "0" + "_" + j + this.getTermsString("a");
					Atom atom = DlgpDEFTParser.parseAtom(atomString + ".");
					kb.addAtom(atom);
					if(predicate.equals(P)) forDelPpref += predicate + "0" + "_" + j + this.getTermsString("X") + ", ";
				}
				
				
				// Add rules
				for(int i=1; i <= n; i++) {
					if(predicate.equals(Q) && i == n) break;
					String label = "["+ defeasible + predicate + R + i + "] ";
					String body = "";
					// add for delp to activate generalized specificity
					if(i==1 && predicate.equals(Q)) body += forDelPpref;
					
					body += predicate + (i-1) + "_0" + this.getTermsString("X");
					for(int j=1; j < this.getNbrAtoms(); j++) {
						body += ", ";
						body += predicate + (i-1) + "_" + j + this.getTermsString("X");
					}
					
					String head = predicate + (i) + "_0" + this.getTermsString("X");
					for(int j=1; j < this.getNbrAtoms(); j++) {
						head += ", ";
						head += predicate + (i) + "_" + j + this.getTermsString("X");
					}
					
					String ruleString = label + head + " :- " + body + ".";
					Rule rule = DlgpDEFTParser.parseRule(ruleString);
					kb.addRule(rule);
					if(predicate.equals(P)) {
						prefLeftSideRule = rule;
					} else {
						prefRightSideRule = rule;
					}
				}
				defeasible = "DEFT";
				predicate = Q;
			}
			// Add Preference
			if(prefRightSideRule != null) {
				Preference pref = new Preference(prefLeftSideRule, prefRightSideRule);
				kb.addPreference(pref);
			}
			// Add Negative Constraint
			String queryAtom = P + n + "_0" + this.getTermsString("a");
			String attackAtom = Q + (n-1) + "_0" + this.getTermsString("a");
			NegativeConstraint nc = DlgpDEFTParser.parseNegativeConstraint("[DEFT] ! :- " + queryAtom + ", " + attackAtom + ".");
			kb.addNegativeConstraint(nc);
			
			// Add query
			Query query = DlgpDEFTParser.parseQuery("? :- " + queryAtom + ".");
			kb.addQuery("Atom n?", query);
			return kb;
		}
		
	}
	
	class ChainKBStructureGeneratorIterable extends AbstractKBStructureGeneratorIterable {
		private int[] sizes;
		private int nbrAtoms;
		private int nbrTerms;
		
		ChainKBStructureGeneratorIterable(int[] sizes, int nbrAtoms, int nbrTerms) {
			this.sizes = sizes;
			this.nbrTerms = nbrTerms;
			this.nbrAtoms = nbrAtoms;
		}

		@Override
		public int[] getSizes() {
			return this.sizes;
		}

		@Override
		public Iterator<KBStructure> iterator() {
			return new ChainKBStructureGenerator(sizes, nbrAtoms, nbrTerms);
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
