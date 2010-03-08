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
package v6.java.preverifier.results;

/**
 * Interface representing class level information in a preverification error.
 * 
 * @author Craig Setera
 */
public interface IClassErrorInformation {
    
    /**
     * Return the name of the class in which the error occurred.
     * 
     * @return
     */
    String getName();

    /**
     * Return the source file for this class.
     * 
     * @return
     */
    String getSourceFile();
}
