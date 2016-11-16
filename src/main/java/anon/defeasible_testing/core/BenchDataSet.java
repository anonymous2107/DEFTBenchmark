package anon.defeasible_testing.core;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public interface BenchDataSet {
	
	void init();
	
	String getName();
	
	List<Pair<String, Iterable<? extends Object>>> getParameters();
}