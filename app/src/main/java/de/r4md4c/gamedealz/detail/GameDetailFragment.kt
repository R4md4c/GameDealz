package de.r4md4c.gamedealz.detail

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.common.base.fragment.BaseFragment
import de.r4md4c.gamedealz.common.deepllink.DeepLinks

class GameDetailFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_game_detail, container, false)


    companion object {
        @JvmStatic
        fun newInstance(title: String, plainId: String): Uri = DeepLinks.buildDetailUri(plainId, title)
    }
}