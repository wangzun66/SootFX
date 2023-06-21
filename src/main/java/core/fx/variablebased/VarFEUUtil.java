package core.fx.variablebased;

import soot.FastHierarchy;
import soot.Scene;
import soot.Type;
import soot.Value;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.*;

public class VarFEUUtil {
    public static boolean isRelatedType(Type type1, Type type2){
        FastHierarchy hierarchy = Scene.v().getOrMakeFastHierarchy();
        return hierarchy.canStoreType(type1, type2) || hierarchy.canStoreType(type2, type1);
    }

    public static boolean isConcernTo(Value op1, Value op2){
        return op1.equals(op2) || equalsFieldRef(op1, op2) || equalsArrayItem(op1, op2) || equalsQueryBase(op1, op2) || equalsFieldType(op1, op2);
    }

    private static boolean equalsFieldRef(Value op, Value queryVar) {
        if (op instanceof JInstanceFieldRef && queryVar instanceof JInstanceFieldRef) {
            return ((JInstanceFieldRef) queryVar).getBase().equals(((JInstanceFieldRef) op).getBase())
                    && ((JInstanceFieldRef) queryVar).getField().equals(((JInstanceFieldRef) op).getField());
        } else if (op instanceof StaticFieldRef && queryVar instanceof StaticFieldRef) {
            return ((StaticFieldRef) op).getFieldRef().equals(((StaticFieldRef) queryVar).getFieldRef());
        }
        return false;
    }

    private static boolean equalsArrayItem(Value op, Value queryVar) {
        if (op instanceof JArrayRef && queryVar instanceof JArrayRef) {
            return ((JArrayRef) queryVar).getBase().equals(((JArrayRef) op).getBase())
                    && ((JArrayRef) queryVar).getIndex().equals(((JArrayRef) op).getIndex());
        }
        return false;
    }

    private static boolean equalsQueryBase(Value op, Value queryVar) {
        if (queryVar instanceof JInstanceFieldRef) {
            Value queryBase = ((JInstanceFieldRef) queryVar).getBase();
            if (queryBase.equals(op)) {
                return true;
            }
        }
        return false;
    }

    private static boolean equalsFieldType(Value op, Value queryVar) {
        if (op instanceof JInstanceFieldRef) {
            Value base = ((JInstanceFieldRef) op).getBase();
            Type fieldType = ((JInstanceFieldRef) op).getField().getType();
            if (base.equals(queryVar) && fieldType.equals(queryVar.getType())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAllocOrMethodAssignment(Value rightOp, Value value) {
        if (rightOp instanceof JNewExpr) {
            JNewExpr newExpr = (JNewExpr) rightOp;
            Type type = newExpr.getType();
            if (type.equals(value.getType())) {
                return true;
            }
        } else if (rightOp instanceof JSpecialInvokeExpr
                || rightOp instanceof JStaticInvokeExpr
                || rightOp instanceof JVirtualInvokeExpr
                || rightOp instanceof JInterfaceInvokeExpr
                || rightOp instanceof JDynamicInvokeExpr) {
            return true;
        }
        return false;
    }
}
