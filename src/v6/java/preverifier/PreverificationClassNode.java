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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import v6.java.preverifier.results.ClassNodeErrorInformation;
import v6.java.preverifier.results.FieldErrorInformation;
import v6.java.preverifier.results.MethodNodeErrorInformation;
import v6.java.preverifier.results.PreverificationError;
import v6.java.preverifier.results.PreverificationErrorLocation;
import v6.java.preverifier.results.PreverificationErrorLocationType;
import v6.java.preverifier.results.PreverificationErrorType;

/**
 * A class visitor that provides necessary functionality for traversing and
 * handling the structure changes required by the preverifier to a class.
 * 
 * @author Craig Setera
 */
public class PreverificationClassNode extends ClassNode {

    // Type definitions
    private static final Type BOOLEAN_TYPE = Type
            .getType("Ljava/lang/Boolean;");
    private static final Type BYTE_TYPE = Type.getType("Ljava/lang/Byte;");
    private static final Type CHARACTER_TYPE = Type
            .getType("Ljava/lang/Character;");
    private static final Type DOUBLE_TYPE = Type.getType("Ljava/lang/Double;");
    private static final Type FLOAT_TYPE = Type.getType("Ljava/lang/Float;");
    private static final Type INTEGER_TYPE = Type
            .getType("Ljava/lang/Integer;");
    private static final Type LONG_TYPE = Type.getType("Ljava/lang/Long;");
    private static final Type SHORT_TYPE = Type.getType("Ljava/lang/Short;");
    private static final Type VOID_TYPE = Type.getType("Ljava/lang/Void;");

    private ClassLoader classLoader;
    private Class<?> doubleClass;
    private ArrayList<PreverificationError> errorList;
    private Class<?> floatClass;

    // Some class instances in the correct classloader
    private Class<?> objectClass;
    // Implementation
    private IPreverificationPolicy preverificationPolicy;
    private Map<Type, PreverificationErrorType> validTypes;

    /**
     * Construct a class adapter for preverification.
     * 
     * @param preverificationPolicy
     * @param classpath
     */
    public PreverificationClassNode(
            IPreverificationPolicy preverificationPolicy,
            ClassLoader classloader) {
        this.preverificationPolicy = preverificationPolicy;
        this.errorList = new ArrayList<PreverificationError>();
        this.classLoader = classloader;

        validTypes = new HashMap<Type, PreverificationErrorType>();

        // Capture some class instances in the specified classloader
        try {
            objectClass = Class.forName("java.lang.Object", true, classLoader);
            floatClass = Class.forName("java.lang.Float", true, classLoader);
            doubleClass = Class.forName("java.lang.Double", true, classLoader);
        } catch (ClassNotFoundException e) {
            // Shouldn't happen
        }
    }

    /**
     * Return the list of errors accumulated during the traversal.
     * 
     * @return
     */
    public List<PreverificationError> getErrorList() {
        return errorList;
    }

    /**
     * Return a boolean indicating whether or not this class node has any
     * errors.
     * 
     * @return
     */
    public boolean hasError() {
        return errorList.size() > 0;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.tree.ClassNode#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public FieldVisitor visitField(final int access, final String name,
            final String desc, final String signature, final Object value) {
        FieldVisitor fieldVisitor = null;

        Type type = Type.getType(desc);
        PreverificationErrorType error = validateType(type);

        if (error == PreverificationErrorType.NO_ERROR) {
            // To match up with the WTK output, we want to make sure to strip
            // off potential constant values from non-static fields.
            Object constantValue = ((access & Opcodes.ACC_STATIC) != 0) ? value
                    : null;
            fieldVisitor = super.visitField(access, name, desc, signature,
                    constantValue);
        } else {
            ClassNodeErrorInformation classInfo = new ClassNodeErrorInformation(
                    this);
            FieldErrorInformation fieldInfo = new FieldErrorInformation(name,
                    desc);
            PreverificationErrorLocation location = new PreverificationErrorLocation(
                    PreverificationErrorLocationType.CLASS_FIELD, classInfo,
                    null, fieldInfo, -1);
            PreverificationError fieldError = new PreverificationError(error,
                    location, null);
            getErrorList().add(fieldError);
        }

        return fieldVisitor;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.tree.ClassNode#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String name,
            final String desc, final String signature, final String[] exceptions) {
        MethodNode mn = new PreverifierMethodNode(this, access, name, desc,
                signature, exceptions);

        boolean isNativeError = !preverificationPolicy
                .areNativeMethodsAllowed()
                && ((access & Opcodes.ACC_NATIVE) != 0);
        boolean isFinalizerError = !preverificationPolicy
                .areFinalizersAllowed()
                && name.equals("finalize")
                && (Type.getArgumentTypes(desc).length == 0);
        PreverificationErrorType signatureErrorType = getMethodSignatureError(desc);
        boolean isInvalidMethodSignature = (signatureErrorType != PreverificationErrorType.NO_ERROR);

        if (isNativeError || isFinalizerError || isInvalidMethodSignature) {
            ClassNodeErrorInformation classInfo = new ClassNodeErrorInformation(
                    this);
            MethodNodeErrorInformation methodInfo = new MethodNodeErrorInformation(
                    classInfo, mn);
            PreverificationErrorLocation location = new PreverificationErrorLocation(
                    PreverificationErrorLocationType.METHOD_SIGNATURE,
                    classInfo, methodInfo, null, -1);

            if (isNativeError) {
                PreverificationError error = new PreverificationError(
                        PreverificationErrorType.NATIVE, location, null);
                getErrorList().add(error);
            }

            if (isFinalizerError) {
                PreverificationError error = new PreverificationError(
                        PreverificationErrorType.FINALIZERS, location, null);
                getErrorList().add(error);
            }

            if (isInvalidMethodSignature) {
                PreverificationError error = new PreverificationError(
                        signatureErrorType, location, null);
                getErrorList().add(error);
            }
        }

        return mn;
    }

