package core.fx.variablebased;

import core.fx.base.Feature;

import core.fx.base.MethodVarFEU;
import core.fx.base.VariableFEU;
import soot.*;
import java.util.*;

public class TypeHierarchySize implements VariableFEU<Integer> {
    /**
     * Size of the type hierarchy of the given value's type.
     */
    @Override
    public Feature<Integer> extract(Value value) {
        FastHierarchy hierarchy = Scene.v().getOrMakeFastHierarchy();
        SootClass clazz = Scene.v().getSootClass(value.getType().toString());
        SootClass obj = Scene.v().getSootClass("java.lang.Object");
        Deque<SootClass> queue = new ArrayDeque<>();
        queue.add(clazz);
        Set<SootClass> superRelatedClasses = new HashSet<>();
        superRelatedClasses.add(obj);
        //get all super classed and interfaces
        while (!queue.isEmpty()){
            SootClass sc = queue.removeFirst();
            if(superRelatedClasses.contains(sc)){
                continue;
            }
            superRelatedClasses.add(sc);
            queue.addAll(sc.getInterfaces());
            SootClass superClass = sc.getSuperclass();
            if(superClass!=null){
                queue.add(superClass);
            }
        }
        //get all sub classed and interfaces
        queue.add(clazz);
        Set<SootClass> subRelatedClasses = new HashSet<>();
        while (!queue.isEmpty()){
            SootClass sc = queue.removeFirst();
            if(subRelatedClasses.contains(sc)){
                continue;
            }
            subRelatedClasses.add(sc);
            queue.addAll(hierarchy.getAllSubinterfaces(sc));
            queue.addAll(hierarchy.getAllImplementersOfInterface(sc));
            queue.addAll(hierarchy.getSubclassesOf(sc));
        }

        Set<SootClass> relatedClasses = new HashSet<>(superRelatedClasses);
        relatedClasses.addAll(subRelatedClasses);
        return new Feature<>(this.getClass().getSimpleName(), relatedClasses.size());
    }
}
