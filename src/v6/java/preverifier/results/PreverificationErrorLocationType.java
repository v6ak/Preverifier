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
 * Enumeration type to represent the location of a preverication error.
 * 
 * @author Craig Setera
 */
public class PreverificationErrorLocationType {
    private static final String[] STRINGS = new String[] { "Class declaration",
            "Class field", "Method definition", "Method field",
            "Method instruction", "Unknown location" };

    /** The code representing a class declaration */
    public static final int CLASS_DEFINITION_CODE = 0;

    /** A class declaration */
    public static final PreverificationErrorLocationType CLASS_DEFINITION = new PreverificationErrorLocationType(
            CLASS_DEFINITION_CODE);

    /** The code representing a class field */
    public static final int CLASS_FIELD_CODE = 1;

    /** A class field location */
    public static final PreverificationErrorLocationType CLASS_FIELD = new PreverificationErrorLocationType(
            CLASS_FIELD_CODE);

    /** The code representing a method definition error */
    public static final int METHOD_SIGNATURE_CODE = 2;

    /** The method definition location */
    public static final PreverificationErrorLocationType METHOD_SIGNATURE = new PreverificationErrorLocationType(
            METHOD_SIGNATURE_CODE);

    /** The code representing a method field definition error */
    public static final int METHOD_FIELD_CODE = 3;

    /** A method field location */
    public static final PreverificationErrorLocationType METHOD_FIELD = new PreverificationErrorLocationType(
            METHOD_FIELD_CODE);

    /** The method instruction location code */
    public static final int METHOD_INSTRUCTION_CODE = 4;

    /** A method instruction location */
    public static final PreverificationErrorLocationType METHOD_INSTRUCTION = new PreverificationErrorLocationType(
            METHOD_INSTRUCTION_CODE);

    /** The code when the location of the error is unknown */
    public static final int UNKNOWN_LOCATION_CODE = 5;

    /** The location of the error is unknown */
    public static final PreverificationErrorLocationType UNKNOWN_LOCATION = new PreverificationErrorLocationType(
            UNKNOWN_LOCATION_CODE);

    // The code representing the location
    private int typeCode;

    /**
     * private constructor to limit options.
     * 
     * @param typeCode
     */
    private PreverificationErrorLocationType(int typeCode) {
        super();
        this.typeCode = typeCode;
    }

    /**
     * Return the location type code.
     * 
     * @return
     */
    public int getTypeCode() {
        return typeCode;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return STRINGS[typeCode];
    }
}
