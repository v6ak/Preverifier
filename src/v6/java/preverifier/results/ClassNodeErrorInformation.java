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

import org.objectweb.asm.tree.ClassNode;

/**
 * Implementation of the IClassErrorInformation interface wrapped around a class
 * node.
 * 
 * @author Craig Setera
 */
@ToString
public class ClassNodeErrorInformation implements IClassErrorInformation {

    private ClassNode classNode;

    /**
     * Construct a new information instance around the specified class node.
     * 
     * @param classNode
     */
    public ClassNodeErrorInformation(ClassNode classNode) {
        super();
        this.classNode = classNode;
    }

    /**
     * @see v6.java.preverifier.results.IClassErrorInformation#getName()
     */
    public String getName() {
        return classNode.name;
    }

    /**
     * @see v6.java.preverifier.results.IClassErrorInformation#getSourceFile()
     */
    public String getSourceFile() {
        return classNode.sourceFile;
    }
}
