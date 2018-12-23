package de.r4md4c.gamedealz.regions

import de.r4md4c.gamedealz.home.HomeActivity

/**
 * A convience method to communicate between [HomeActivity] and [RegionSelectionDialogFragment]
 */
interface OnRegionChangeSubmitted {

    fun onRegionSubmitted()
}
