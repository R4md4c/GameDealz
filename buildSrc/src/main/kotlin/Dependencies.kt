const val kotlinVersion = "1.3.50"


object BuildPlugins {

    object Versions {
        const val buildToolsVersion = "3.5.2"
        const val navVersion = "2.1.0"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.buildToolsVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val navigationSafeArgsPlugin = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navVersion}"
}

object AndroidSdk {
    const val min = 19
    const val compile = 28
    const val target = compile
}

object Libraries {
    internal object Versions {
        const val okhttp = "3.12.1"
        const val retrofit = "2.5.0"
        const val retrofitCoroutinesAdapter = "0.9.2"
        const val okio = "2.2.2"
        const val acra = "5.2.1"
        const val constraintLayout = "2.0.0-beta2"
        const val nav_version = BuildPlugins.Versions.navVersion
        const val paging_version = "2.1.0"
        const val koin = "1.0.2"
        const val coroutines = "1.3.2"
        const val room = "2.2.0"
        const val timber = "4.7.1"
        const val lifecycle = "2.1.0"
        const val glide = "4.10.0"
        const val swiperefreshlayout = "1.0.0"
        const val appCompat = "1.1.0"
        const val recyclerView = "1.1.0-beta05"
        const val androidX = "1.2.0-beta01"
        const val material = "1.1.0-beta01"
        const val workmanager = "2.2.0"
        const val multidex = "2.0.1"
        const val annotations = "1.1.0"
        const val fastAdapter = "3.3.1"
        const val materialDrawer = "6.1.2"
        const val rx2 = "2.2.13"
        const val moshi = "1.8.0"
        const val kotshi = "2.0.1"
    }

    const val kotshiApi = "se.ansman.kotshi:api:${Versions.kotshi}"
    const val kotshiCompiler = "se.ansman.kotshi:compiler:${Versions.kotshi}"
    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"

    const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    const val roomRxJava2 = "androidx.room:room-rxjava2:${Versions.room}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.room}"

    const val workManagerKtx = "androidx.work:work-runtime-ktx:${Versions.workmanager}"

    const val jsoup = "org.jsoup:jsoup:1.12.1"

    const val retrofitCoroutinesAdapter =
        "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:${Versions.retrofitCoroutinesAdapter}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofitMoshiAdapter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"

    const val rxJava2 = "io.reactivex.rxjava2:rxjava:${Versions.rx2}"
    const val multidex = "androidx.multidex:multidex:${Versions.multidex}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel:${Versions.lifecycle}"
    const val lifecycleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val lifecycleLiveData = "androidx.lifecycle:lifecycle-livedata:${Versions.lifecycle}"
    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"

    const val pagingCommonKtx = "androidx.paging:paging-common-ktx:${Versions.paging_version}"
    const val pagingRuntimeKtx = "androidx.paging:paging-runtime-ktx:${Versions.paging_version}"
    const val androidXAnnotations = "androidx.annotation:annotation:${Versions.annotations}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.androidX}"

    const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.nav_version}"
    const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.nav_version}"
    const val material = "com.google.android.material:material:${Versions.material}"

    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swiperefreshlayout}"
    const val stfalImageViewer = "com.github.stfalcon:stfalcon-imageviewer:0.1.0"

    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glideHttpIntegration = "com.github.bumptech.glide:okhttp3-integration:${Versions.glide}"
    const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    const val okHttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okio = "com.squareup.okio:okio:${Versions.okio}"

    const val acraCore = "ch.acra:acra-core:${Versions.acra}"

    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    const val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val kotlinCoroutinesRx2 = "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:${Versions.coroutines}"

    const val fastAdapter = "com.mikepenz:fastadapter-extensions:${Versions.fastAdapter}"
    const val materialDrawer = "com.mikepenz:materialdrawer:${Versions.materialDrawer}"
    const val stateMachine = "com.github.tinder:statemachine:0.1.2"
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    const val koinCore = "org.koin:koin-core:${Versions.koin}"
    const val koinCoreExt = "org.koin:koin-core-ext:${Versions.koin}"
    const val koinAndroid = "org.koin:koin-android:${Versions.koin}"
    const val koinAndroidXScope = "org.koin:koin-androidx-scope:${Versions.koin}"
    const val koinAndroidXViewModel = "org.koin:koin-androidx-viewmodel:${Versions.koin}"

}

object TestLibraries {
    private object Versions {
        const val mockk = "1.9.3"
        const val robolectric = "4.2.1"
        const val assertj = "2.9.1"
        const val mockito_kotlin = "2.1.0"
        const val core_testing = "2.1.0"
        const val liveDataTesting = "1.1.0"
        const val junit4 = "4.12"
        const val androidXTest = "1.2.0"
        const val espresso = "3.2.0"
        const val uiAutomator = "2.2.0"
        const val mockitoAndroid = "2.25.0"
    }

    const val workManager = "androidx.work:work-testing:${Libraries.Versions.workmanager}"

    const val androidXTestExtJunit = "androidx.test.ext:junit:1.1.1"
    const val room = "androidx.room:room-testing:${Libraries.Versions.room}"
    const val mockK = "io.mockk:mockk:${Versions.mockk}"
    const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockito_kotlin}"
    const val assertjCore = "org.assertj:assertj-core:${Versions.assertj}"
    const val liveDataTesting = "com.jraska.livedata:testing:${Versions.liveDataTesting}"
    const val liveDataTestingKtx = "com.jraska.livedata:testing-ktx:${Versions.liveDataTesting}"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Libraries.Versions.coroutines}"
    const val archComponentsCoreTesting = "androidx.arch.core:core-testing:${Versions.core_testing}"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
    const val junit = "junit:junit:${Versions.junit4}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    const val koinTest = "org.koin:koin-test:${Libraries.Versions.koin}"
    const val androidXTestCore = "androidx.test:core:${Versions.androidXTest}"

    const val mockitoAndroid = "org.mockito:mockito-android:${Versions.mockitoAndroid}"
    const val testRunner = "androidx.test:runner:${Versions.androidXTest}"
    const val testRules = "androidx.test:rules:${Versions.androidXTest}"
    const val testOrchestrator = "androidx.test:orchestrator:${Versions.androidXTest}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val uiAutomator = "androidx.test.uiautomator:uiautomator:${Versions.uiAutomator}"
}
