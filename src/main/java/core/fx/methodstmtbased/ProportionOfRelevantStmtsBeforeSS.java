package core.fx.methodstmtbased;

import core.fx.base.Feature;
import core.fx.base.MethodStmtFEU;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import java.util.*;

public class ProportionOfRelevantStmtsBeforeSS implements MethodStmtFEU<Double> {

    /**
     * AAS: Maximum Proportion of AAS Sparse-CFG
     * @param target method
     * @param tail  query stmt
     * @return
     */
    @Override
    public Feature<Double> extract(SootMethod target, Stmt tail) {
        int count = getRelevantStmtsCount(target, tail);
        int methodSize = target.getActiveBody().getUnits().size();
        if(methodSize == 0){
            methodSize = 1;
        }
        double proportion = (double) count / (double) methodSize;
        return new Feature<>(this.getClass().getSimpleName(), Math.floor(proportion*10 + 0.5)/10);
    }

    private int getRelevantStmtsCount(SootMethod target, Stmt stmt){
        Set<Unit> visited = new HashSet<>();
        Set<Unit> relevantStmts = new HashSet<>();
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        Deque<Unit> unitsInCurrentDepth = new ArrayDeque<>(graph.getHeads());
        List<Unit> unitInNextDepth = new ArrayList<>();
        while(!unitsInCurrentDepth.isEmpty()){
            Unit unit = unitsInCurrentDepth.removeFirst();
            visited.add(unit);
            if(unit instanceof Stmt){
                if(unit instanceof JIdentityStmt){
                    relevantStmts.add(unit);
                }else if(((Stmt) unit).containsInvokeExpr()){
                    relevantStmts.add(unit);
                }else if(unit instanceof JAssignStmt){
                    relevantStmts.add(unit);
                }
            }
            List<Unit> succs = graph.getSuccsOf(unit);
            for(Unit succ : succs){
                if(!visited.contains(succ)){
                    unitInNextDepth.add(succ);
                }
            }
            if(unitsInCurrentDepth.isEmpty() && !unitInNextDepth.contains(stmt)){
                unitsInCurrentDepth.addAll(unitInNextDepth);
                unitInNextDepth.clear();
            }
        }
        return relevantStmts.size();
    }
}
