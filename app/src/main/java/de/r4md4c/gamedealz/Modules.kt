package de.r4md4c.gamedealz

import de.r4md4c.gamedealz.home.HomeViewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val MAIN = module {

    viewModel {
        HomeViewModel(get())
    }

}