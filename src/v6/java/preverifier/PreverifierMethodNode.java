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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import v6.java.preverifier.results.ClassNodeErrorInformation;
import v6.java.preverifier.results.FieldErrorInformation;
import v6.java.preverifier.results.MethodNodeErrorInformation;
import v6.java.preverifier.results.PreverificationError;
import v6.java.preverifier.results.PreverificationErrorLocation;
import v6.java.preverifier.results.PreverificationErrorLocationType;
import v6.java.preverifier.results.PreverificationErrorType;

/**
 * MethodNode subclass that does preverification of the associated method code
 * as well as looking for preverification errors.
 * 
 * @author Craig Setera
 */
@SuppressWarnings("unchecked")
public class PreverifierMethodNode extends MethodNode {

    private PreverificationClassNode classNode;
    private int lineNumber;
    private List<Integer> jsrInstructionIndices;
    private Map<Label, Integer> labelIndices;

    /**
     * Construct a new method node.
     * 
     * @param access
     * @param name
     * @param desc
     * @param exceptions
     * @param attrs
     */
    public PreverifierMethodNode(PreverificationClassNode classNode,
            int access, String name, String desc, String signature,
            String[] exceptions) {
        super(access, name, desc, signature, exceptions);
        this.classNode = classNode;
        lineNumber = -1;
        jsrInstructionIndices = new ArrayList<Integer>();
        labelIndices = new HashMap<Label, Integer>();
    }

    /**
     * Return the indices of the JSR instruction encountered during visitation.
     * 
     * @return
     */
    List<Integer> getJsrInstructionIndices() {
        return jsrInstructionIndices;
    }

    /**
     * Return the map from label instances to their indices within the list of
     * instructions.
     * 
     * @return
     */
    Map<Label, Integer> getLabelIndices() {
        return labelIndices;
    }


    /* (non-Javadoc)
     * @see org.objectweb.asm.tree.MemberNode#visitEnd()
     */
    public void visitEnd() {
        boolean hasNoCode = ((access & Opcodes.ACC_NATIVE) != 0)
                || ((access & Opcodes.ACC_ABSTRACT) != 0);

        if (hasNoCode) {
            classNode.methods.add(this);
        } else {
            MethodRewriter handler = new MethodRewriter(classNode, this);
            try {
                MethodNode updatedMethod = handler.getUpdatedMethod();
                classNode.methods.add(updatedMethod);
            } catch (AnalyzerException e) {
                throw new RuntimeException("Method " + name + ": "
                        + e.getMessage(), e);
            }
        }
    }

    /**
     * @see org.objectweb.asm.MethodVisitor#visitFieldInsn(int,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public void visitFieldInsn(int opcode, String owner, String name,
            String desc) {
        if (isDisallowedInstruction(opcode)) {
            ClassNodeErrorInformation classInfo = new ClassNodeErrorInformation(
                    classNode);
            MethodNodeErrorInformation methodInfo = new MethodNodeErrorInformation(
                    classInfo, this);
            FieldErrorInformation fieldInfo = new FieldErrorInformation(name,
                    desc);

            PreverificationErrorLocation location = new PreverificationErrorLocation(
                    PreverificationErrorLocationType.METHOD_FIELD, classInfo,
                    methodInfo, fieldInfo, lineNumber);
            PreverificationError error = new PreverificationError(
                    PreverificationErrorType.FLOATING_POINT, location, null);
            addError(error);
        }

        super.visitFieldInsn(opcode, owner, name, desc);
    }

    /**
     * @see org.objectweb.asm.MethodVisitor#visitIincInsn(int, int)
     */
    public void visitIincInsn(int var, int increment) {
        // TODO Fix this...
        super.visitIincInsn(var, increment);
    }

