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
 * The location of the error.
 * 
 * @author Craig Setera
 */
@ToString
public class PreverificationErrorLocation {
    private PreverificationErrorLocationType locationType;
    private IClassErrorInformation classInformation;
    private IMethodErrorInformation methodInformation;
    private IFieldErrorInformation fieldInformation;
    private int lineNumber;

    /**
     * Construct a new location object.
     * 
     * @param locationType
     * @param classInformation
     * @param methodInformation
     * @param fieldInformation
     * @param lineNumber
     */
    public PreverificationErrorLocation(
            PreverificationErrorLocationType locationType,
            IClassErrorInformation classInformation,
            IMethodErrorInformation methodInformation,
            IFieldErrorInformation fieldInformation, int lineNumber) {
        super();

        this.classInformation = classInformation;
        this.fieldInformation = fieldInformation;
        this.lineNumber = lineNumber;
        this.locationType = locationType;
        this.methodInformation = methodInformation;
    }

    /**
     * Return information about the class in which the error occurred or
     * <code>null</code> if there is not class information.
     * 
     * @return Returns the class information.
     */
    public IClassErrorInformation getClassInformation() {
        return classInformation;
    }

    /**
     * Return information about the field in which the error occurred or
     * <code>null</code> if there is not field information.
     * 
     * @return Returns the field information.
     */
    public IFieldErrorInformation getFieldInformation() {
        return fieldInformation;
    }

    /**
     * @return Returns the lineNumber.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Return information about the type of the location in which the error
     * occurred or <code>null</code> if there is not location type
     * information.
     * 
     * @return Returns the locationType.
     */
    public PreverificationErrorLocationType getLocationType() {
        return locationType;
    }

    /**
     * Return information about the method in which the error occurred or
     * <code>null</code> if there is not method information.
     * 
     * @return Returns the methodInformation.
     */
    public IMethodErrorInformation getMethodInformation() {
        return methodInformation;
    }
}
