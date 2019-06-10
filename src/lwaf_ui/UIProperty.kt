import kotlin.reflect.KProperty

open class ReadOnlyUIProperty<out T> internal constructor(
        protected open val internalValue: T,
        val propertyName: String
) {
    open val value: T get() = internalValue
    open operator fun getValue(self: UINode, prop: KProperty<*>): T = internalValue

    override fun toString(): String
            = "property '$propertyName'"
}

class ComputedUIProperty<out N: UINode, out T> internal constructor(
        val owner: N,
        propertyName: String,
        private val computeValue: (N) -> T
): ReadOnlyUIProperty<T>(computeValue(owner), propertyName) {
    override val value: T get() = computeValue(owner)
    override operator fun getValue(self: UINode, prop: KProperty<*>): T = computeValue(owner)
}

class UIProperty<out N: UINode, T> internal constructor(
        private val owner: N,
        propertyName: String,
        override var internalValue: T
): ReadOnlyUIProperty<T>(internalValue, propertyName) {
    private val changeWatchers = mutableListOf<(N, T, T) -> Unit>()
    private val valuePredicates = mutableListOf<(N, T, T) -> Boolean>()

    operator fun setValue(self: UINode, prop: KProperty<*>, value: T) {
        val oldValue = internalValue
        if (valuePredicates.all { it(owner, oldValue, value) }) {
            internalValue = value
            changeWatchers.map { it(owner, oldValue, value) }
        }
    }

    fun onChange(callback: (N, T, T) -> Unit) { changeWatchers.add(callback) }
    fun attachValuePredicate(predicate: (N, T, T) -> Boolean) { valuePredicates.add(predicate) }
}

fun <T> readOnlyProperty(name: String, value: T): ReadOnlyUIProperty<T> = ReadOnlyUIProperty(value, name)
fun <N: UINode, T> computedProperty(name: String, self: N, compute: N.() -> T): ComputedUIProperty<N, T> = ComputedUIProperty(self, name, compute)
fun <N: UINode, T> property(name: String, self: N, value: T): UIProperty<N, T> = UIProperty(self, name, value)
