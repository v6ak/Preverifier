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
 *     Diego Sandin (Motorola)  - Fix errors after updating ASM library form 
 *                                version 2.2.2 to 3.0.0
 *     Vít Šesták				- package renamed, visibility reduced                                
 */
package v6.java.preverifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Value;

/**
 * Handler for a single method in the class. Capable of inlining subroutines and
 * creating the Stack map attribute.
 * <p>
 * FIXME This class need to be re-implemented
 * </p>
 * 
 * @author Craig Setera
 */
@SuppressWarnings("all")
class MethodRewriter {

    /**
     * A Label instance mapped to the new region and code offset after inlining
     * 
     * @author Craig Setera
     */
    public class MappedLabel extends Label {
        private Label originalLabel;

        /**
         * Construct a new instance
         */
        public MappedLabel(Label label) {
            super();
            originalLabel = label;
        }

        public Label getOriginalLabel() {
            return originalLabel;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return originalLabel.toString() + " -> " + super.toString();
        }
    }

    /** A region of instructions to be handled. */
    class Region {
        protected int endIndex;
        protected Map labelMap;
        protected Set<Label> labels;
        protected Region parentRegion;
        protected int startIndex;
        protected List<TryCatchBlockNode> tryCatchBlocks;

        /**
         * Construct a new instance.
         */
        Region() {
            this(0, 0);
        }

        /**
         * Construct a new instance.
         * 
         * @param startIndex
         * @param endIndex
         */
        Region(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;

            labelMap = new HashMap();
            labels = new HashSet<Label>();
            tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
        }

        /**
         * Find the label map recursively as necessary.
         * 
         * @param originalLabel
         * @return
         */
        protected Map<Label, Label> findLabelMap(Label originalLabel) {
            Map map = findLabelMapRecursive(originalLabel);
            return (map != null) ? map : labelMap;
        }

        /**
         * Find the label map recursively.
         * 
         * @param originalLabel
         * @return
         */
        protected Map findLabelMapRecursive(Label originalLabel) {
            Map map = null;

            if (labels.contains(originalLabel)) {
                map = labelMap;
            } else if (parentRegion != null) {
                map = parentRegion.findLabelMapRecursive(originalLabel);
            }

            return map;
        }

        /**
         * Find and return the specified label mapped appropriately for the
         * region or <code>null</code> if not found.
         * 
         * @param originalLabel
         * @return
         */
        protected Label findMappedLabel(Label originalLabel) {
            Label mappedLabel = (Label) labelMap.get(originalLabel);
            if ((mappedLabel == null) && (parentRegion != null)) {
                mappedLabel = parentRegion.findMappedLabel(originalLabel);
            }

            return mappedLabel;
        }

        /**
         * Add a new label to this region's list.
         * 
         * @param label
         */
        void addLabel(Label label) {
            labels.add(label);
        }

        /**
         * Add a new try catch block that is contained within this region.
         * 
         * @param tryCatchBlock
         */
        void addTryCatchBlock(TryCatchBlockNode tryCatchBlock) {
            tryCatchBlocks.add(tryCatchBlock);
        }

        /**
         * Return a boolean indicating whether the specified TryCatchBlockNode
         * is enclosed within this instruction region.
         * 
         * @param tryCatchBlock
         * @return
         */
        boolean encloses(TryCatchBlockNode tryCatchBlock) {
            return labels.contains(tryCatchBlock.start)
                    && labels.contains(tryCatchBlock.end);
        }

        /**
         * Enter the region. Do any setup for this region.
         * 
         * @param method
         */
        void enter(MethodNode method) {
            labelMap.clear();
            copyTryCatchBlocks(method, this, tryCatchBlocks);
        }

        /**
         * Exit the region. Do any cleanup for this region.
         * 
         * @param method
         */
        void exit(MethodNode method) {
        }

        /**
         * Return the length (in AbstractInsnNode instances) of this region.
         * 
         * @return
         */
        int getLength() {
            return endIndex - startIndex;
        }

        /**
         * Get the mapped label for the specified label based on the current
         * region.
         * 
         * @param originalLabel
         * @return
         */
        Label getMappedLabel(Label originalLabel) {
            Label mappedLabel = findMappedLabel(originalLabel);
            if (mappedLabel == null) {
                mappedLabel = new MappedLabel(originalLabel);

                Map<Label, Label> map = findLabelMap(originalLabel);
                map.put(originalLabel, mappedLabel);
            }

            return mappedLabel;
        }

