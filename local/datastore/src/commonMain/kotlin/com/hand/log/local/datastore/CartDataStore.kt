package com.hand.log.local.datastore

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import com.hand.log.local.datastore.Fruittie
import com.hand.log.local.datastore.di.json
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.use

@Serializable
data class Cart(
	val items: List<CartItem>,
)

@Serializable
data class CartItem(
    val id: Long,
    val count: Int,
)

internal object CartJsonSerializer : OkioSerializer<Cart> {
    override val defaultValue: Cart = Cart(emptyList())

    override suspend fun readFrom(source: BufferedSource): Cart = json.decodeFromString<Cart>(source.readUtf8())

    override suspend fun writeTo(
		t: Cart,
		sink: BufferedSink,
    ) {
        sink.use {
            it.writeUtf8(json.encodeToString(Cart.serializer(), t))
        }
    }
}

class CartDataStore(
    private val produceFilePath: () -> String,
) {
    private val db = DataStoreFactory.create(
        storage = OkioStorage<Cart>(
            fileSystem = FileSystem.SYSTEM,
            serializer = CartJsonSerializer,
            producePath = {
                produceFilePath().toPath()
            },
        ),
    )
    val cart: Flow<Cart>
        get() = db.data

    suspend fun add(fruittie: Fruittie) = update(fruittie, 1)

    suspend fun remove(fruittie: Fruittie) = update(fruittie, -1)

    suspend fun update(
		fruittie: Fruittie,
		diff: Int,
    ) {
        db.updateData { prevCart ->
            val newItems = mutableListOf<CartItem>()
            var found = false
            prevCart.items.forEach {
                if (it.id == fruittie.id) {
                    found = true
                    newItems.add(
                        it.copy(
                            count = it.count + diff,
                        ),
                    )
                } else {
                    newItems.add(it)
                }
            }
            if (!found) {
                newItems.add(
                    CartItem(id = fruittie.id, count = diff),
                )
            }
            newItems.removeAll {
                it.count <= 0
            }
            Cart(
                items = newItems,
            )
        }
    }
}