    /**
     * @see org.objectweb.asm.MethodVisitor#visitInsn(int)
     */
    public void visitInsn(int opcode) {
        if (isDisallowedInstruction(opcode)) {
            ClassNodeErrorInformation classInfo = new ClassNodeErrorInformation(
                    classNode);
            MethodNodeErrorInformation methodInfo = new MethodNodeErrorInformation(
                    classInfo, this);

            PreverificationErrorLocation location = new PreverificationErrorLocation(
                    PreverificationErrorLocationType.METHOD_INSTRUCTION,
                    classInfo, methodInfo, null, lineNumber);
            PreverificationError error = new PreverificationError(
                    PreverificationErrorType.FLOATING_POINT, location, null);
            addError(error);
        }

        super.visitInsn(opcode);
    }

    /**
     * @see org.objectweb.asm.MethodVisitor#visitIntInsn(int, int)
     */
    public void visitIntInsn(int opcode, int operand) {
        if (opcode == Opcodes.NEWARRAY) {
            if ((operand == Opcodes.T_DOUBLE) || (operand == Opcodes.T_FLOAT)) {
                ClassNodeErrorInformation classInfo = new ClassNodeErrorInformation(
                        classNode);
                MethodNodeErrorInformation methodInfo = new MethodNodeErrorInformation(
                        classInfo, this);

                PreverificationErrorLocation location = new PreverificationErrorLocation(
                        PreverificationErrorLocationType.METHOD_INSTRUCTION,
                        classInfo, methodInfo, null, lineNumber);
                PreverificationError error = new PreverificationError(
                        PreverificationErrorType.FLOATING_POINT, location, null);
                addError(error);
            }
        }

        super.visitIntInsn(opcode, operand);
    }