        /**
         * Return the try catch blocks in this region.
         * 
         * @return
         */
        List<TryCatchBlockNode> getTryCatchBlocks() {
            return tryCatchBlocks;
        }

        /**
         * Returnn a boolean indicating whether the specified opcode at the
         * specified index in the method is a subroutine store opcode.
         * 
         * @param methodNode
         * @param index
         * @return
         */
        boolean isSubroutineReturnStore(MethodNode methodNode, int index) {
            return false;
        }

        /**
         * Store the parent region.
         * 
         * @param parentRegion
         */
        void setParentRegion(Region parentRegion) {
            this.parentRegion = parentRegion;
        }
    }

    /** Holder for information about a subroutine in a method */
    class Subroutine extends Region {
        Label label;
        int returnVariable;

        /**
         * Construct a new subroutine instance.
         * 
         * @param label
         */
        Subroutine(Label label) {
            super();
            this.label = label;
        }

        /**
         * @return Returns the returnVariable.
         */
        public int getReturnVariable() {
            return returnVariable;
        }

        /**
         * @param returnVariable The returnVariable to set.
         */
        public void setReturnVariable(int returnVariable) {
            this.returnVariable = returnVariable;
        }

        /* (non-Javadoc)
         * @see org.eclipse.mtj.internal.core.preverifier.MethodRewriter.Region#isSubroutineReturnStore(org.objectweb.asm.tree.MethodNode, int)
         */
        @Override
        boolean isSubroutineReturnStore(MethodNode methodNode, int index) {
            boolean isReturnStore = false;

//             AbstractInsnNode insnNode = getInstruction(methodNode, index);
//             if (insnNode.getOpcode() == Opcodes.ASTORE) {
//             VarInsnNode varInsnNode = (VarInsnNode) insnNode;
//             isReturnStore = (varInsnNode.var == returnVariable);
//             }

            return isReturnStore;
        }
    }

    private Map lineNumberMap;
    private Map<LabelNode, ArrayList<LocalVariableNode>> localVariableByEndLabelMap;

    private Map localVariableByStartLabelMap;
    private PreverifierMethodNode srcMethod;
    private Map subroutineMap;
    private MethodNode updatedMethod;

    /**
     * Construct a new rewriter instance.
     * 
     * @param classNode
     * @param srcMethod
     */
    public MethodRewriter(PreverificationClassNode classNode,
            PreverifierMethodNode srcMethod) {
        super();

        this.srcMethod = srcMethod;
        localVariableByEndLabelMap = new HashMap<LabelNode, ArrayList<LocalVariableNode>>();
    }

    /**
     * Return the method with subroutines inlined and an associated
     * StackMapAttribute.
     * 
     * @return
     * @throws AnalyzerException
     */
    public MethodNode getUpdatedMethod() throws AnalyzerException {
        boolean inliningRequired = (srcMethod.getJsrInstructionIndices().size() > 0);
        if (inliningRequired) {
            inlineSubroutines();
        } else {
            updatedMethod = srcMethod;
        }

        createStackMapAttribute();
        return updatedMethod;
    }

    /**
     * Add a new local variabled to the variables by end label map.
     * 
     * @param newLocalVariable
     */
    private void addNewLocalVariableByEnd(LocalVariableNode newLocalVariable) {
        ArrayList endLocalVariables = (ArrayList) localVariableByEndLabelMap
                .get(newLocalVariable.end);

        if (endLocalVariables == null) {
            endLocalVariables = new ArrayList();
            localVariableByEndLabelMap.put(newLocalVariable.end,
                    endLocalVariables);
        }

        endLocalVariables.add(newLocalVariable);
    }

