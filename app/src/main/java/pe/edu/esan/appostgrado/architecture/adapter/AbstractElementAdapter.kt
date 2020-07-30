package pe.edu.esan.appostgrado.architecture.adapter

import com.google.gson.*
import pe.edu.esan.appostgrado.entidades.UserEsan
import java.lang.reflect.Type


class AbstractElementAdapter : JsonSerializer<UserEsan>, JsonDeserializer<UserEsan> {


    override fun serialize(src: UserEsan, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val result = JsonObject()
        result.add("type", JsonPrimitive(src.javaClass.getSimpleName()))
        result.add("properties", context.serialize(src, src.javaClass))

        return result
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): UserEsan {
        val jsonObject = json.getAsJsonObject()
        val type = jsonObject.get("type").getAsString()
        val element = jsonObject.get("properties")

        try {
            return context.deserialize(element, Class.forName("pe.edu.esan.appostgrado.entidades.$type"))
        } catch (cnfe: ClassNotFoundException) {
            throw JsonParseException("Unknown element type: $type", cnfe)
        }

    }
}