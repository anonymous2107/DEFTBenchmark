package anon.defeasible_testing.core;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import anon.defeasible_testing.defeasible.AbstractKBStructureGenerator;
import anon.defeasible_testing.defeasible.DefeasibleBenchDataSet;
import anon.defeasible_testing.defeasible.KBStructure;
import fr.lirmm.graphik.util.stream.IteratorException;

public class BenchRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(BenchRunner.class);
	
	private final BenchDataSet BENCH;
	private final Iterable<Approach> APPROACHES;
	private final OutputStream OUT;
	private final int NB_ITERATION;
	
	private long timeout;
	
	public BenchRunner(BenchDataSet bench, Iterable<Approach> approaches, OutputStream out) {
		this(bench, approaches, out, 1, 60000);
	}
	
	public BenchRunner(BenchDataSet bench, Iterable<Approach> approaches, OutputStream out, int nbIteration, long timeout) {
		this.BENCH = bench;
		this.APPROACHES = approaches;
		this.OUT = out;
		this.NB_ITERATION = nbIteration;
		this.timeout = timeout;
	}
	
	public void run(Map<String, ? extends Object> params) throws FileNotFoundException, IteratorException {
		System.out.println("===Start the execusion of the Bench===");
		this.BENCH.init();
		PrintStream writer = new PrintStream(OUT);
		//writer.format("%s,%s,%s,%s,%s,%s,%s,%s\n", "approach", "bench", "size", "iteration", "prepareTime", "executionTime", "answer");
		writer.print("approach,bench,size,iteration,loadingTime,executionTime,answer\n");
		
		// Loop for BENCH Parameters
		Iterator<Pair<String, Iterable<? extends Object>>> itBenchParams = this.BENCH.getParameters().iterator();
		while(itBenchParams.hasNext()) {
			Pair<String, Iterable<? extends Object>> benchParam = itBenchParams.next();
			// if it is not the parameter we are looking for then skip it:
			if(benchParam.getKey() != DefeasibleBenchDataSet.KBs) continue;
			
			// Loop for different KBs
			AbstractKBStructureGenerator itBenchParam = (AbstractKBStructureGenerator) benchParam.getValue().iterator();
			while(itBenchParam.hasNext()) {
				KBStructure kb = (KBStructure) itBenchParam.next();
				
				// Loop for Approaches
				Iterator<Approach> itApproaches = this.APPROACHES.iterator();
				while(itApproaches.hasNext()) {
					Approach approach = itApproaches.next();
					approach.initialize();
					List<Pair<String, ? extends Object>> kbPair = new LinkedList<Pair<String, ? extends Object>>();
					kbPair.add(new ImmutablePair<String, KBStructure>("KB", kb));
					
					// Loop for Iterations
					for(int iteration = 0; iteration < this.NB_ITERATION; ++iteration) {
						StringBuilder output = new StringBuilder();
						output.append(approach.getName());
						output.append(',');
						output.append(BENCH.getName());
						output.append(',');
						output.append(itBenchParam.getCurrentSize());
						output.append(',');
						output.append(iteration);
						
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info(output.toString());
						}
						// call prepare function of the approach
						approach.prepare(kbPair.iterator());
						// run the approach
						Thread thread = new Thread(approach);
						thread.start();
						// If the thread takes too much time log it and kill it
						try {
							thread.join(timeout);
						} catch (InterruptedException e1) {

						}
						
						String loadingTime = "TO";
						String executionTime = "TO";
						String answer = "TO";
						
						if(thread.isAlive()) {
							if(LOGGER.isWarnEnabled()) {
								LOGGER.warn("TIMEOUT: ", output.toString());
							}
							thread.stop();
						} else {
							Iterator<Pair<String, ? extends Object>> data = approach.getResults();
							while (data.hasNext()) {
								Pair<String, ? extends Object> pair = data.next();
								if(pair.getKey().equals(Approach.LOADING_TIME)) {
									loadingTime = String.valueOf(pair.getValue());
								} else if(pair.getKey().equals(Approach.EXE_TIME)) {
									executionTime = String.valueOf(pair.getValue());
								} else if(pair.getKey().equals(Approach.ANSWER)) {
									answer = String.valueOf(pair.getValue());
								}
							}
						}
						output.append(',');
						output.append(loadingTime);
						output.append(',');
						output.append(executionTime);
						output.append(',');
						output.append(answer);
						output.append('\n');
						writer.print(output.toString());
						System.out.println(output.toString());
						writer.flush();
					}
				}
			}
		}
		writer.close();
	}
}