    /**
     * Return a boolean indicating whether the method signature is invalid.
     * 
     * @param desc
     * @return
     */
    private PreverificationErrorType getMethodSignatureError(String desc) {
        Type returnType = Type.getReturnType(desc);
        PreverificationErrorType errorType = validateType(returnType);

        if (errorType != PreverificationErrorType.NO_ERROR) {
            Type[] paramTypes = Type.getArgumentTypes(desc);
            for (int i = 0; (i < paramTypes.length)
                    && (errorType != PreverificationErrorType.NO_ERROR); i++) {
                errorType = validateType(paramTypes[i]);
            }
        }

        return errorType;
    }

    /**
     * Return the root type of the specified type, stripping away the array and
     * primitive wrappers.
     * 
     * @param type
     * @return
     */
    private Type getObjectType(Type type) {
        Type objectType = type;

        switch (type.getSort()) {
            // Convert primitive types to object types
            case Type.BOOLEAN:
                objectType = BOOLEAN_TYPE;
                break;
            case Type.CHAR:
                objectType = CHARACTER_TYPE;
                break;
            case Type.BYTE:
                objectType = BYTE_TYPE;
                break;
            case Type.SHORT:
                objectType = SHORT_TYPE;
                break;
            case Type.INT:
                objectType = INTEGER_TYPE;
                break;
            case Type.FLOAT:
                objectType = FLOAT_TYPE;
                break;
            case Type.LONG:
                objectType = LONG_TYPE;
                break;
            case Type.DOUBLE:
                objectType = DOUBLE_TYPE;
                break;
            case Type.VOID:
                objectType = VOID_TYPE;
                break;

            case Type.ARRAY:
                objectType = getObjectType(type.getElementType());
                break;
        }

        return objectType;
    }

    /**
     * Attempt to locate the class that defines the specified type instance.
     * 
     * @param objectType
     * @return
     * @throws ClassNotFoundException
     */
    private Class<?> getTypeClass(Type objectType)
            throws ClassNotFoundException {
        String dottedName = objectType.getInternalName().replace('/', '.');
        return Class.forName(dottedName, true, classLoader);
    }

    /**
     * Return a boolean indicating whether or not the specified type is
     * disallowed.
     * 
     * @param clazz
     * @return
     */
    private boolean isDisallowedType(Class<?> clazz) {
        boolean disallowed = false;

        if (!getPreverificationPolicy().isFloatingPointAllowed()) {
            disallowed = isFloatingPointType(clazz);
        }

        return disallowed;
    }

    /**
     * Return a boolean indicating whether or not the specified type is a
     * floating point type.
     * 
     * @param clazz
     * @return
     */
    private boolean isFloatingPointType(Class<?> clazz) {
        return (clazz == floatClass) || (clazz == doubleClass);
    }

    /**
     * Validate the hierarchy to see if it is ok.
     * 
     * @param clazz
     * @return
     */
    private PreverificationErrorType validateHierarchy(Class<?> clazz) {
        PreverificationErrorType error = null;

        if ((clazz == null) || (clazz == objectClass)) {
            error = PreverificationErrorType.NO_ERROR;
        } else {
            if (isDisallowedType(clazz)) {
                // Assumption is that the only disallowed types are floating
                // point types
                error = PreverificationErrorType.FLOATING_POINT;
            } else {
                error = validateHierarchy(clazz.getSuperclass());
            }
        }

        return error;
    }

    /**
     * Return a classloader for the verification classpath.
     * 
     * @return
     */
    ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Return the preverification policy in use.
     * 
     * @return
     */
    IPreverificationPolicy getPreverificationPolicy() {
        return preverificationPolicy;
    }

    /**
     * Return an error code concerning the validity of the specified type.
     * 
     * @param type
     * @return
     */
    PreverificationErrorType validateType(Type type) {
        PreverificationErrorType errorCode = validTypes.get(type);

        if (errorCode == null) {
            Type objectType = getObjectType(type);

            try {
                Class<?> clazz = getTypeClass(objectType);
                errorCode = validateHierarchy(clazz);
            } catch (ClassNotFoundException e) {
                errorCode = PreverificationErrorType.MISSING_TYPE;
            }

            validTypes.put(type, errorCode);
        }

        return errorCode;
    }
}
