package core.fx.methodstmtbased;

import core.fx.base.Feature;
import core.fx.base.MethodStmtFEU;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;

public class IsReturnStmt implements MethodStmtFEU<Boolean> {

    /**
     * Whether the given stmt is the return statement of the target method
     */
    @Override
    public Feature<Boolean> extract(SootMethod target, Stmt stmt) {
        if(stmt instanceof JReturnStmt || stmt instanceof JReturnVoidStmt){
            if(target.getActiveBody().getUnits().contains(stmt)){
                return  new Feature<>(this.getClass().getSimpleName(), true);
            }
        }
        return  new Feature<>(this.getClass().getSimpleName(), false);
    }
}
