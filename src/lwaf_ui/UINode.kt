import lwaf_core.minus
import lwaf_core.plus
import lwaf_core.vec2
import kotlin.properties.Delegates
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

open class UINode {
    /* hierarchical properties */
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

    /* layout properties */
    var computedSize = vec2(0f)
    var computedLocalPosition = vec2(0f)
    // set to true when children need repositioning
    var needsRepositioning: Boolean by Delegates.observable(true) { _, _, value ->
        if (value) parent?.needsRepositioning = true
    }
    var layout: Layout by Delegates.observable(FillLayout() as Layout) { _, old, new ->
        old.nodes.remove(this)
        new.nodes.add(this)
        needsRepositioning = true
    }
    var width: Float? by property("width", this, null)
    var height: Float? by property("height", this, null)
    var margin: Boundary by Delegates.observable(Boundary()) { _, o, n -> needsRepositioning = needsRepositioning || o != n }
    var padding: Boundary by Delegates.observable(Boundary()) { _, o, n -> needsRepositioning = needsRepositioning || o != n }

    init {
        this.onPropertyChanged(this::width) { _, _ -> this.parent?.needsRepositioning = true }
        this.onPropertyChanged(this::height) { _, _ -> this.parent?.needsRepositioning = true }
    }

    fun addChild(child: UINode) { child.parent = this }
    fun removeChild(child: UINode) { if (child.parent == this) child.parent = null }
    fun getChildren() = ArrayList(children)
    fun getAllChildren() = generateSequence(listOf(this)) { parents ->
        parents.flatMap { it.children } .let { if (it.isEmpty()) null else it }
    } .drop(1) .toList() .flatten()

    open fun draw(context: DrawContext2D, position: vec2, size: vec2) {
        children.map { child ->
            child.draw(
                    context,
                    position + child.computedLocalPosition + vec2(child.margin.left, child.margin.right),
                    child.computedSize - child.margin.totalSize()
            )
        }
    }
}

fun <N: UINode, T> N.onPropertyChanged(property: KProperty0<T>, callback: N.(T, T) -> Unit)
        = withProperty(property) { prop -> prop.onChange(callback) }

fun <N: UINode, T> N.onPropertyChangedTo(property: KProperty0<T>, callback: N.(T) -> Unit)
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