    /**
     * Add a new stack map type.
     * 
     * @param localOrStack
     * @param region
     * @param label
     * @param value
     */
    private void addStackMapType(List localOrStack, Label label, Value value) {
        // BasicValue basicValue = (BasicValue) value;
        //
        // if (this == BasicValue.UNINITIALIZED_VALUE) {
        // localOrStack.add(newStackMapType(label,
        // StackMapType.ITEM_Uninitialized));
        // } else {
        // Type valueType = basicValue.getType();
        //
        // if (valueType == null) {
        // localOrStack.add(newStackMapType(label, StackMapType.ITEM_Top));
        // } else {
        // switch (valueType.getSort()) {
        // case Type.BOOLEAN:
        // case Type.BYTE:
        // case Type.CHAR:
        // case Type.INT:
        // case Type.SHORT:
        // localOrStack.add(newStackMapType(label,
        // StackMapType.ITEM_Integer));
        // break;
        //
        // case Type.DOUBLE:
        // localOrStack.add(newStackMapType(label,
        // StackMapType.ITEM_Double));
        // localOrStack.add(newStackMapType(label,
        // StackMapType.ITEM_Top));
        // break;
        //
        // case Type.FLOAT:
        // localOrStack.add(newStackMapType(label,
        // StackMapType.ITEM_Float));
        // break;
        //
        // case Type.LONG:
        // localOrStack.add(newStackMapType(label,
        // StackMapType.ITEM_Long));
        // localOrStack.add(newStackMapType(label,
        // StackMapType.ITEM_Top));
        // break;
        //
        // case Type.OBJECT: {
        // StackMapType mapType = null;
        //
        // String typeName = valueType.getInternalName();
        // if (typeName.equals("null")) {
        // mapType = newStackMapType(label, StackMapType.ITEM_Null);
        // } else {
        // mapType = newStackMapType(label,
        // StackMapType.ITEM_Object);
        // mapType.setObject(typeName);
        // }
        //
        // localOrStack.add(mapType);
        // break;
        // }
        //
        // case Type.ARRAY: {
        // StackMapType mapType = newStackMapType(label,
        // StackMapType.ITEM_Object);
        // mapType.setObject(valueType.toString());
        // localOrStack.add(mapType);
        // break;
        // }
        // }
        // }
        // }
    }

    /**
     * Copy the method node's metadata that is not changing.
     * 
     * @param method
     * @return
     */
    private MethodNode copyMethodMetadata(MethodNode method) {
        // Start a new method node
        String[] exceptions = (String[]) method.exceptions
                .toArray(new String[method.exceptions.size()]);

        return new MethodNode(method.access, method.name, method.desc,
                method.signature, exceptions);
    }

    /**
     * Copy the specified region of code potentially recursively.
     * 
     * @param method
     * @param region
     * @throws AnalyzerException
     */
    private void copyRegion(MethodNode method, Region region)
            throws AnalyzerException {
        // // Do any region-specific setup
        // region.enter(method);
        //
        // // Walk the instructions.. inlining as we go
        // for (int index = region.startIndex; index < region.endIndex; ++index)
        // {
        // AbstractInsnNode insnNode = getInstruction(srcMethod, index);
        //
        // // Special case for labels, as they may indicate the start of
        // // a subroutine that can be skipped.
        // if (insnNode.getType() == AbstractInsnNode.LABEL) {
        // Label label = ((LabelNode) insnNode).label;
        // visitLabel(method, region, label);
        //
        // if (isSubroutineStart(label)) {
        // // Skip over this subroutine implementation
        // Subroutine subroutine = (Subroutine) subroutineMap
        // .get(label);
        // index = subroutine.endIndex;
        // }
        // } else {
        // if (!region.isSubroutineReturnStore(srcMethod, index)) {
        // visitInstruction(method, region, insnNode);
        // }
        // }
        // }
        //
        // // Do any region-specific cleanup
        // region.exit(method);
    }

    /**
     * Copy all of the try/catch blocks in the method, realigning based on the
     * rewritten code.
     * 
     * @param methodNode
     * @param methodRegion
     */
    private void copyTryCatchBlocks(MethodNode methodNode, Region region,
            List blocks) {
        // Iterator tryCatchBlocks = blocks.iterator();
        // while (tryCatchBlocks.hasNext()) {
        // TryCatchBlockNode tryCatch = (TryCatchBlockNode) tryCatchBlocks
        // .next();
        // if (shouldCopy(tryCatch)) {
        // methodNode.visitTryCatchBlock(region
        // .getMappedLabel(tryCatch.start), region
        // .getMappedLabel(tryCatch.end), region
        // .getMappedLabel(tryCatch.handler), tryCatch.type);
        // }
        // }
    }

    /**
     * Create a mapping from line number location (Label) to the instance of the
     * line number nodes.
     * 
     * @return
     */
    private Map createLineNumberMap() {
        Map map = new HashMap();

        // Iterator lineNumbers = srcMethod.lineNumbers.iterator();
        // while (lineNumbers.hasNext()) {
        // LineNumberNode lineNumber = (LineNumberNode) lineNumbers.next();
        // map.put(lineNumber.start, lineNumber);
        // }

        return map;
    }

