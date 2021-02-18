package blue.starry.saya.common

import com.google.common.base.CaseFormat
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun Document.getFirstElementByTagName(tagName: String): Element? {
    return documentElement.getFirstElementByTagName(tagName)
}

fun Element.getFirstElementByTagName(tagName: String): Element? {
    return getElementsByTagName(tagName).asSequence().firstOrNull() as? Element
}

fun <T> NodeList.map(block: (Element) -> T): List<T> = asSequence().map { block(it as Element) }.toList()

fun NodeList.asSequence(): Sequence<Node> = sequence {
    repeat(length) {
        yield(item(it))
    }
}

interface XmlModel {
    val xml: Element
}

@Suppress("UNCHECKED_CAST")
inline fun <T> XmlModel.delegate(tagName: String? = null, crossinline block: (Element) -> T): ReadOnlyProperty<XmlModel, T> {
    return object : ReadOnlyProperty<XmlModel, T> {
        private var value: T? = null
        private val initialized = AtomicBoolean()

        override fun getValue(thisRef: XmlModel, property: KProperty<*>): T {
            if (!initialized.get()) {
                val key = tagName ?: CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, property.name)
                value = xml.getFirstElementByTagName(key)?.let(block)
            }

            return value as T
        }
    }
}

fun XmlModel.string(tagName: String? = null) = delegate(tagName) { it.textContent!! }
fun XmlModel.stringOrNull(tagName: String? = null) = delegate(tagName) { it.textContent ?: null }

fun XmlModel.int(tagName: String? = null) = delegate(tagName) { it.textContent.toInt() }
fun XmlModel.intOrNull(tagName: String? = null) = delegate(tagName) { it.textContent?.toIntOrNull() }
