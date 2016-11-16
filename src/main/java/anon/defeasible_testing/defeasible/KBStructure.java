package anon.defeasible_testing.defeasible;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fr.lirmm.graphik.DEFT.core.Preference;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.NegativeConstraint;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.core.atomset.graph.DefaultInMemoryGraphAtomSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;

public class KBStructure extends Object {
	private AtomSet atoms;
	private RuleSet rules;
	private RuleSet negativeConstraints;
	private LinkedList<Preference> preferences;
	private LinkedList<Pair<String, Query>> queries;
	
	public KBStructure() {
		this.atoms = new DefaultInMemoryGraphAtomSet();
		this.rules = new LinkedListRuleSet();
		this.negativeConstraints = new LinkedListRuleSet();
		this.preferences = new LinkedList<Preference>();
		this.queries = new LinkedList<Pair<String, Query>>();
	}
	
	public CloseableIterator<Atom> getAtoms() {
		return atoms.iterator();
	}
	
	public Iterator<Rule> getRules() {
		return rules.iterator();
	}
	
	public Iterator<Rule> getNegativeConstraints() {
		return negativeConstraints.iterator();
	}
	
	public Iterator<Preference> getPreferences() {
		return preferences.iterator();
	}
	
	public Iterator<Pair<String, Query>> getQueries() {
		return this.queries.iterator();
	}
	
	public void addAtom(Atom atom) {
		try {
			this.atoms.add(atom);
		} catch (AtomSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addAtomSet(AtomSet atomSet) {
		try {
			this.atoms.addAll(atomSet);
		} catch (AtomSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addRule(Rule rule) {
		this.rules.add(rule);
	}
	
	public void addNegativeConstraint(NegativeConstraint rule) {
		this.negativeConstraints.add((Rule)rule);
	}
	
	public void addPreference(Preference preference) {
		this.preferences.add(preference);
	}
	
	public void addQuery(String str, Query query) {
		this.queries.add(new ImmutablePair<String, Query>(str, query));
	}
	
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		try {
			CloseableIterator<Atom> itAtoms = this.atoms.iterator();
			while(itAtoms.hasNext()) {
				strBuilder.append(itAtoms.next());
				strBuilder.append("\n");
			}
			
			Iterator<Rule> itRules = this.rules.iterator();
			while(itRules.hasNext()) {
				strBuilder.append(itRules.next());
				strBuilder.append("\n");
			}
			
			Iterator<Preference> itPreference = this.preferences.iterator();
			while(itPreference.hasNext()) {
				strBuilder.append(itPreference.next());
				strBuilder.append("\n");
			}
			
			Iterator<Rule> itNC = this.negativeConstraints.iterator();
			while(itNC.hasNext()) {
				strBuilder.append(itNC.next());
				strBuilder.append("\n");
			}
			
			Iterator<Pair<String, Query>> itQueries = this.queries.iterator();
			while(itQueries.hasNext()) {
				Pair<String, Query> queryPair = itQueries.next();
				strBuilder.append(queryPair.getKey() + ": " + queryPair.getValue());
				strBuilder.append("\n");
			}
			
		} catch (IteratorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strBuilder.toString();
	}
}
