package core.fx.manifestbased;

import core.fx.base.Feature;
import core.fx.base.ManifestFeatureExtractor;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class ManifestServiceCount implements ManifestFeatureExtractor<Long> {

    @Override
    public Feature<Long> extract(ProcessManifest target) {
        Long count = new Long(target.getServices().size());
        return new Feature<>(this.getClass().getSimpleName(), count);
    }

}