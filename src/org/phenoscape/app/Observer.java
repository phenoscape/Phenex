package org.phenoscape.app;

import java.util.Collection;

/**
 * An object which observes properties of another class of object.
 * @author Jim Balhoff
 *
 * @param <T> The class of objects to be observed.
 */
public interface Observer<T extends PropertyChangeObject> {
    
    /**
     * Begin observing changes to this object.
     */
    public void startObserving(T object);

    /**
     * Begin observing changes to each object in this collection.
     */
    public void startObserving(Collection<T> objects);
    
    /**
     * Stop observing changes to this object.
     */
    public void stopObserving(T object);

    /**
     * Stop observing changes to each object in this collection.
     */
    public void stopObserving(Collection<T> objects);
    
}
