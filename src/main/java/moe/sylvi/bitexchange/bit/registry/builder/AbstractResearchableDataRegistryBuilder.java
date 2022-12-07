package moe.sylvi.bitexchange.bit.registry.builder;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.research.AbstractCombinedResearchRequirement;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.util.JsonHelper;

import java.util.List;

public abstract class AbstractResearchableDataRegistryBuilder<R, I extends BitInfoResearchable<R>> extends AbstractDataRegistryBuilder<R, I> {
    public AbstractResearchableDataRegistryBuilder(BitRegistry<R, I> registry, int priority) {
        super(registry, priority);
    }
    public AbstractResearchableDataRegistryBuilder(BitRegistry<R, I> registry) {
        super(registry);
    }

    protected long parseResearch(JsonObject json, long defaultResearch) {
        return JsonHelper.getLong(json, "research", defaultResearch);
    }

    protected boolean parseResearchable(JsonObject json, boolean defaultResearchable) {
        return JsonHelper.getBoolean(json, "researchable", defaultResearchable);
    }

    protected List<ResearchRequirement> parseResearchRequirements(JsonObject json) throws Throwable {
        List<ResearchRequirement> result = Lists.newArrayList();
        String field = json.has("required_research") ? "required_research" : (json.has("value_ref") ? "value_ref" : null);
        if (field != null) {
            String[] ids = JsonHelper.getString(json, field).split(",");
            for (String id : ids) {
                if (id.isEmpty()) {
                    continue;
                }
                List<BitResource> parsed = BitHelper.parseMultiResourceId(id, registry);

                List<ResearchRequirement> requirements = Lists.newArrayList();
                for (BitResource resource : parsed) {
                    if (resource.getRegistry().get(resource.getResource()) instanceof BitInfoResearchable researchable) {
                        ResearchRequirement requirement = researchable.createResearchRequirement();
                        if (!requirements.contains(requirement)) {
                            requirements.add(requirement);
                        }
                    }
                }

                if (!requirements.isEmpty()) {
                    result.add(AbstractCombinedResearchRequirement.of(requirements));
                }
            }
        }
        return result;
    }

    protected List<ResearchRequirement> modifyResearchRequirements(I info, JsonObject json) throws Throwable {
        if (json.has("required_research")) {
            return parseResearchRequirements(json);
        } else {
            return info.getResearchRequirements();
        }
    }
}
