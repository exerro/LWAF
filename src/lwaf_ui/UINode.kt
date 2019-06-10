import kotlin.properties.Delegates
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

open class UINode {
    private val children: MutableList<UINode> = mutableListOf()
    internal var root: UIRoot? by Delegates.observable<UIRoot?>(null) { _, _, new ->
        children.map { it.root = new }
    }
    var parent: UINode? by Delegates.observable<UINode?>(null) { _, old, new ->
        old?.children?.remove(this)
        new?.children?.add(this)

        if (new != null) {
            root = new.root
        }
    }

    fun addChild(child: UINode) { child.parent = this }
    fun removeChild(child: UINode) { if (child.parent == this) child.parent = null }
    fun getChildren() = ArrayList(children)
    fun getAllChildren() = generateSequence(listOf(this)) { parents ->
        parents.flatMap { it.children } .let { if (it.isEmpty()) null else it }
    } .drop(1) .toList() .flatten()
}

fun <N: UINode, T> N.onPropertyChanged(property: KProperty0<T>, callback: N.(T, T) -> Unit)
        = withProperty(property) { prop -> prop.onChange(callback) }

fun <N: UINode, T> N.onPropertyChanged(property: KProperty0<T>, callback: N.(T) -> Unit)
        = withProperty(property) { prop -> prop.onChange { self, _, new -> callback(self, new) } }

fun <N: UINode, T> N.assertPropertyMatches(property: KProperty0<T>, predicate: N.(T, T) -> Boolean)
        = withProperty(property) { prop -> prop.attachValuePredicate(predicate) }

fun <N: UINode, T> N.assertPropertyMatches(property: KProperty0<T>, predicate: N.(T) -> Boolean)
        = withProperty(property) { prop -> prop.attachValuePredicate { self, _, new -> predicate(self, new) } }

val <N : UINode> N.properties: List<ReadOnlyUIProperty<Any>> get()
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

private inline fun <N: UINode, T> N.withProperty(property: KProperty0<T>, func: N.(UIProperty<N, T>) -> Unit) {
    property.isAccessible = true
    val delegate = property.getDelegate()

    if (delegate != null && delegate::class.isSubclassOf(UIProperty::class)) {
        @Suppress("UNCHECKED_CAST") // safe unless the property was incorrectly initialised
        func(this, (delegate as UIProperty<N, T>))
    }
    else {
        throw Exception("No such UI property '${property.name}' in type ${this::class.simpleName}")
    }
}
