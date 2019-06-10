import lwaf_core.vec2
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

open class UINode

open class Button: UINode() {
    open var text by property("text", this, "")
    open val size by readOnlyProperty("size", vec2(3f))
    open val area by computedProperty("area", this) { size.x * size.y }
}

open class RoundedButton: Button() {
    override var text by property("text", this, "")
}

inline val <reified N : UINode> N.properties: List<ReadOnlyUIProperty<Any>> get()
        = this::class.memberProperties
            .asSequence()
            .map { property ->
                property.isAccessible = true
                @Suppress("UNCHECKED_CAST") // the following cast is required and safe
                (property as KProperty1<N, Any>).getDelegate(this)
            }
            .filter { it != null } .map { it!! }
            .filter { delegate ->
                delegate::class.isSubclassOf(ReadOnlyUIProperty::class)
            }
            .map { delegate ->
                @Suppress("UNCHECKED_CAST") // totally safe due to the filter above
                delegate as ReadOnlyUIProperty<Any>
            }
            .toList()

inline fun <reified N: UINode, reified T> N.onPropertyChanged(property: KProperty0<T>, noinline callback: N.(T, T) -> Unit) {
    property.isAccessible = true
    val delegate = property.getDelegate()

    if (delegate != null) {
        if (delegate::class.isSubclassOf(UIProperty::class)) {
            @Suppress("UNCHECKED_CAST") // safe unless the property was incorrectly initialised
            val self = (delegate as UIProperty<N, T>)
            self.onChange(callback)
            println(self)
        }
        else {
            throw Exception("'${property.name}' is not a UI property")
        }
    }
    else {
        throw Exception("No such property '${property.name}' of type ${T::class.simpleName} in type ${N::class.simpleName}")
    }
}
