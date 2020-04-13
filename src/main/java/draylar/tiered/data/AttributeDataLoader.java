package draylar.tiered.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import draylar.tiered.api.PotentialAttribute;
import draylar.tiered.gson.EntityAttributeModifierDeserializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class AttributeDataLoader extends JsonDataLoader {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(
                    EntityAttributeModifier.class,
                    new EntityAttributeModifierDeserializer()
            ).create();

    private static final String PARSING_ERROR_MESSAGE = "Parsing error loading recipe {}";
    private static final String LOADED_RECIPES_MESSAGE = "Loaded {} recipes";
    private static final Logger LOGGER = LogManager.getLogger();

    private Map<Identifier, PotentialAttribute> itemAttributes = ImmutableMap.of();

    public AttributeDataLoader() {
        super(GSON, "item_attributes");
    }

    @Override
    protected void apply(Map<Identifier, JsonObject> loader, ResourceManager manager, Profiler profiler) {
        Map<Identifier, PotentialAttribute> readItemAttributes = Maps.newHashMap();

        for (Map.Entry<Identifier, JsonObject> entry : loader.entrySet()) {
            Identifier identifier = entry.getKey();

            try {
                PotentialAttribute itemAttribute = GSON.fromJson(entry.getValue(), PotentialAttribute.class);
                readItemAttributes.put(new Identifier(itemAttribute.getID()), itemAttribute);
            } catch (IllegalArgumentException | JsonParseException exception) {
                LOGGER.error(PARSING_ERROR_MESSAGE, identifier, exception);
            }
        }

        itemAttributes = readItemAttributes;
        LOGGER.info(LOADED_RECIPES_MESSAGE, readItemAttributes.size());
    }

    /**
     * Returns a list of potential item attributes ({@link PotentialAttribute}) read from "data/modid/item_attributes".
     *
     * @return  list of potential read item attributes
     */
    public Map<Identifier, PotentialAttribute> getItemAttributes() {
        return itemAttributes;
    }
}