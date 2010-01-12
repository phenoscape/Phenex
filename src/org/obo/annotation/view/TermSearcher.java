package org.obo.annotation.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.obo.app.swing.AutocompleteSearcher;
import org.obo.app.swing.MatchType;
import org.obo.app.swing.SearchHit;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.Synonym;
import org.obo.util.TermUtil;
import org.oboedit.controller.SessionManager;

public class TermSearcher implements AutocompleteSearcher<OBOObject> {

    private final List<SearchHit<OBOObject>> matches = new ArrayList<SearchHit<OBOObject>>();
    private final Collection<OBOObject> terms;
    private static final MatchType NAME_MATCH = new MatchType() {
        public String getName() {
            return "";
        }
    };
    private static final MatchType SYNONYM_MATCH = new MatchType() {
        public String getName() {
            return "Synonym";
        }
    };
    private static final Comparator<TermHit> COMPARATOR = new Comparator<TermHit>() {
        public int compare(TermHit o1, TermHit o2) {
            return o1.getMatchText().compareTo(o2.getMatchText());
        }
    };

    public TermSearcher(Collection<OBOObject> terms) {
        this.terms = terms;
    }

    public List<SearchHit<OBOObject>> getMatches() {
        return this.matches;
    }

    public void setSearch(String input) {
        this.matches.clear();
        final List<TermHit> startsWith = new ArrayList<TermHit>();
        final List<TermHit> containedIn = new ArrayList<TermHit>();
        final String lowerInput = input.toLowerCase();
        for (OBOObject term : this.terms) {
            if ((term.getName() != null)) {
                final int position = term.getName().toLowerCase().indexOf(lowerInput);
                if (position == 0) {
                    startsWith.add(new TermHit(term, term.getName(), NAME_MATCH));
                } else if (position > -1) {
                    containedIn.add(new TermHit(term, term.getName(), NAME_MATCH));
                }
            }
            for (Synonym synonym : term.getSynonyms()) {
                if (synonym.getText() != null) {
                    final int position = synonym.getText().toLowerCase().indexOf(lowerInput);
                    if (position == 0) {
                        startsWith.add(new TermHit(term, synonym.getText(), SYNONYM_MATCH));
                    } else if (position > -1) {
                        containedIn.add(new TermHit(term, synonym.getText(), SYNONYM_MATCH));
                    }
                }
            }
        }
        Collections.sort(startsWith, COMPARATOR);
        Collections.sort(containedIn, COMPARATOR);
        this.matches.addAll(startsWith);
        this.matches.addAll(containedIn);
    }

    public String toString(OBOObject valueObject) {
        return valueObject == null ? null : valueObject.getName();
    }

    public SearchHit<OBOObject> getExactHit(String text) {
        Collection<OBOClass> terms = TermUtil.getTerms(SessionManager.getManager().getSession());
        for (OBOClass term : terms) {
            if ((term.getName() != null) && term.getName().equals(text)) {
                return new TermHit(term, term.getName(), new MatchType() {
                    public String getName() {
                        return "Name";
                    }
                });
            }
        }
        return null;
    }

    public Class<OBOObject> getHitClass() {
        return OBOObject.class;
    }

    public SearchHit<OBOObject> getAsHit(OBOObject item) {
        return new TermHit(item, "", new MatchType() {
            public String getName() {
                return "";
            }
        });
    }

    public boolean isSame(String text, OBOObject item) {
        return item != null ? text.equals(item.getName()) : false;
    }

}