    /**
     * Create a mapping from local variable location (Label) to the instance of
     * the local variable nodes.
     * 
     * @return
     */
    private Map createLocalVariableMap() {
        Map map = new HashMap();

        Iterator localVariables = srcMethod.localVariables.iterator();
        while (localVariables.hasNext()) {
            LocalVariableNode localVariable = (LocalVariableNode) localVariables
                    .next();
            ArrayList startList = (ArrayList) map.get(localVariable.start);
            if (startList == null) {
                startList = new ArrayList();
                map.put(localVariable.start, startList);
            }

            startList.add(localVariable);
        }

        return map;
    }

    /**
     * Create a new StackMapAttribute for the method. This method also removes
     * any dead code that would cause the stack map attribute to be incorrect.
     * 
     * @throws AnalyzerException
     */
    private void createStackMapAttribute() throws AnalyzerException {

        // StackMapAttribute stackMapAttribute = new StackMapAttribute();
        //
        // //Attribute attribute = new Attribute("method");
        // Set targetLabels = findTargetLabels();
        //
        // // We need to have the verifier operating with the classpath
        // // of the class being rewritten
        // Interpreter interpreter = new SimpleVerifierPlusClassloader(classNode
        // .getClassLoader());
        // Analyzer analyzer = new Analyzer(interpreter);
        // Frame[] frames = analyzer.analyze(classNode.name, updatedMethod);
        //
        // int deadCodeFrameCount = 0;
        // int[] deadCodeIndices = new int[frames.length];
        //
        // for (int i = 0; i < updatedMethod.instructions.size(); i++) {
        // Frame frame = frames[i];
        //
        // // We only need to add stack map attributes for labels
        // // that are the target of certain instructions
        // AbstractInsnNode insnNode = getInstruction(updatedMethod, i);
        // if (insnNode.getType() == AbstractInsnNode.LABEL) {
        // LabelNode labelNode = (LabelNode) insnNode;
        //
        // if (targetLabels.contains(labelNode.getLabel())
        // && (frame != null)) {
        // stackMapAttribute.frames.add(newStackMapFrame(labelNode
        // .getLabel(), frame));
        // }
        // } else if (frame == null) {
        // // Track the indices of dead code for removal after
        // // building the stack map
        // deadCodeIndices[deadCodeFrameCount++] = i;
        // }
        //
        // }
        //
        // // Write out the newly created stack map attribute
        // // and remove any unnecessary dead code that would
        // // have an adverse effect on the stack map attribute
        // if (stackMapAttribute.frames.size() != 0) {
        // updatedMethod.visitAttribute(stackMapAttribute);
        //
        // // Remove the dead code in reverse order so that
        // // the indices remain correct after each removal
        // for (int i = deadCodeFrameCount - 1; i >= 0; i--) {
        // //updatedMethod.instructions.remove(deadCodeIndices[i]);
        // System.out.println("Remove the dead code");
        // }
        // }
    }

    /**
     * Scan the instructions in the source method and find the subroutines and
     * locations of any target labels.
     * 
     * @param method
     * @throws AnalyzerException
     */
    private void createSubroutineMap() throws AnalyzerException {
        subroutineMap = new HashMap();

        // // Look through the JSR instructions and collect the
        // // target labels. Those target labels are the starting
        // // points for the subroutines.
        // Set subroutineStartLabels = new HashSet();
        // Iterator jsrInstructionIndices = srcMethod.getJsrInstructionIndices()
        // .iterator();
        // while (jsrInstructionIndices.hasNext()) {
        // Integer instructionIndex = (Integer) jsrInstructionIndices.next();
        // JumpInsnNode jumpNode = (JumpInsnNode) getInstruction(srcMethod,
        // instructionIndex.intValue());
        // subroutineStartLabels.add(jumpNode.label);
        // }
        //
        // // Now, start searching for the subroutine starts. This
        // // must be done to account for nested subroutine implementations
        // if (subroutineStartLabels.size() > 0) {
        // for (int i = 0; i < srcMethod.instructions.size(); i++) {
        // AbstractInsnNode insnNode = getInstruction(srcMethod, i);
        // if (insnNode.getType() == AbstractInsnNode.LABEL) {
        // LabelNode labelNode = (LabelNode) insnNode;
        // if (subroutineStartLabels.contains(labelNode.label)) {
        // // This is the start of a subroutine
        // i = captureSubroutine(subroutineStartLabels,
        // subroutineMap, i, srcMethod,
        // (LabelNode) insnNode);
        // }
        // }
        // }
        // }
    }

