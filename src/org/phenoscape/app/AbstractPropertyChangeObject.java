package org.phenoscape.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public abstract class AbstractPropertyChangeObject implements PropertyChangeObject {
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        this.pcs.addPropertyChangeListener(l);
    }

    public void addPropertyChangeListener(String propName, PropertyChangeListener l) {
        this.pcs.addPropertyChangeListener(propName, l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        this.pcs.removePropertyChangeListener(l);
    }

    public void removePropertyChangeListener(String propName, PropertyChangeListener l) {
        this.pcs.removePropertyChangeListener(propName, l);
    }
    
    public void putValue(String propertyKey, Object value) {
        final String setter = this.setter(propertyKey);
        if (this.hasMethod(setter, this.getClass(propertyKey))) {
            this.callMethod(this.getMethod(setter, this.getClass(propertyKey)), value);
        } else {
            throw new UndefinedKeyException(propertyKey);
        }
    }
    
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

    public Class<?> getClass(String propertyKey) throws UndefinedKeyException {
        throw new UndefinedKeyException(propertyKey);
}

    protected void firePropertyChange(PropertyChangeEvent evt) {
        this.pcs.firePropertyChange(evt);
    }

    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        this.pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
        this.pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

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
