package anon.defeasible_testing.defeasible;

import java.util.Iterator;

public abstract class AbstractKBStructureGeneratorIterable implements Iterable<KBStructure> {
	
	public abstract int[] getSizes();

	public abstract Iterator<KBStructure> iterator();

}
