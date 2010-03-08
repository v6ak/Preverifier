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
 * Interface representing field level information
 * in a preverification error.
 * 
 * @author Craig Setera
 */
public interface IMethodErrorInformation {
	/**
	 * Return the name of the method in which the error occurred.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Return the type description information for the field in which the
	 * error occurred.
	 * 
	 * @return
	 */
	String getTypeDescription();
}