    /**
     * Return the target labels used in generation of the stack map attribute.
     * 
     * @return
     */
    private Set findTargetLabels() {
        Set targetLabels = new HashSet();

        Iterator insns = updatedMethod.instructions.iterator();
        while (insns.hasNext()) {
            AbstractInsnNode insnNode = (AbstractInsnNode) insns.next();
            switch (insnNode.getType()) {
                case AbstractInsnNode.JUMP_INSN:
                    JumpInsnNode jumpInsnNode = (JumpInsnNode) insnNode;
                    targetLabels.add(jumpInsnNode.label);
                    break;

                case AbstractInsnNode.LOOKUPSWITCH_INSN: {
                    LookupSwitchInsnNode lookupSwitchNode = (LookupSwitchInsnNode) insnNode;
                    Iterator labels = lookupSwitchNode.labels.iterator();
                    while (labels.hasNext()) {
                        Label label = (Label) labels.next();
                        targetLabels.add(label);
                    }
                    targetLabels.add(lookupSwitchNode.dflt);
                }
                    break;

                case AbstractInsnNode.TABLESWITCH_INSN: {
                    TableSwitchInsnNode tableSwitchNode = (TableSwitchInsnNode) insnNode;
                    Iterator labels = tableSwitchNode.labels.iterator();
                    while (labels.hasNext()) {
                        Label label = (Label) labels.next();
                        targetLabels.add(label);
                    }
                    targetLabels.add(tableSwitchNode.dflt);
                }
                    break;
            }
        }

        Iterator blocks = updatedMethod.tryCatchBlocks.iterator();
        while (blocks.hasNext()) {
            TryCatchBlockNode tryCatchBlock = (TryCatchBlockNode) blocks.next();
            targetLabels.add(tryCatchBlock.handler);
        }

        return targetLabels;
    }

    /**
     * Return the instruction at the specified index.
     * 
     * @param method
     * @param index
     * @return
     */
    // private AbstractInsnNode getInstruction(MethodNode method, int index) {
    // List instructions = method.instructions;
    // return (AbstractInsnNode) ((index < instructions.size()) ? instructions
    // .get(index) : null);
    // }
    /**
     * Return the index of the specified label within the specified method node.
     * 
     * @param methodNode
     * @param label
     * @return
     */
    private int getLabelIndex(PreverifierMethodNode methodNode, Label label) {
        Integer i = methodNode.getLabelIndices().get(label);
        return i.intValue();
    }

    /**
     * Return the last instruction in the specified method.
     * 
     * @param method
     * @return
     */
    // private AbstractInsnNode getLastInstruction(MethodNode method) {
    // return getInstruction(method, method.instructions.size() - 1);
    // }
    /**
     * Map the specified list of labels into an array of labels mapped into the
     * target region.
     * 
     * @param region
     * @param labels
     * @return
     */
    private Label[] getMappedLabelArray(Region region, List labels) {
        Label[] mappedLabels = new Label[labels.size()];

        for (int i = 0; i < mappedLabels.length; i++) {
            mappedLabels[i] = region.getMappedLabel((Label) labels.get(i));
        }

        return mappedLabels;
    }

    /**
     * Inline all subroutines.
     * 
     * @throws AnalyzerException
     */
    private void inlineSubroutines() throws AnalyzerException {
        // // Set up the first instruction region to be
        // // processed as the entire method code...
        // Region methodRegion = new Region(0, srcMethod.instructions.size());
        //
        // // Scan through the instructions in the method and
        // // get organized
        // createSubroutineMap();
        // sortTryCatchBlocks(methodRegion);
        //
        // // Copy the non-code information
        // updatedMethod = copyMethodMetadata(srcMethod);
        //
        // // Set up some mapping information
        // lineNumberMap = createLineNumberMap();
        // localVariableByStartLabelMap = createLocalVariableMap();
        //
        // // Copy the instructions while inlining subroutines
        // copyRegion(updatedMethod, methodRegion);
        //
        // // The WTK reduces the visibility of the local variables
        // if (shouldReduceVariableVisibility(updatedMethod)) {
        // InsnList instructions = updatedMethod.instructions;
        // int instructionCount = instructions.size();
        // AbstractInsnNode lastLabel = (AbstractInsnNode) instructions
        // .remove(instructionCount - 1);
        // instructions.add(instructionCount - 2, lastLabel);
        // }
        //
        // updatedMethod.visitMaxs(srcMethod.maxStack, srcMethod.maxLocals);
    }

