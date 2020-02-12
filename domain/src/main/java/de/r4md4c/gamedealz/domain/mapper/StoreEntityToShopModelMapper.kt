package de.r4md4c.gamedealz.domain.mapper

import dagger.Reusable
import de.r4md4c.gamedealz.data.entity.Store
import de.r4md4c.gamedealz.domain.model.ShopModel
import javax.inject.Inject

@Reusable
internal class StoreEntityToShopModelMapper @Inject constructor() : Mapper<Store, ShopModel> {
    override fun map(input: Store): ShopModel = ShopModel(input.id, input.name, input.color)
}
