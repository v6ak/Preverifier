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

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

/**
 * A subclass of the SimpleVerifier that allows a classloader to be specified
 * for use in locating and loading classes used during verification. This
 * verifier also extends the type system provided by the SimpleVerifier,
 * allowing boolean, byte, character and short arrays to be passed through the
 * type system.
 * 
 * @author Craig Setera
 */
public class SimpleVerifierPlusClassloader extends SimpleVerifier {
    private ClassLoader classLoader;

    /**
     * Construct a new verifier.
     * 
     * @param classLoader
     */
    public SimpleVerifierPlusClassloader(ClassLoader classLoader) {
        super();
        this.classLoader = classLoader;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.tree.analysis.SimpleVerifier#getClass(org.objectweb.asm.Type)
     */
    @SuppressWarnings("unchecked")
    protected Class getClass(Type t) {
        Class<?> clazz = null;

        if (classLoader == null) {
            clazz = super.getClass(t);
        } else {
            try {
                String className = getRootClassName(t);
                clazz = classLoader.loadClass(className);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e.toString());
            }
        }

        return clazz;
    }

    /**
     * Return the root class name for the specified type, having removed array
     * prefixes and such.
     * 
     * @param type
     * @return
     */
    protected String getRootClassName(Type type) {
        String rootClassName = null;

        switch (type.getSort()) {
        case Type.BOOLEAN:
            rootClassName = "java.lang.Boolean";
            break;

        case Type.CHAR:
            rootClassName = "java.lang.Character";
            break;

        case Type.BYTE:
            rootClassName = "java.lang.Byte";
            break;

        case Type.SHORT:
            rootClassName = "java.lang.Short";
            break;

        case Type.INT:
            rootClassName = "java.lang.Integer";
            break;

        case Type.FLOAT:
            rootClassName = "java.lang.Float";
            break;

        case Type.LONG:
            rootClassName = "java.lang.Long";
            break;

        case Type.DOUBLE:
            rootClassName = "java.lang.Double";
            break;

        case Type.ARRAY:
            rootClassName = getRootClassName(type.getElementType());
            break;

        case Type.OBJECT:
            rootClassName = type.getInternalName().replace('/', '.');
            break;
        }

        return rootClassName;
    }
}
