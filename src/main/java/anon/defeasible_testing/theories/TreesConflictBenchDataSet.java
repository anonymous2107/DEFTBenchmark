package anon.defeasible_testing.theories;

import java.util.Iterator;

import anon.defeasible_testing.defeasible.AbstractKBStructureGenerator;
import anon.defeasible_testing.defeasible.AbstractKBStructureGeneratorIterable;
import anon.defeasible_testing.defeasible.DefeasibleBenchDataSet;
import anon.defeasible_testing.defeasible.KBStructure;
import fr.lirmm.graphik.DEFT.io.DlgpDEFTParser;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Rule;

public class TreesConflictBenchDataSet extends DefeasibleBenchDataSet {
	private static final String NAME = "TreesBench";
	
	private int[] sizes;
	private Iterable<? extends Object> KBGeneratorIterable;
	
	public TreesConflictBenchDataSet(int[] sizes, int k) {
		this.sizes = sizes;
		this.KBGeneratorIterable = new TeamsKBStructureGeneratorIterable(sizes, k);
	}
	
	@Override
	protected int[] getSizes() {
		return this.sizes;
	}
	
	class TeamsKBStructureGenerator extends AbstractKBStructureGenerator {
		private int k;
		public TeamsKBStructureGenerator(int[] sizes, int k) {
			super(sizes);
			this.k = k;
		}
		
		
		@Override
		protected KBStructure generate(int n) {
			KBStructure kb = new KBStructure();
			// Generate the KB
			this.generateRecursive(kb, "", 0, n);
			
			// Add query
			Query query = DlgpDEFTParser.parseQuery("? :- " + "p_0(a,b)" + ".");
			kb.addQuery("Atom n?", query);
			return kb;
		}
		
		private void generateRecursive(KBStructure kb, String num, int currentLvl, int maxLvl) {			
			if(currentLvl == maxLvl) {
				// create Atoms
				for(int i=0; i<k; i++) {
					String atomString = "p" + num + "_" + i + "(a,b)";
					Atom atom = DlgpDEFTParser.parseAtom(atomString + ".");
					kb.addAtom(atom);
				}
				return;
			}
			
			this.generateForAtom(kb, num + "_" + 0);
			this.generateForAtom(kb, num + "_" + 1);
			this.generateForAtom(kb, num + "_" + 2);
			this.generateForAtom(kb, num + "_" + 3);
			this.generateForAtom(kb, num + "_" + 4);
			this.generateForAtom(kb, num + "_" + 5);
			
			this.generateRecursive(kb, num + "_" + 0, currentLvl + 1, maxLvl);
			this.generateRecursive(kb, num + "_" + 1, currentLvl + 1, maxLvl);
			this.generateRecursive(kb, num + "_" + 2, currentLvl + 1, maxLvl);
			this.generateRecursive(kb, num + "_" + 3, currentLvl + 1, maxLvl);
			this.generateRecursive(kb, num + "_" + 4, currentLvl + 1, maxLvl);
			this.generateRecursive(kb, num + "_" + 5, currentLvl + 1, maxLvl);
		}
		private void generateForAtom(KBStructure kb, String num) {
			final String P = "p";
			final String Q = "q";
			final String R = "r";
			final String vars = "(X,Y)";
			
			String defeasible = "DEFT";
			String predicate = P;
			
			String atom = P + num;
			String negatedAtom = Q + num;
			String intendedAtom = negatedAtom;
			
			for(int j=0; j < k; j++) {
				if(j == (k-1)) {
					intendedAtom = atom; 
					defeasible = "";
				}

				String label = "["+ defeasible + R + num + "_" + j + "] ";
				String body = "";
				
				body += atom + "_" + j + vars;
				
				String head = intendedAtom + vars;
				
				String ruleString = label + head + " :- " + body + ".";
				Rule rule = DlgpDEFTParser.parseRule(ruleString);
				kb.addRule(rule);
			}

			// Add Negative Constraint
			//String queryAtom = atom + vars;
			//String attackAtom = negatedAtom + vars;
			//NegativeConstraint nc = DlgpDEFTParser.parseNegativeConstraint("[DEFT] ! :- " + queryAtom + ", " + attackAtom + ".");
			//kb.addNegativeConstraint(nc);
		}
		
	}
	
	class TeamsKBStructureGeneratorIterable extends AbstractKBStructureGeneratorIterable {
		private int[] sizes;
		private int k;
		TeamsKBStructureGeneratorIterable(int[] sizes, int k) {
			this.sizes = sizes;
			this.k = k;
		}

		@Override
		public int[] getSizes() {
			return this.sizes;
		}

		@Override
		public Iterator<KBStructure> iterator() {
			return new TeamsKBStructureGenerator(sizes, k);
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
