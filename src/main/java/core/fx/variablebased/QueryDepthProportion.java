package core.fx.variablebased;

import core.fx.base.Feature;
import core.fx.base.VariableFEU;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import java.util.*;

//AAS-Feature, express the position of queryStmt in whole method
//In AAS-Feature, all stmts after queryDepth will be ignored.
public class QueryDepthProportion implements VariableFEU<Double> {
    @Override
    public Feature<Double> extract(SootMethod target, Stmt tail, Value variable) {
        Set<Unit> visited = new HashSet<>();
        int depth = 1;
        int tailDepth = 0;
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        Deque<Unit> unitsInCurrentDepth = new ArrayDeque<>(graph.getHeads());
        List<Unit> unitInNextDepth = new ArrayList<>();
        while(!unitsInCurrentDepth.isEmpty()){
            Unit unit = unitsInCurrentDepth.removeFirst();
            visited.add(unit);
            if(unit instanceof Stmt){
                Stmt stmt = (Stmt) unit;
                if(stmt.equals(tail)){
                    tailDepth = depth;
                }
            }
            List<Unit> succs = graph.getSuccsOf(unit);
            for(Unit succ : succs){
                if(!visited.contains(succ)){
                    unitInNextDepth.add(succ);
                }
            }
            if(unitsInCurrentDepth.isEmpty()){
                unitsInCurrentDepth.addAll(unitInNextDepth);
                unitInNextDepth.clear();
                depth++;
            }
        }
        Double proportion = (double) tailDepth / (double) depth ;
        return new Feature<>(this.getClass().getSimpleName(), proportion);
    }
}
