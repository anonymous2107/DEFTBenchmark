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

public class SimpleChainFESBenchDataSet extends DefeasibleBenchDataSet {
	private static final String NAME = "ChainFESBench";
	
	private int[] sizes;
	private Iterable<? extends Object> KBGeneratorIterable;
	
	public SimpleChainFESBenchDataSet(int[] sizes) {
		this.sizes = sizes;
		this.KBGeneratorIterable = new ChainFESKBStructureGeneratorIterable(sizes);
	}
	
	@Override
	protected int[] getSizes() {
		return this.sizes;
	}
	
	class ChainFESKBStructureGenerator extends AbstractKBStructureGenerator {

		public ChainFESKBStructureGenerator(int[] sizes) {
			super(sizes);
		}
		
		@Override
		protected KBStructure generate(int n) {
			KBStructure kb = new KBStructure();
			// Generate the KB
			final String P = "p";
			final String R = "r";
			String defeasible = "DEFT";
			String predicate = P;
			
			// Add first atoms
			String atomString = predicate + "0(a,c)" + ".";
			Atom atom = DlgpDEFTParser.parseAtom(atomString);
			kb.addAtom(atom);
			atomString = predicate + "0(c,b)" + ".";
			atom = DlgpDEFTParser.parseAtom(atomString);
			kb.addAtom(atom);
			
			// Add rules
			for(int i=1; i <= n+1; i++) {
				String label = "["+ defeasible + R + i + "] ";
				String body = P + (i-1) + "(X,Y),";
				body += P + (i-1) + "(Y,Z)";
				
				String head = predicate + (i-1) + "(X,Z),";
				head += predicate + (i) + "(X,Y),";
				head += predicate + (i) + "(Y,Z)";
				
				String ruleString = label + head + " :- " + body + ".";
				Rule rule = DlgpDEFTParser.parseRule(ruleString);
				kb.addRule(rule);
			}
			
			// Add query
			Query query = DlgpDEFTParser.parseQuery("? :- " + P + n + "(a,b)" + ".");
			kb.addQuery("Atom n?", query);
			return kb;
		}
		
	}
	
	class ChainFESKBStructureGeneratorIterable extends AbstractKBStructureGeneratorIterable {
		private int[] sizes;
		
		ChainFESKBStructureGeneratorIterable(int[] sizes) {
			this.sizes = sizes;
		}

		@Override
		public int[] getSizes() {
			return this.sizes;
		}

		@Override
		public Iterator<KBStructure> iterator() {
			return new ChainFESKBStructureGenerator(sizes);
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
