package anon.defeasible_testing.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import anon.defeasible_testing.core.Approach;
import anon.defeasible_testing.defeasible.AbstractRuleBasedReasoningTool;
import anon.defeasible_testing.defeasible.KBStructure;
import fr.lirmm.graphik.DEFT.core.DefeasibleKB;
import fr.lirmm.graphik.DEFT.core.DefeasibleRule;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.Rules;
import fr.lirmm.graphik.util.CPUTimeProfiler;
import fr.lirmm.graphik.util.Profiler;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;
import net.sf.tweety.arg.delp.DefeasibleLogicProgram;
import net.sf.tweety.arg.delp.DelpReasoner;
import net.sf.tweety.arg.delp.parser.DelpParser;
import net.sf.tweety.arg.delp.semantics.GeneralizedSpecificity;
import net.sf.tweety.commons.Answer;
import net.sf.tweety.commons.Formula;
import net.sf.tweety.commons.ParserException;
import net.sf.tweety.logics.fol.parser.FolParser;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.syntax.Negation;

public class DeLPTool extends AbstractRuleBasedReasoningTool{
	private static final String NAME = "DeLP";
	
	Profiler profiler;
	
	private LinkedList<Pair<String, String>> queriesString;
	private StringBuilder KBStringBuilder;
	private LinkedList<Pair<String, ? extends Object>> results;
	
	public void initialize() {
		this.profiler = new CPUTimeProfiler();
		this.queriesString = new LinkedList<Pair<String, String>>();
		this.results = new LinkedList<Pair<String, ? extends Object>>();
		this.KBStringBuilder = new StringBuilder();
	}
	
	public DeLPTool() {
		this.initialize();
	}
	
	private void parseQueries(Iterator<Pair<String, Query>> queries) {
		while(queries.hasNext()) {
			this.queriesString.add(parseQuery(queries.next()));
		}
	}
	
	private Pair<String, String> parseQuery(Pair<String, Query> queryEntry) {
		Atom queryAtom = ((ConjunctiveQuery) queryEntry.getValue()).getAtomSet().iterator().next();
		String queryString = parseAtom(queryAtom);
		return new ImmutablePair<String, String>(queryEntry.getKey(), queryString); 
	}
	
	private void parseRules(Iterator<Rule> rules) {
		while(rules.hasNext()) {
			this.KBStringBuilder.append(parseRule(rules.next()));
		}
	}
	
	private String parseRule(Rule rule) {
		Iterator<Rule> itAtomicRule = Rules.computeAtomicHead(rule).iterator();
		
		String str = "";
		String implies = " <- ";
		if(rule instanceof DefeasibleRule) {
			implies = " -< ";
		}
		
		// TODO when there is multiple head!
		while(itAtomicRule.hasNext()) {
			Rule atomicRule = itAtomicRule.next();
			CloseableIteratorWithoutException<Atom> itHead = atomicRule.getHead().iterator();
			while(itHead.hasNext()) {
				str += parseAtom(itHead.next());
			}
			str += implies;
			CloseableIteratorWithoutException<Atom> itBody = atomicRule.getBody().iterator();
			if(itBody.hasNext()) str += parseAtom(itBody.next());
			while(itBody.hasNext()) {
				str += ", ";
				str += parseAtom(itBody.next());
			}
			str += ".\n";
		}
		return str;
	}
	
