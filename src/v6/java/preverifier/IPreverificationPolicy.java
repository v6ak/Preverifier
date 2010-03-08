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
 * Interface that provides the policy information used during the process of
 * doing preverification.
 * 
 * @author Craig Setera
 */
public interface IPreverificationPolicy {
    
    /**
     * Return a boolean indicating whether finalizer methods are allowed.
     * 
     * @return
     */
    public boolean areFinalizersAllowed();

    /**
     * Return a boolean indicating whether native methods are allowed.
     * 
     * @return
     */
    public boolean areNativeMethodsAllowed();

    /**
     * Return a boolean indicating whether floating point is allowed.
     * 
     * @return
     */
    public boolean isFloatingPointAllowed();
}
