package org.phenoscape.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;
import org.phenoscape.app.PropertyChangeObject;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

public class ObservableEventList<T extends PropertyChangeObject> extends ObservableElementList<T> implements ObservableList<T> {
    
    private final List<T> observedElements;
    private final List<ObservableListListener> listeners = new ArrayList<ObservableListListener>();

    public ObservableEventList(EventList<T> source) {
        super(source, new PropertyChangeConnector<T>());
        this.observedElements = new ArrayList<T>(source);
        this.addListEventListener(new ListChangeListener());
    }

    public void addObservableListListener(ObservableListListener listener) {
        this.listeners.add(listener);
    }

    public void removeObservableListListener(ObservableListListener listener) {
        this.listeners.remove(listener);
    }

    public boolean supportsElementPropertyChanged() {
        return true;
    }

    public void listDidChange(ListEvent<T> listChanges) {
        while (listChanges.hasNext()) {
            listChanges.next();
            final int index = listChanges.getIndex();
            if (listChanges.getType() == ListEvent.UPDATE) {
                this.observedElements.set(index, listChanges.getSourceList().get(index));
                this.fireListElementPropertyChanged(index);
            } else if (listChanges.getType() == ListEvent.DELETE) {
                final T deleted = this.observedElements.get(index);
                this.observedElements.remove(index);
                this.fireListElementsRemoved(listChanges.getIndex(), Collections.singletonList(deleted));
            } else if (listChanges.getType() == ListEvent.INSERT) {
                this.observedElements.add(index, listChanges.getSourceList().get(index));
                this.fireListElementsAdded(index, 1);
            }
        }
    }
    
    @Override
    protected boolean isWritable() {
        return true;
    }
    
    private void fireListElementPropertyChanged(int index) {
        for (ObservableListListener listener : this.listeners) {
            listener.listElementPropertyChanged(this, index);
        }
    }
    
    private void fireListElementsRemoved(int index, List<T> oldElements) {
        for (ObservableListListener listener : this.listeners) {
            listener.listElementsRemoved(this, index, oldElements);
        }
    }
    
    private void fireListElementsAdded(int index, int length) {
        for (ObservableListListener listener : this.listeners) {
            listener.listElementsAdded(this, index, length);
        }
    }
    
    private class ListChangeListener implements ListEventListener<T> {

        public void listChanged(ListEvent<T> listChanges) {
            listDidChange(listChanges);
        }
        
    }
    
    private static class PropertyChangeConnector<T extends PropertyChangeObject> implements Connector<T> {

        private ObservableElementList<T> list;

        public EventListener installListener(T element) {
            final Observer observer = new Observer();
            element.addPropertyChangeListener(observer);
            return observer;
        }

        public void setObservableElementList(ObservableElementList<T> list) {
            this.list = list;
        }

        public void uninstallListener(T element, EventListener listener) {
            element.removePropertyChangeListener((Observer)listener);
        }

        private class Observer implements PropertyChangeListener {

            @SuppressWarnings("unchecked")
            public void propertyChange(PropertyChangeEvent evt) {
                list.elementChanged((T)evt.getSource());
            }

        }

    }
    
}
