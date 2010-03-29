package org.obo.app.model;

import java.beans.PropertyChangeListener;

/**
 * An object that notifies listeners when its properties are changed. Implementors 
 * should document and provide constants for the available observable properties.
 * @author Jim Balhoff
 */
public interface PropertyChangeObject {
    
    /**
     * Add a property listener which is notified of all changes.
     */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Add a property listener which is notified of changes to the specified property.
     */
    public void addPropertyChangeListener(String propName, PropertyChangeListener l);

    /**
     * Remove a property listener from all notifications from this object.
     */
    public void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Remove a property listener from notifications of changes for a particular property.
     */
    public void removePropertyChangeListener(String propName, PropertyChangeListener l);
    
    /**
     * Set the value of a property by name. If the name does not refer to a supported property, 
     * an UndefinedKeyException should be thrown.
     */
    public void putValue(String propertyKey, Object value) throws UndefinedKeyException;
    
    /**
     * Get the value of a property by name. If the name does not refer to a supported property, 
     * an UndefinedKeyException should be thrown.
     */
    public Object getValue(String propertyKey) throws UndefinedKeyException;
    
    /**
     * Returns the value Class of the given property. If the name does not refer to a supported property, 
     * an UndefinedKeyException should be thrown.
     */
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