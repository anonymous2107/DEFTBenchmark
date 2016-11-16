package anon.defeasible_testing.tools;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import anon.defeasible_testing.core.Approach;
import anon.defeasible_testing.defeasible.AbstractRuleBasedReasoningTool;
import anon.defeasible_testing.defeasible.KBStructure;
import fr.lirmm.graphik.DEFT.core.DefeasibleKB;
import fr.lirmm.graphik.DEFT.core.Preference;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseException;
import fr.lirmm.graphik.graal.api.forward_chaining.RuleApplicationException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismFactoryException;
import fr.lirmm.graphik.util.CPUTimeProfiler;
import fr.lirmm.graphik.util.Profiler;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;

public class DEFTTool extends AbstractRuleBasedReasoningTool{
	private static final String NAME = "DEFT";
	
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
	
	public DEFTTool() {
		this.initialize();
	}
	
	private void parseQueries(Iterator<Pair<String, Query>> queries) {
		while(queries.hasNext()) {
			this.queriesString.add(parseQuery(queries.next()));
		}
	}
	
	private Pair<String, String> parseQuery(Pair<String, Query> queryEntry) {
		Atom queryAtom = ((ConjunctiveQuery) queryEntry.getValue()).getAtomSet().iterator().next();
		String queryString = "? :- " + parseAtom(queryAtom) + ".";
		return new ImmutablePair<String, String>(queryEntry.getKey(), queryString); 
	}
	
	private void parseRules(Iterator<Rule> rules) {
		while(rules.hasNext()) {
			this.KBStringBuilder.append(parseRule(rules.next()));
			this.KBStringBuilder.append('\n');
		}
	}
	
	private String parseRule(Rule rule) {
		String str = "";
		if(rule.getLabel() != null) {
			str += "[" + rule.getLabel() + "] ";
		}
		
		CloseableIteratorWithoutException<Atom> itHead = rule.getHead().iterator();
		if(itHead.hasNext()) str += parseAtom(itHead.next());
		while(itHead.hasNext()) {
			str += ", ";
			str += parseAtom(itHead.next());
		}
		str += " :- ";
		CloseableIteratorWithoutException<Atom> itBody = rule.getBody().iterator();
		if(itBody.hasNext()) str += parseAtom(itBody.next());
		while(itBody.hasNext()) {
			str += ", ";
			str += parseAtom(itBody.next());
		}
		str += ".";
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
		String str = "! :- ";
		CloseableIteratorWithoutException<Atom> itBody = nc.getBody().iterator();
		if(itBody.hasNext()) str += parseAtom(itBody.next());
		while(itBody.hasNext()) {
			str += ",";
			str += parseAtom(itBody.next());
		}
		str += ".";
		return str;
	}
	
	private void parsePreferences(Iterator<Preference> prefs) {
		while(prefs.hasNext()) {
			this.KBStringBuilder.append(parsePreference(prefs.next()));
		}
	}
	
	private String parsePreference(Preference pref) {
		String str = "[" + pref.getLeftSide() + "]" + " > " + "[" + pref.getRightSide() + "].";
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
			DefeasibleKB kb = new DefeasibleKB(new StringReader(kbString));
			kb.saturate();
			Pair<String, String> queryStringPair = this.queriesString.iterator().next();
			Atom atom = kb.getAtomsSatisfiyingAtomicQuery(queryStringPair.getValue()).iterator().next();
			this.profiler.stop(Approach.LOADING_TIME);
			
			// II- Query answering phase
			this.profiler.start(Approach.EXE_TIME);
			int entailment = kb.EntailmentStatus(atom);
			this.profiler.stop(Approach.EXE_TIME);
			
			// Add Query and Answer
			this.results.add(queryStringPair);
			this.results.add(new ImmutablePair<String, Object>(Approach.ANSWER, this.getAnswer(entailment)));
			
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
		} catch (AtomSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IteratorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ChaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HomomorphismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HomomorphismFactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuleApplicationException e) {
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
		
		// 5- Parsing Preferences
		this.parsePreferences(kb.getPreferences());
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
