package org.obo.app.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.eekboom.utils.Strings;

public class DefaultSearcher<T> implements AutocompleteSearcher<T> {
    
    private final Collection<T> choices;
    private final Class<T> hitClass;
    private final List<SearchHit<T>> matches = new ArrayList<SearchHit<T>>();
    public static final MatchType TO_STRING_STARTS_WITH = new MatchType() {
        public String getName() {
            return "";
        }
    };
    public static final MatchType TO_STRING_CONTAINS = new MatchType() {
        public String getName() {
            return "";
        }
    };
    public static final Comparator<DefaultHit<?>> HIT_COMPARATOR = new Comparator<DefaultHit<?>>() {
        public int compare(DefaultHit<?> o1, DefaultHit<?> o2) {
            return Strings.compareNatural(o1.getMatchText(), o2.getMatchText());
        }
    };

    public DefaultSearcher(Collection<T> choices, Class<T> hitClass) {
        this.choices = choices;
        this.hitClass = hitClass;
    }

    public SearchHit<T> getAsHit(T item) {
        return new DefaultHit<T>(item, "", new MatchType() {
            public String getName() {
                return "";
            }
        });
    }

    public SearchHit<T> getExactHit(String text) {
        for (T choice : this.choices) {
            if (text.equals(choice.toString())) {
                return new DefaultHit<T>(choice, text, TO_STRING_STARTS_WITH);
            }
        }
        return null;
    }
    
    public Class<T> getHitClass() {
        return this.hitClass;
    }

    public List<SearchHit<T>> getMatches() {
        return this.matches;
    }

    public boolean isSame(String text, T item) {
        return item != null ? text.equals(item.toString()) : false;
    }

    public void setSearch(String input) {
        this.matches.clear();
        final List<DefaultHit<T>> startsWith = new ArrayList<DefaultHit<T>>();
        final List<DefaultHit<T>> containedIn = new ArrayList<DefaultHit<T>>();
        final String lowerInput = input.toLowerCase();
        for (T choice : this.choices) {
            final int position = choice.toString().toLowerCase().indexOf(lowerInput);
            if (position == 0) {
                startsWith.add(new DefaultHit<T>(choice, choice.toString(), TO_STRING_STARTS_WITH));
            } else if (position > -1) {
                containedIn.add(new DefaultHit<T>(choice, choice.toString(), TO_STRING_CONTAINS));
            }
        }
        Collections.sort(startsWith, HIT_COMPARATOR);
        Collections.sort(containedIn, HIT_COMPARATOR);
        this.matches.addAll(startsWith);
        this.matches.addAll(containedIn);
    }

    public String toString(T valueObject) {
        return valueObject == null ? null : valueObject.toString();
    }

}
