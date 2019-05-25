package lwaf_core

private val cache: MutableMap<String, Resource> = HashMap()
private val onCompleteCallbacks: MutableList<Pair<Resource, (Resource) -> Unit>> = ArrayList()

interface Resource {
    fun getResourceID(): String
    fun free()
}

interface GLResource {
    fun destroy()
}

class ResourceWrapper<T>(val value: T, private val resID: String): Resource {
    override fun getResourceID(): String = resID
    override fun free() {}
}

fun <T: Resource> loadResourceAsync(identifier: String, loader: (String) -> T, onComplete: (T) -> Unit) {
    Thread {
        val resource = loadResource(identifier, loader)
        // shh
        @Suppress("UNCHECKED_CAST")
        onCompleteCallbacks.add(Pair(resource, { res -> onComplete(res as T) }))
    } .start()
}

fun <T: Resource> loadResourceAsync(identifier: String, loader: () -> T, onComplete: (T) -> Unit) {
    loadResourceAsync(identifier, { _ -> loader() }, onComplete)
}

fun <T: Resource> loadResource(identifier: String, loader: (String) -> T): T {
    if (!cache.containsKey(identifier)) {
        Logging.log("resource.load") { "Loading resource '$identifier'" }
        cache[identifier] = loader(identifier)
    }

    assert(identifier == cache[identifier]!!.getResourceID())

    // yeah this is dodgy but eh
    @Suppress("UNCHECKED_CAST")
    return cache[identifier]!! as T
}

fun <T: Resource> loadResource(identifier: String, loader: () -> T): T {
    return loadResource(identifier) { _ -> loader() }
}

fun finaliseQueuedResources() {
    for ((data, callback) in onCompleteCallbacks) callback(data)
    onCompleteCallbacks.clear()
}

fun freeResource(identifier: String) {
    val cached = cache[identifier]

    if (cached != null) {
        Logging.log("resource.free") { "Freeing resource '$identifier'" }
        cache.remove(identifier)
        cached.free()
    }
}

fun freeResources() {
    cache.keys.map { it } .map { freeResource(it) }
}
