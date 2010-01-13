package org.obo.app.swing;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

/**
 * An action which traverses the component hierarchy, beginning with the currently focused component, 
 * until it finds a object implementing a method with the name of the action's actionCommand.  The
 * method must accept no arguments.  If an object in the component hierarchy implements the method 
 * "getResponderDelegate" or is a JComponent with a client property "responderDelegate", the object 
 * returned will also be queried for an implementation of the actionCommand.
 * @author Jim Balhoff
 */
@SuppressWarnings("serial")
public class ResponderChainAction extends AbstractAction {

    private String actionCommand;
    private Object finalResponder = null;
    public static String DELEGATE_METHOD = "getResponderDelegate";
    public static String CLIENT_PROPERTY = "responderDelegate";

    /**
     * Creates a ResponderChainAction object which will try to invoke a method named by the given actionCommand.
     * The ResponderChainAction will use the actionCommand as the description string and a default icon.
     * @param actionCommand The name of the method this action invokes.
     */
    public ResponderChainAction(String actionCommand) {
        this(actionCommand, actionCommand, null);
    }

    /**
     * Creates a ResponderChainAction object which will try to invoke a method named by the given actionCommand.
     * The ResponderChainAction will use the specified description string and a default icon.
     * @param actionCommand The name of the method this action invokes.
     * @param name The action's description string.
     */
    public ResponderChainAction(String actionCommand, String name) {
        this(actionCommand, name, null);
    }

    /**
     * Creates a ResponderChainAction object which will try to invoke a method named by the given actionCommand.
     * The ResponderChainAction will use the specified description string and the specified icon.
     * @param actionCommand The name of the method this action invokes.
     * @param name The action's description string.
     * @param icon The action's icon.
     */
    public ResponderChainAction(String actionCommand, String name, Icon icon) {
        super(name, icon);
        this.setActionCommand(actionCommand);
    }

    /**
     * Invoked when an action occurs.  The action searches the component hierarchy for a object
     * which can respond to the action's actionCommand, and if successful invokes that method on the object.
     */
    public void actionPerformed(ActionEvent event) {
        final Object target = this.getValidResponder();
        if (target == null) {
            log().debug("No valid targets");
            return;
        }
        final Method method = this.getMethodForName(target, this.getActionCommand());
        this.callMethod(method, target);
    }

    public String getActionCommand() {
        return actionCommand;
    }

    public void setActionCommand(String actionCommand) {
        this.actionCommand = actionCommand;
    }

    public Object getFinalResponder() {
        return this.finalResponder;
    }

    public void setFinalResponder(Object anObject) {
        this.finalResponder = anObject;
    }

    private Component getFocusOwner() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
    }

    /**
     * Returns the first found object which implements a method named by the action's
     * actionCommand, or null if none is found.
     */
    private Object getValidResponder() {
        if (this.getActionCommand() == null) return null;
        Object responder = this.getNextResponder(null);
        while (responder != null) {
            if (this.objectRespondsToMethod(responder, this.getActionCommand())) {
                return responder;
            } else if (this.objectHasDelegateForMethod(responder, this.getActionCommand())) {
                return this.getDelegateForObject(responder);
            } else {
                responder = this.getNextResponder(responder);
            }
        }
        return responder;
    }

    /**
     * Returns the next object which should be queried for an implementation of the
     * actionCommand. This is typically the parent Component of an existing Component,
     * but could also be an object returned by getResponderDelegate or the client property 
     * "responderDelegate". If null is passed, the currently focused Component is returned.
     */
    private Object getNextResponder(Object currentResponder) {
        if (currentResponder == null) return this.getFocusOwner();
        if (currentResponder instanceof Component) {
            final Component parent = ((Component)currentResponder).getParent();
            if (parent != null) {
                return parent;
            }
        }
        return this.getFinalResponder();
    }

    private boolean objectRespondsToMethod(Object anObject, String methodName) {
        return this.getMethodForName(anObject, methodName) != null;
    }

    private boolean objectHasDelegateForMethod(Object anObject, String methodName) {
        final Object delegate = this.getDelegateForObject(anObject);
        if (delegate != null) {
            return this.objectRespondsToMethod(delegate, methodName);
        } else {
            return false;
        }
    }

    private Method getMethodForName(Object anObject, String methodName) {
        try {
            final Method method = anObject.getClass().getMethod(methodName);
            return method;
        } catch (SecurityException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Object getDelegateForObject(Object anObject) {
        if (this.objectRespondsToMethod(anObject, DELEGATE_METHOD)) {
            final Method method = this.getMethodForName(anObject, DELEGATE_METHOD);
            return this.callMethod(method, anObject);
        } else if (anObject instanceof JComponent) {
            return ((JComponent)anObject).getClientProperty(CLIENT_PROPERTY);
        }
        else {
            return null;
        }
    }

    private Object callMethod(Method method, Object anObject) {
        try {
            return method.invoke(anObject);
        } catch (IllegalArgumentException e) {
            log().error("Unable to invoke method on target", e);
        } catch (IllegalAccessException e) {
            log().error("Unable to invoke method on target", e);
        } catch (InvocationTargetException e) {
            log().error("Unable to invoke method on target", e);
        }
        return null;
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