	private void parseAtoms(CloseableIterator<Atom> closeableIterator) {
		try {
		while(closeableIterator.hasNext()) {
			String atomString = parseAtom(closeableIterator.next());
			this.KBStringBuilder.append(atomString);
			this.KBStringBuilder.append('.');
			this.KBStringBuilder.append('\n');
		}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String parseAtom(Atom atom) {
		String str = atom.getPredicate().getIdentifier().toString();
		str += "(";
		Iterator<Term> itTerms = atom.getTerms().iterator();
		str += itTerms.next();
		while(itTerms.hasNext()) {
			str += "," + itTerms.next();
		}
		str += ")";
		return str;
	}
	
	private void parseNegativeConstraints(Iterator<Rule> negativeContraints) {
		while(negativeContraints.hasNext()) {
			this.KBStringBuilder.append(parseNegativeConstraint(negativeContraints.next()));
			this.KBStringBuilder.append('\n');
		}
	}
	
	private String parseNegativeConstraint(Rule nc) {
		String str = "";
		String implies = " <- ";
		if(nc.getLabel() != null && !nc.getLabel().isEmpty()) implies = " -< ";
		CloseableIteratorWithoutException<Atom> itBody = nc.getBody().iterator();
		if(itBody.hasNext()) str += "~" + parseAtom(itBody.next()) + implies;
		if(itBody.hasNext()) str += parseAtom(itBody.next());
		str += ".";
		return str;
	}
	
	public void run() {
		//System.out.println("KBDEFTBuilder");
		String kbString = this.getKBString();
		//System.out.println(kbString);
		//System.out.println(this.queriesString.iterator().next().getValue());
		try {
			//this.profiler.start(Approach.TOTAL_TIME);
			// I- Preparation phase
			this.profiler.clear();
			this.profiler.start(Approach.LOADING_TIME);
			//DefeasibleKB kb = new DefeasibleKB(new CharSequenceReader(this.KBStringBuilder));
			DelpParser parser = new DelpParser();
			DefeasibleLogicProgram delp = parser.parseBeliefBase(kbString);
			GeneralizedSpecificity comp = new GeneralizedSpecificity();
			
			DelpReasoner reasoner = new DelpReasoner(delp,comp);
			
			FolParser folParser = new FolParser();
			folParser.setSignature(parser.getSignature());
			
			Pair<String, String> queryStringPair = this.queriesString.iterator().next();
			// query
			String qString = queryStringPair.getValue();
			Formula f;
			if(qString.startsWith("~"))
				f = new Negation((FolFormula)folParser.parseFormula(qString.substring(1)));
			else f = folParser.parseFormula(qString);
		
			this.profiler.stop(Approach.LOADING_TIME);
			
			// II- Query answering phase
			this.profiler.start(Approach.EXE_TIME);
			Answer ans = reasoner.query(f);
			String answer = String.valueOf(ans.getAnswerBoolean());
			this.profiler.stop(Approach.EXE_TIME);
			
			// Add Query and Answer
			this.results.add(queryStringPair);
			this.results.add(new ImmutablePair<String, Object>(Approach.ANSWER, answer));
			
			//this.profiler.stop(Approach.TOTAL_TIME);
			
			// Add times
			Iterator<Entry<String, Object>> itProfiler = this.profiler.entrySet().iterator();
			while(itProfiler.hasNext()) {
				Entry<String, Object> entry = itProfiler.next();
				this.results.add(new ImmutablePair<String, Object>(entry.getKey(), entry.getValue()));
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IteratorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void prepare(Iterator<Pair<String, ? extends Object>> params) {
		KBStructure kb = (KBStructure) params.next().getValue();
		
		//System.out.println(kb.toString());
		
		this.KBStringBuilder = new StringBuilder();
		// 1- Parsing Queries
		this.parseQueries(kb.getQueries());
		
		// 2- Parsing Atoms
		this.parseAtoms(kb.getAtoms());
		
		// 3- Parsing Rules 
		this.parseRules(kb.getRules());
		
		// 4- Parsing Negative Constraints
		this.parseNegativeConstraints(kb.getNegativeConstraints());
	}

	public Iterator<Pair<String, ? extends Object>> getResults() {
		return results.iterator();
	}

	@Override
	public String getKBString() {
		return this.KBStringBuilder.toString();
	}

	public String getName() {
		return NAME;
	}

	@Override
	public String getAnswer(Object answer) {
		int ans = (Integer) answer;
		String answerString = "";
		
		switch(ans) {
			case DefeasibleKB.NOT_ENTAILED: answerString = "No"; break;
			case DefeasibleKB.DEFEASIBLY_ENTAILED: answerString = "Yes"; break;
			case DefeasibleKB.STRICTLY_ENTAILED: answerString = "Yes";
		}

		return answerString;
	}

}
