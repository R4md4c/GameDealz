package de.r4md4c.gamedealz.common.base.fragment

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import de.r4md4c.gamedealz.SCOPE_FRAGMENT
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getOrCreateScope

abstract class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindScope(getOrCreateScope(SCOPE_FRAGMENT))
    }

    fun setTitle(title: String?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = title
    }

    fun setTitle(@StringRes titleResId: Int) {
        (activity as? AppCompatActivity)?.supportActionBar?.setTitle(titleResId)
    }

}