    /**
     * Return a method indicating whether the specified label is the start of a
     * referenced subroutine.
     * 
     * @param label
     * @return
     */
    private boolean isSubroutineStart(Label label) {
        return subroutineMap.containsKey(label);
    }

    /**
     * Remove the trailing TOP value from the list.
     * 
     * @param localsOrStack
     * @return
     */
    private boolean removeTrailingTop(ArrayList localsOrStack) {
        boolean removed = false;

        // if (localsOrStack.size() > 0) {
        // int lastIndex = localsOrStack.size() - 1;
        //
        // StackMapType type = (StackMapType) localsOrStack.get(lastIndex);
        // if (type.getType() == StackMapType.ITEM_Top) {
        // localsOrStack.remove(lastIndex);
        // removed = true;
        // }
        // }

        return removed;
    }

    /**
     * Create a new stack map frame.
     * 
     * @param region
     * @param label
     * @param frame
     * @return
     * @throws AnalyzerException
     */
    // private StackMapFrame newStackMapFrame(Label label, Frame frame)
    // throws AnalyzerException {
    // // Handle the locals
    // ArrayList locals = new ArrayList();
    // int localsCount = frame.getLocals();
    // for (int i = 0; i < localsCount; i++) {
    // addStackMapType(locals, label, frame.getLocal(i));
    // }
    // removeTrailingTops(locals);
    //
    // // Handle the stack
    // ArrayList stack = new ArrayList();
    // int stackCount = frame.getStackSize();
    // for (int i = 0; i < stackCount; i++) {
    // addStackMapType(stack, label, frame.getStack(i));
    // }
    // removeTrailingTops(stack);
    //
    // return new StackMapFrame(label, locals, stack);
    // }
    /**
     * Create a new StackMapType instance for the specified label.
     * 
     * @param label
     * @param typeCode
     * @return
     */
    // private StackMapType newStackMapType(Label label, int typeCode) {
    // StackMapType type = StackMapType.getTypeInfo(typeCode);
    // type.setLabel(label);
    // return type;
    // }
    /**
     * Remove the trailing TOP types from the specified locals or stack.
     * 
     * @param locals
     */
    private void removeTrailingTops(ArrayList localsOrStack) {
        while (removeTrailingTop(localsOrStack)) {
        }
    }

    /**
     * Capture the specified subroutine, potentially recursively capturing
     * nested subroutines. Captured subroutines are added to the map of
     * subroutines.
     * 
     * @param subroutineStartLabels
     * @param subroutineMap
     * @param index
     * @param method
     * @param labelNode
     * @return
     */
    // private int captureSubroutine(Set subroutineStartLabels, Map
    // subroutineMap,
    // int index, MethodNode method, LabelNode labelNode) {
    // Subroutine subroutine = new Subroutine(labelNode.label);
    //
    // subroutine.startIndex = index + 1;
    // for (subroutine.endIndex = subroutine.startIndex; true;
    // subroutine.endIndex++) {
    // AbstractInsnNode insn = getInstruction(method, subroutine.endIndex);
    //
    // if (insn.getType() == AbstractInsnNode.LABEL) {
    // Label label = ((LabelNode) insn).label;
    // if (subroutineStartLabels.contains(label)) {
    // subroutine.endIndex = captureSubroutine(
    // subroutineStartLabels, subroutineMap,
    // subroutine.endIndex, srcMethod, (LabelNode) insn);
    // } else {
    // subroutine.addLabel(label);
    // }
    // } else {
    // if (insn.getOpcode() == Opcodes.RET) {
    // // Figure out the variable that is being used
    // // for the return instruction
    // VarInsnNode varInsnNode = (VarInsnNode) insn;
    // int variableNumber = varInsnNode.var;
    // subroutine.setReturnVariable(variableNumber);
    //
    // break;
    // }
    // }
    // }
    //
    // // Add the newly found subroutine to the map
    // Label label = labelNode.label;
    // if (!subroutineMap.containsKey(label)) {
    // subroutineMap.put(label, subroutine);
    // }
    //
    // return subroutine.endIndex + 1;
    // }
    /**
     * Return a boolean indicating whether or not this try-catch block should be
     * copied to the new class.
     * 
     * @param tryCatch
     * @return
     */
    private boolean shouldCopy(TryCatchBlockNode tryCatch) {
        boolean shouldCopy = true;

        // int startIndex = getLabelIndex(srcMethod, tryCatch.start);
        // ArrayList insns = new ArrayList();
        //
        // for (int i = startIndex; i < srcMethod.instructions.size(); i++) {
        // AbstractInsnNode insn = getInstruction(srcMethod, i);
        //
        // if (insn.getType() == AbstractInsnNode.LABEL) {
        // LabelNode l = (LabelNode) insn;
        // if (l.label.equals(tryCatch.end)) {
        // break;
        // }
        // } else {
        // insns.add(insn);
        // }
        // }
        //
        // if (insns.size() == 1) {
        // AbstractInsnNode insn = (AbstractInsnNode) insns.get(0);
        // shouldCopy = (insn.getOpcode() != Opcodes.JSR);
        // }

        return shouldCopy;
    }

