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
 *     Vít Šesták				- package renamed, toString() added
 */
package v6.java.preverifier.results;

import lombok.ToString;


/**
 * Implementation of the IFieldErrorInformation interface.
 * 
 * @author Craig Setera
 */
@ToString
public class FieldErrorInformation implements IFieldErrorInformation {

    private String name;
    private String typeDescription;

    /**
     * Construct a new field error information.
     * 
     * @param name
     * @param typeDescription
     */
    public FieldErrorInformation(String name, String typeDescription) {
        super();
        this.name = name;
        this.typeDescription = typeDescription;
    }

    /**
     * @see v6.java.preverifier.results.IFieldErrorInformation#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see v6.java.preverifier.results.IFieldErrorInformation#getTypeDescription()
     */
    public String getTypeDescription() {
        return typeDescription;
    }
}