    /**
     * @see org.objectweb.asm.CodeVisitor#visitLocalVariable(java.lang.String,
     *      java.lang.String, org.objectweb.asm.Label, org.objectweb.asm.Label,
     *      int)
     */
    public void visitLocalVariable(String name, String desc, String signature,
            Label start, Label end, int index) {
        if (isDisallowedType(desc)) {
            ClassNodeErrorInformation classInfo = new ClassNodeErrorInformation(
                    classNode);
            MethodNodeErrorInformation methodInfo = new MethodNodeErrorInformation(
                    classInfo, this);
            FieldErrorInformation fieldInfo = new FieldErrorInformation(name,
                    desc);

            PreverificationErrorLocation location = new PreverificationErrorLocation(
                    PreverificationErrorLocationType.METHOD_FIELD, classInfo,
                    methodInfo, fieldInfo, lineNumber);
            PreverificationError error = new PreverificationError(
                    PreverificationErrorType.FLOATING_POINT, location, null);
            addError(error);
        }

        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    /**
     * @see org.objectweb.asm.CodeVisitor#visitMultiANewArrayInsn(java.lang.String,
     *      int)
     */
    public void visitMultiANewArrayInsn(String desc, int dims) {
        if (isDisallowedType(desc)) {
            ClassNodeErrorInformation classInfo = new ClassNodeErrorInformation(
                    classNode);
            MethodNodeErrorInformation methodInfo = new MethodNodeErrorInformation(
                    classInfo, this);

            PreverificationErrorLocation location = new PreverificationErrorLocation(
                    PreverificationErrorLocationType.METHOD_INSTRUCTION,
                    classInfo, methodInfo, null, lineNumber);
            PreverificationError error = new PreverificationError(
                    PreverificationErrorType.FLOATING_POINT, location, null);
            addError(error);
        }

        super.visitMultiANewArrayInsn(desc, dims);
    }

    /**
     * @see org.objectweb.asm.CodeVisitor#visitVarInsn(int, int)
     */
    public void visitVarInsn(int opcode, int var) {
        // TODO Fix this... addFloatingPointErrorAsNecessary(opcode);
        super.visitVarInsn(opcode, var);
    }

    /**
     * @see org.objectweb.asm.CodeVisitor#visitJumpInsn(int,
     *      org.objectweb.asm.Label)
     */
    public void visitJumpInsn(int opcode, Label label) {
        if (isDisallowedInstruction(opcode)) {
            ClassNodeErrorInformation classInfo = new ClassNodeErrorInformation(
                    classNode);
            MethodNodeErrorInformation methodInfo = new MethodNodeErrorInformation(
                    classInfo, this);

            PreverificationErrorLocation location = new PreverificationErrorLocation(
                    PreverificationErrorLocationType.METHOD_INSTRUCTION,
                    classInfo, methodInfo, null, lineNumber);
            PreverificationError error = new PreverificationError(
                    PreverificationErrorType.FLOATING_POINT, location, null);
            addError(error);
        } else if (opcode == Opcodes.JSR) {
            // Don't follow JSR's as they will be removed anyway...
            jsrInstructionIndices.add(new Integer(instructions.size()));
        }

        super.visitJumpInsn(opcode, label);
    }

    /**
     * @see org.objectweb.asm.tree.MethodNode#visitLabel(org.objectweb.asm.Label)
     */
    public void visitLabel(Label label) {
        labelIndices.put(label, new Integer(instructions.size()));
        super.visitLabel(label);
    }

    /**
     * @see org.objectweb.asm.CodeVisitor#visitLineNumber(int,
     *      org.objectweb.asm.Label)
     */
    public void visitLineNumber(int line, Label start) {
        lineNumber = line;
        super.visitLineNumber(line, start);
    }

    /**
     * Add a new error to the list of errors.
     */
    private void addError(PreverificationError error) {
        classNode.getErrorList().add(error);
    }

    /**
     * Return a boolean indicating whether the specified opcode is a disallowed
     * floating point Opcodes.
     * 
     * @param opcode
     * @return
     */
    private boolean isDisallowedInstruction(int opcode) {
        return !classNode.getPreverificationPolicy().isFloatingPointAllowed()
                && isFloatingPointOpcode(opcode);
    }

    /**
     * Return a boolean indicating whether the specifie type description is a
     * disallowed floating point Opcodes.
     * 
     * @param typeDescription
     * @return
     */
    private boolean isDisallowedType(String typeDescription) {
        Type type = Type.getType(typeDescription);
        PreverificationErrorType error = classNode.validateType(type);
        return error != PreverificationErrorType.NO_ERROR;
    }

    /**
     * Return a boolean indicating whether the specified opcode is
     * a floating point Opcodes.
     * 
     * @param opcode
     * @return
     */
    private boolean isFloatingPointOpcode(int opcode) {
        boolean isFloatingPointOpcode = false;

        switch (opcode) {
        case Opcodes.FCONST_0:
        case Opcodes.FCONST_1:
        case Opcodes.FCONST_2:
        case Opcodes.DCONST_0:
        case Opcodes.DCONST_1:
        case Opcodes.FLOAD:
        case Opcodes.DLOAD:
        case Opcodes.FSTORE:
        case Opcodes.DSTORE:
        case Opcodes.FALOAD:
        case Opcodes.DALOAD:
        case Opcodes.FASTORE:
        case Opcodes.DASTORE:
        case Opcodes.FADD:
        case Opcodes.DADD:
        case Opcodes.FSUB:
        case Opcodes.DSUB:
        case Opcodes.FMUL:
        case Opcodes.DMUL:
        case Opcodes.FDIV:
        case Opcodes.DDIV:
        case Opcodes.FREM:
        case Opcodes.DREM:
        case Opcodes.FNEG:
        case Opcodes.DNEG:
        case Opcodes.FCMPG:
        case Opcodes.FCMPL:
        case Opcodes.DCMPG:
        case Opcodes.DCMPL:
        case Opcodes.I2F:
        case Opcodes.F2I:
        case Opcodes.I2D:
        case Opcodes.D2I:
        case Opcodes.L2F:
        case Opcodes.L2D:
        case Opcodes.F2L:
        case Opcodes.D2L:
        case Opcodes.F2D:
        case Opcodes.D2F:
        case Opcodes.FRETURN:
        case Opcodes.DRETURN:
            isFloatingPointOpcode = true;
            break;
        }

        return isFloatingPointOpcode;
    }
}
