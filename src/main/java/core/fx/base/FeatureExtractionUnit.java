package core.fx.base;


public interface FeatureExtractionUnit<V,T> {

    default String getName(){
        return this.getClass().getSimpleName();
    }

    default String getName(String value){
        return String.format("%s(\"%s\")", getName(), value);
    }
}
