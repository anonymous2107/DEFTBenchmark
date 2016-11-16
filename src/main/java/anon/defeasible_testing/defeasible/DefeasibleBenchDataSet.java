package anon.defeasible_testing.defeasible;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import anon.defeasible_testing.core.BenchDataSet;

public abstract class DefeasibleBenchDataSet implements BenchDataSet {
	public static final String KBs = "KB";
	
	public List<Pair<String, Iterable<? extends Object>>> getParameters() {
		List<Pair<String, Iterable<? extends Object>>> params = new LinkedList<Pair<String, Iterable<? extends Object>>>();
		Pair<String, Iterable<? extends Object>> kbIterable = new ImmutablePair<String, Iterable<? extends Object>>(DefeasibleBenchDataSet.KBs, this.getKBGeneratorIterable());
		params.add(kbIterable);
		
		return params;
	}
	
	public void init() {
		// Nothing to initialize
	}
	
	protected abstract Iterable<? extends Object> getKBGeneratorIterable();
	protected abstract int[] getSizes();
}
