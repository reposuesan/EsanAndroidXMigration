package pe.edu.esan.appostgrado.architecture.adapter

import android.util.Log
import com.google.gson.*
import pe.edu.esan.appostgrado.entidades.UserEsan
import pe.edu.esan.appostgrado.view.MenuPrincipalActivity
import java.lang.reflect.Type


class AbstractElementAdapter : JsonSerializer<UserEsan>, JsonDeserializer<UserEsan> {

    private val LOG = AbstractElementAdapter::class.simpleName

    override fun serialize(src: UserEsan, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val result = JsonObject()
        result.add("type", JsonPrimitive(src.javaClass.simpleName))
        result.add("properties", context.serialize(src, src.javaClass))

        return result
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): UserEsan {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type").asString
        /*Log.i(LOG, "The type for the deserialization is: $type")*/
        val element = jsonObject.get("properties")

        try {
            return context.deserialize(element, Class.forName("pe.edu.esan.appostgrado.entidades.$type"))
        } catch (cnfe: ClassNotFoundException) {
            throw JsonParseException("Unknown element type: $type", cnfe)
        }

    }
}