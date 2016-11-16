package anon.defeasible_testing.defeasible;

import java.util.Iterator;

public abstract class AbstractKBStructureGenerator implements Iterator<KBStructure> {
	private int[] sizes;
	private int position;
	private int nbrAtoms;
	private int nbrTerms;
	
	private boolean hasNextCallDone;
	
	public AbstractKBStructureGenerator(int[] sizes, int nbrAtoms, int nbrTerms) {
		this.sizes = sizes;
		this.nbrTerms = nbrTerms;
		this.nbrAtoms = nbrAtoms;
		this.position = -1;
		this.hasNextCallDone = false;
	}
	
	public AbstractKBStructureGenerator(int[] sizes) {
		this(sizes, 1, 1);
	}

	public boolean hasNext() {
		if(!this.hasNextCallDone) {
			++this.position;
			this.hasNextCallDone = true;
		}
		return this.position < sizes.length;
	}

	public KBStructure next() {
		if(!this.hasNextCallDone) {
			this.hasNext();
		}
		this.hasNextCallDone = false;
		return generate(sizes[position]);
	}
	
	public int getCurrentSize() {
		return this.sizes[this.position];
	}
	
	protected abstract KBStructure generate(int i);
	
	protected String getTermsString(String param) {
		String str = "(" + param + "0";
		
		for(int i =1; i < this.nbrTerms; i++) {
			str += "," + param + i;
		}
		str += ")";
		return str;
	}
	
	protected int getNbrAtoms() {
		return this.nbrAtoms;
	}
	
}
