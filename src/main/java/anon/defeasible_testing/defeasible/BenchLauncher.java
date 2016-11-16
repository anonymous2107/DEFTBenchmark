package anon.defeasible_testing.defeasible;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import anon.defeasible_testing.core.Approach;
import anon.defeasible_testing.core.BenchDataSet;
import anon.defeasible_testing.core.BenchRunner;
import anon.defeasible_testing.theories.ChainFESBenchDataSet;
import anon.defeasible_testing.theories.ChainPrefBenchDataSet;
import anon.defeasible_testing.theories.CircleBenchDataSet;
import anon.defeasible_testing.theories.LevelsBenchDataSet;
import anon.defeasible_testing.theories.SimpleChainBenchDataSet;
import anon.defeasible_testing.theories.SimpleChainFESBenchDataSet;
import anon.defeasible_testing.theories.TeamsBenchDataSet;
import anon.defeasible_testing.theories.TeamsGADBenchDataSet;
import anon.defeasible_testing.theories.TreesBenchDataSet;
import anon.defeasible_testing.theories.TreesConflictBenchDataSet;
import anon.defeasible_testing.tools.DEFTTool;
import fr.lirmm.graphik.util.stream.IteratorException;

public class BenchLauncher {
	public static final String BENCH_CHAIN = "CHAIN";
	public static final String BENCH_SIMPLE_CHAIN = "SIMPLE_CHAIN";
	public static final String BENCH_SIMPLE_CHAIN_FES = "SIMPLE_CHAIN_FES";
	public static final String BENCH_CHAIN_FES = "CHAIN_FES";
	public static final String BENCH_CHAIN_PREF = "CHAIN_PREF";
	public static final String BENCH_CIRCLE = "CIRCLE";
	public static final String BENCH_LEVELS = "LEVELS";
	public static final String BENCH_TEAMS = "TEAMS";
	public static final String BENCH_TEAMS_GAD = "TEAMS_GAD";
	public static final String BENCH_TREES = "TEES";
	public static final String BENCH_TREES_CONFLICT = "TEES_CONFLICT";
	
	
	@Parameter(names = { "-h", "--help" }, description = "Print this message", help = true)
	private boolean            help;
	
	@Parameter(names = { "-n", "--size" }, converter = IntArrayConverter.class, description = "Comma-separated list of sizes of the generated knowledge bases")
	private int[]              sizes          = new int[] {80000}; // {1500,2000};  //{1,2,3,4};//{1,10,100,200,300,400,500,1000};
	
	@Parameter(names = { "-b", "--bench" }, description = BENCH_CHAIN+"|"+BENCH_CHAIN_FES +"|"+BENCH_CIRCLE+"|...")
	private String             benchType             = BENCH_LEVELS;
	
	@Parameter(names = { "-o", "--output-file" }, description = "Output file (use '-' for stdout)")
	private String             outputFilePath = "Teams2.csv";//"-";
	
	@Parameter(names = { "-t", "--timeout" }, description = "Timeout in ms")
	private long               timeout        = 3000000;
	
	@Parameter(names = { "-i", "--iterations" }, description = "Number of iterations for each Bench size")
	private int 			   iterations     = 2;
	
	@Parameter(names = { "-a", "--n-atoms" }, description = "Number of atoms per rule")
	private int 			   nbrAtoms          = 1;
	
	@Parameter(names = { "-x", "--n-terms" }, description = "Number of terms per rule")
	private int 			   nbrTerms          = 1;
	
	public static void main(String args[]) throws FileNotFoundException, IteratorException {
		System.out.println("===Start the execusion===");
		
		BenchLauncher options = new BenchLauncher();

		JCommander commander = new JCommander(options, args);

		if (options.help) {
			commander.usage();
			System.exit(0);
		}
		
		OutputStream outputStream = null;
		if (options.outputFilePath.equals("-")) {
			outputStream = System.out;
		} else {
			try {
				outputStream = new FileOutputStream(options.outputFilePath);
			} catch (Exception e) {
				System.err.println("Could not open file: " + options.outputFilePath);
				System.err.println(e);
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		List<Approach> approaches = new LinkedList<Approach>();
		approaches.add(new DEFTTool());
		//approaches.add(new DEFTToolOptimized()); // Very fast but support for preferences is not fully tested
		//approaches.add(new DeLPTool());
		//approaches.add(new ASPICTool());
		
		BenchDataSet bench = null;
		if(options.benchType == options.BENCH_CHAIN_FES) {
			// bench circle
			bench = new ChainFESBenchDataSet(options.sizes);
		} else if(options.benchType == options.BENCH_CHAIN_PREF) {
			bench = new ChainPrefBenchDataSet(options.sizes, options.nbrAtoms, options.nbrTerms);
		} else if(options.benchType == options.BENCH_TEAMS) {
			bench = new TeamsBenchDataSet(options.sizes);
		} else if(options.benchType == options.BENCH_TEAMS_GAD) {
			bench = new TeamsGADBenchDataSet(options.sizes);
		} else if(options.benchType == options.BENCH_SIMPLE_CHAIN) {
			bench = new SimpleChainBenchDataSet(options.sizes, options.nbrAtoms, options.nbrTerms);
		} else if(options.benchType == options.BENCH_CIRCLE) {
			bench = new CircleBenchDataSet(options.sizes, options.nbrAtoms, options.nbrTerms);
		} else if(options.benchType == options.BENCH_SIMPLE_CHAIN_FES) {
			bench = new SimpleChainFESBenchDataSet(options.sizes);
		} else if(options.benchType == options.BENCH_LEVELS) {
			bench = new LevelsBenchDataSet(options.sizes);
		} else if(options.benchType == options.BENCH_TREES) {
			bench = new TreesBenchDataSet(options.sizes, 5);
		} else if(options.benchType == options.BENCH_TREES_CONFLICT) {
			bench = new TreesConflictBenchDataSet(options.sizes, 5);
		}
		
		new BenchRunner(bench, approaches, outputStream, options.iterations, options.timeout).run(null);
		
		System.out.println("===End the execusion===");
	}
	
	
	
	
	
	
	
	public class IntArrayConverter implements IStringConverter<int[]> {
		public int[] convert(String value) {
			String[] strArray = value.split(",");
			int[] intArray = new int[strArray.length];
			for(int i = 0; i < strArray.length; i++) {
			    intArray[i] = Integer.parseInt(strArray[i]);
			}
			return intArray;
		}
	}

}
