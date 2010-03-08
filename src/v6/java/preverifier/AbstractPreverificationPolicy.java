/**
 * Copyright (c) 2003,2008 Craig Setera and others.
 * Copyright (c) 2010 Vít Šesták
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Craig Setera (EclipseME) - Initial implementation
 *     Diego Sandin (Motorola)  - Refactoring package name to follow eclipse 
 *                                standards
 *     Vít Šesták				- package renamed
 */
package v6.java.preverifier;


/**
 * Abstract superclass of the preverification policies.
 * 
 * @author Craig Setera
 */
public abstract class AbstractPreverificationPolicy implements
        IPreverificationPolicy {
    
    /**
     * Construct a new policy instance.
     * 
     * @param traceEnabled
     */
    public AbstractPreverificationPolicy() {
        super();
    }

    /**
     * @see v6.java.preverifier.IPreverificationPolicy#areFinalizersAllowed()
     */
    public boolean areFinalizersAllowed() {
        return false;
    }

    /**
     * @see v6.java.preverifier.IPreverificationPolicy#areNativeMethodsAllowed()
     */
    public boolean areNativeMethodsAllowed() {
        return false;
    }
}