    /**
     * Return a boolean indicating whether the variable visibility should be
     * reduced.
     * 
     * @param method
     * @return
     */
    private boolean shouldReduceVariableVisibility(MethodNode method) {
        boolean shouldReduce = false;

        // if (getLastInstruction(method) instanceof LabelNode) {
        // List instructions = updatedMethod.instructions;
        // int instructionCount = instructions.size();
        //
        // AbstractInsnNode node = getInstruction(method, instructionCount - 2);
        // if (node instanceof InsnNode) {
        // InsnNode insnNode = (InsnNode) node;
        // switch (insnNode.getOpcode()) {
        // case Opcodes.IRETURN:
        // case Opcodes.LRETURN:
        // case Opcodes.FRETURN:
        // case Opcodes.DRETURN:
        // case Opcodes.ARETURN:
        // case Opcodes.RETURN:
        // case Opcodes.ATHROW:
        // shouldReduce = true;
        // break;
        // }
        // }
        // }

        return shouldReduce;
    }

    /**
     * Sort the try/catch blocks such that they are associated with the smallest
     * region that surrounds that block.
     * 
     * @param methodRegion
     */
    private void sortTryCatchBlocks(Region methodRegion) {
        Iterator blocks = srcMethod.tryCatchBlocks.iterator();
        while (blocks.hasNext()) {
            Region enclosingRegion = methodRegion;
            TryCatchBlockNode block = (TryCatchBlockNode) blocks.next();

            Iterator regions = subroutineMap.values().iterator();
            while (regions.hasNext()) {
                Region region = (Region) regions.next();
                if (region.encloses(block)) {
                    if (enclosingRegion == null) {
                        enclosingRegion = region;
                    } else {
                        // Pick the smallest region that encloses the block
                        if (region.getLength() < enclosingRegion.getLength()) {
                            enclosingRegion = region;
                        }
                    }
                }
            }

            enclosingRegion.addTryCatchBlock(block);
        }
    }

    /**
     * Visit the specified instruction and do the right thing.
     * 
     * @param method
     * @param region
     * @param insnNode
     * @throws AnalyzerException
     */
    private void visitInstruction(MethodNode method, Region region,
            AbstractInsnNode insnNode) throws AnalyzerException {
        int opcode = insnNode.getOpcode();
        switch (opcode) {
            case Opcodes.JSR:
                visitJumpToSubroutine(method, region, (JumpInsnNode) insnNode);
                break;

            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
            case Opcodes.GOTO:
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                visitJump(method, region, (JumpInsnNode) insnNode);
                break;

            case Opcodes.LOOKUPSWITCH:
                visitLookupSwitch(method, region,
                        (LookupSwitchInsnNode) insnNode);
                break;

            case Opcodes.TABLESWITCH:
                visitTableSwitch(method, region, (TableSwitchInsnNode) insnNode);
                break;

            default:
                insnNode.accept(method);
        }
    }

    /**
     * Visit the specified jump instructions, mapping the labels into the target
     * method.
     * 
     * @param method
     * @param region
     * @param jumpNode
     */
    private void visitJump(MethodNode method, Region region,
            JumpInsnNode jumpNode) {
        // Label mappedLabel = region.getMappedLabel(jumpNode.label);
        // JumpInsnNode newJumpNode = new JumpInsnNode(jumpNode.getOpcode(),
        // mappedLabel);
        // newJumpNode.accept(method);
    }

