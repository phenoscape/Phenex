package org.phenoscape.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Default implementation for the PropertyChangeObject interface. Subclasses should call some firePropertyChange 
 * method for changes to each observable property, and also override getClass(String) to return the appropriate 
 * class for each property. 
 * @author Jim Balhoff
 */
public abstract class AbstractPropertyChangeObject implements PropertyChangeObject {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /* (non-Javadoc)
     * @see org.phenoscape.app.PropertyChangeObject#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        this.pcs.addPropertyChangeListener(l);
    }

    /* (non-Javadoc)
     * @see org.phenoscape.app.PropertyChangeObject#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propName, PropertyChangeListener l) {
        this.pcs.addPropertyChangeListener(propName, l);
    }

    /* (non-Javadoc)
     * @see org.phenoscape.app.PropertyChangeObject#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        this.pcs.removePropertyChangeListener(l);
    }

    /* (non-Javadoc)
     * @see org.phenoscape.app.PropertyChangeObject#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propName, PropertyChangeListener l) {
        this.pcs.removePropertyChangeListener(propName, l);
    }

    /**
     * This implementation searches for and calls a method of the form "setProperty" for a given "property", 
     * taking a single argument of the class returned by getClass(String) for that property. If no 
     * such method is found, UndefinedKeyException is thrown.
     */
    public void putValue(String propertyKey, Object value) {
        final String setter = this.setter(propertyKey);
        if (this.hasMethod(setter, this.getClass(propertyKey))) {
            this.callMethod(this.getMethod(setter, this.getClass(propertyKey)), value);
        } else {
            throw new UndefinedKeyException(propertyKey);
        }
    }

    /**
     * This implementation searches for and returns the result of a method of the form "getProperty" 
     * for a given "property", taking no arguments. If no such method is found, UndefinedKeyException is thrown.
     */
    public Object getValue(String propertyKey) throws UndefinedKeyException {
        @SuppressWarnings("unchecked")
        final String getter = this.getter(propertyKey);
        final Object item;
        if (this.hasMethod(getter)) {
            item = this.callMethod(this.getMethod(getter));
        } else {
            throw new UndefinedKeyException(propertyKey);
        }
        return item;
    }

    /**
     * Subclasses should override to return the appropriate class for each observable property. If propertyKey 
     * does not refer to an observable property, subclasses should return the result of calling super, which 
     * throws UndefinedKeyException.
     */
    public Class<?> getClass(String propertyKey) throws UndefinedKeyException {
        throw new UndefinedKeyException(propertyKey);
    }

    /**
     * Notify all property change listeners that some property has changed. Subclasses should 
     * generally fire changes for specific properties instead.
     */
    protected void firePropertyChange(PropertyChangeEvent evt) {
        this.pcs.firePropertyChange(evt);
    }

    /**
     * Notify listeners that the specified property has changed.
     */
    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        this.pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Notify listeners that the specified property has changed.
     */
    protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
        this.pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Notify listeners that the specified property has changed.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        this.pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    private String getter(String key) {
        return "get" + StringUtils.capitalize(key);
    }

    private String setter(String key) {
        return "set" + StringUtils.capitalize(key);
    }

    private boolean hasMethod(String methodName, Class<?>... parameterTypes) {
        return this.getMethod(methodName, parameterTypes) != null;
    }

    private Method getMethod(String methodName, Class<?>... parameterTypes) {
        try {
            final Method method = this.getClass().getMethod(methodName, parameterTypes);
            return method;
        } catch (SecurityException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Object callMethod(Method method, Object... args) {
        try {
            return method.invoke(this, args);
        } catch (IllegalArgumentException e) {
            log().error("Unable to invoke method", e);
        } catch (IllegalAccessException e) {
            log().error("Unable to invoke method", e);
        } catch (InvocationTargetException e) {
            log().error("Unable to invoke method", e);
        }
        return null;
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
