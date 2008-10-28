package org.phenoscape.app;

import java.util.Collection;

public interface Observer<T extends PropertyChangeObject> {
    
    public void startObserving(T object);

    public void startObserving(Collection<T> objects);
    
    public void stopObserving(T object);

    public void stopObserving(Collection<T> objects);
    
}
