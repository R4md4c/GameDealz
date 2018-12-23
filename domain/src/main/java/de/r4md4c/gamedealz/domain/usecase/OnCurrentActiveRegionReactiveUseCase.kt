package de.r4md4c.gamedealz.domain.usecase

import de.r4md4c.gamedealz.domain.model.ActiveRegion
import kotlinx.coroutines.channels.ReceiveChannel

interface OnCurrentActiveRegionReactiveUseCase {

    /**
     * Listen for Active Region Channels.
     *
     * @return a Channel that emits each time an active region changes.
     */
    suspend fun activeRegionChange(): ReceiveChannel<ActiveRegion>
}