    /**
     * Visit a JSR instruction... Inlining the subroutine.
     * 
     * @param method
     * @param region
     * @param jumpNode
     * @throws AnalyzerException
     */
    private void visitJumpToSubroutine(MethodNode method, Region region,
            JumpInsnNode jumpNode) throws AnalyzerException {
        // // Back up and see if we need to remap a local variable label
        // // The WTK preverifier extends the scope of a local variable
        // // one instruction further if the last instruction of the block
        // // is a variable store instruction
        // AbstractInsnNode insnNode = getLastInstruction(updatedMethod);
        //
        // if (insnNode instanceof VarInsnNode) {
        // List instructions = updatedMethod.instructions;
        // AbstractInsnNode insnNode2 = (AbstractInsnNode) instructions
        // .get(instructions.size() - 2);
        //
        // if (insnNode2 instanceof LabelNode) {
        // // Looks like we have the correct situation here.
        // // Introduce a new label as the last instruction
        // // and add it to the label map to be used when
        // // adding the local variables
        // LabelNode labelNode = (LabelNode) insnNode2;
        // ArrayList localVariables = (ArrayList) localVariableByEndLabelMap
        // .get(labelNode.label);
        //
        // if (localVariables != null) {
        // Iterator vars = localVariables.iterator();
        // while (vars.hasNext()) {
        // LocalVariableNode var = (LocalVariableNode) vars.next();
        // var.end = new Label();
        // method.visitLabel(var.end);
        // }
        // }
        // }
        // }
        //
        // // Inline the subroutine
        // Subroutine subroutine = (Subroutine)
        // subroutineMap.get(jumpNode.label);
        // subroutine.setParentRegion(region);
        // copyRegion(method, subroutine);
    }

    /**
     * Visit the specified label in the context of the region. Handle all things
     * related to that label.
     * 
     * @param method
     * @param region
     * @param label
     */
    private void visitLabel(MethodNode method, Region region, Label label) {
        // Label mappedLabel = region.getMappedLabel(label);
        // method.visitLabel(mappedLabel);
        //
        // // Check for any related attributes
        // LineNumberNode lineNumber = (LineNumberNode)
        // lineNumberMap.get(label);
        // if (lineNumber != null) {
        // method.visitLineNumber(lineNumber.line, mappedLabel);
        // }
        //
        // ArrayList localVariables = (ArrayList) localVariableByStartLabelMap
        // .get(label);
        // if (localVariables != null) {
        // Iterator vars = localVariables.iterator();
        // while (vars.hasNext()) {
        // int localVariableCount = method.localVariables.size();
        // LocalVariableNode localVariable = (LocalVariableNode) vars
        // .next();
        //
        // method.visitLocalVariable(localVariable.name,
        // localVariable.desc, localVariable.signature, region
        // .getMappedLabel(localVariable.start), region
        // .getMappedLabel(localVariable.end),
        // localVariable.index);
        //
        // LocalVariableNode newLocalVariable = (LocalVariableNode)
        // method.localVariables
        // .get(localVariableCount);
        // addNewLocalVariableByEnd(newLocalVariable);
        // }
        // }
    }

    /**
     * Visit the specified instruction, mapping the labels into the target
     * method.
     * 
     * @param codeVisitor
     * @param region
     * @param node
     */
    private void visitLookupSwitch(MethodNode codeVisitor, Region region,
            LookupSwitchInsnNode node) {
        // Label dflt = region.getMappedLabel(node.dflt);
        //
        // int[] keys = new int[node.keys.size()];
        // for (int i = 0; i < keys.length; i++) {
        // keys[i] = ((Integer) node.keys.get(i)).intValue();
        // }
        //
        // Label[] labels = getMappedLabelArray(region, node.labels);
        //
        // LookupSwitchInsnNode newSwitch = new LookupSwitchInsnNode(dflt, keys,
        // labels);
        // newSwitch.accept(codeVisitor);
    }

    /**
     * Visit the specified instructions, mapping the labels into the target
     * method.
     * 
     * @param codeVisitor
     * @param region
     * @param node
     */
    private void visitTableSwitch(MethodNode codeVisitor, Region region,
            TableSwitchInsnNode node) {
        // Label dflt = region.getMappedLabel(node.dflt);
        // Label[] labels = getMappedLabelArray(region, node.labels);
        //
        // TableSwitchInsnNode newSwitch = new TableSwitchInsnNode(node.min,
        // node.max, dflt, labels);
        // newSwitch.accept(codeVisitor);
    }
}
