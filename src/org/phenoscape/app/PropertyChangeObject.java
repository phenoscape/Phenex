package org.phenoscape.app;

import java.beans.PropertyChangeListener;

public interface PropertyChangeObject {
    
    public void addPropertyChangeListener(PropertyChangeListener l);

    public void addPropertyChangeListener(String propName, PropertyChangeListener l);

    public void removePropertyChangeListener(PropertyChangeListener l);

    public void removePropertyChangeListener(String propName, PropertyChangeListener l);
    
    public void putValue(String propertyKey, Object value) throws UndefinedKeyException;
    
    public Object getValue(String propertyKey) throws UndefinedKeyException;
    
    public Class<?> getClass(String propertyKey) throws UndefinedKeyException;
    
    @SuppressWarnings("serial")
    public static class UndefinedKeyException extends RuntimeException {

        public UndefinedKeyException(String message, Throwable cause) {
            super(message, cause);
        }

        public UndefinedKeyException(String message) {
            super(message);
        }

        public UndefinedKeyException(Throwable cause) {
            super(cause);
        }

    }

}