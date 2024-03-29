import buildSrc.BuildConfig

const val kotlinVersion = BuildConfig.KOTLIN_VERSION

object BuildPlugins {

    object Versions {
        const val buildToolsVersion = BuildConfig.AGP_VERSION
        const val navVersion = "2.7.4"
        const val detektVersion = "1.20.0"
    }

    const val kotlinAllOpenPlugin = "org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion"
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.buildToolsVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val navigationSafeArgsPlugin = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navVersion}"

    const val detektFormatting =
        "io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detektVersion}"
}

object AndroidSdk {
    const val min = 21
    const val compile = 34
    const val target = 33
}

object Libraries {
    internal object Versions {
        const val okhttp = "4.9.1"
        const val retrofit = "2.9.0"
        const val retrofitCoroutinesAdapter = "0.9.2"
        const val okio = "2.10.0"
        const val acra = "5.5.0"
        const val fragments = "1.6.2"
        const val constraintLayout = "2.1.0"
        const val nav_version = BuildPlugins.Versions.navVersion
        const val paging_version = "3.1.1"
        const val coroutines = "1.7.3"
        const val room = "2.6.0"
        const val timber = "4.7.1"
        const val lifecycle = "2.6.2"
        const val glide = "4.12.0"
        const val swiperefreshlayout = "1.0.0"
        const val appCompat = "1.3.1"
        const val recyclerView = "1.2.1"
        const val androidX = "1.12.0"
        const val material = "1.4.0"
        const val workmanager = "2.8.0"
        const val multidex = "2.0.1"
        const val annotations = "1.2.0"
        const val fastAdapter = "3.3.1"
        const val materialDrawer = "6.1.2"
        const val dagger = "2.48.1"
        const val assistedInject = "0.6.0"
        const val moshi = "1.15.0"
        const val appAuth = "0.8.1" // AndroidX support
        const val sqliteKtx = "2.1.0"
        const val store4 = "4.0.0-alpha03"
    }

    const val store4 = "com.dropbox.mobile.store:store4:${Versions.store4}"

    const val moshiCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"

    const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.room}"

    const val workManagerKtx = "androidx.work:work-runtime-ktx:${Versions.workmanager}"

    const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    const val assistedInjectAnnotations =
        "com.squareup.inject:assisted-inject-annotations-dagger2:${Versions.assistedInject}"
    const val assistedInjectCompiler =
        "com.squareup.inject:assisted-inject-processor-dagger2:${Versions.assistedInject}"

    const val jsoup = "org.jsoup:jsoup:1.14.1"

    const val retrofitCoroutinesAdapter =
        "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:${Versions.retrofitCoroutinesAdapter}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofitMoshiAdapter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"

    const val multidex = "androidx.multidex:multidex:${Versions.multidex}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel:${Versions.lifecycle}"
    const val lifecycleViewModelSavedState =
        "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.lifecycle}"
    const val lifecycleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    const val lifecycleLiveDataKtx =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    const val lifecycleJava8Common =
        "androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}"
    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    const val fragmentsKtx = "androidx.fragment:fragment-ktx:${Versions.fragments}"
    const val fragmentTesting = "androidx.fragment:fragment-testing:${Versions.fragments}"
    const val pagingCommonKtx = "androidx.paging:paging-common-ktx:${Versions.paging_version}"
    const val pagingRuntimeKtx = "androidx.paging:paging-runtime-ktx:${Versions.paging_version}"
    const val androidXAnnotations = "androidx.annotation:annotation:${Versions.annotations}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.androidX}"
    const val sqliteKtx = "androidx.sqlite:sqlite-ktx:${Versions.sqliteKtx}"

    // Logging solution For pure Java modules
    const val slf4j = "org.slf4j:slf4j-api:1.7.32"
    const val timberSlf4j = "com.arcao:slf4j-timber:3.1@aar"

    const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.nav_version}"
    const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.nav_version}"
    const val material = "com.google.android.material:material:${Versions.material}"

    const val swipeRefreshLayout =
        "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swiperefreshlayout}"
    const val stfalImageViewer = "com.github.stfalcon:stfalcon-imageviewer:1.0.10"

    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glideHttpIntegration = "com.github.bumptech.glide:okhttp3-integration:${Versions.glide}"
    const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    const val okHttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val okhttpHttpUrlConnection =
        "com.squareup.okhttp3:okhttp-urlconnection:${Versions.okhttp}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okio = "com.squareup.okio:okio:${Versions.okio}"

    const val acraCore = "ch.acra:acra-core:${Versions.acra}"

    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    const val kotlinCoroutines =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val kotlinFlowExtensions = "com.github.akarnokd:kotlin-flow-extensions:0.0.8"

    const val fastAdapter = "com.mikepenz:fastadapter-extensions:${Versions.fastAdapter}"
    const val materialDrawer = "com.mikepenz:materialdrawer:${Versions.materialDrawer}"
    const val stateMachine = "com.github.tinder:statemachine:0.2.0"
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
    const val appAuth = "com.github.openid:AppAuth-Android:${Versions.appAuth}"
}

object TestLibraries {
    private object Versions {
        const val robolectric = "4.11.1"
        const val assertj = "3.20.2"
        const val mockito_kotlin = "5.1.0"
        const val core_testing = "2.1.0"
        const val liveDataTesting = "1.2.0"
        const val junit4 = "4.13.2"
        const val androidXTest = "1.4.0"
        const val orchestratorVersion = "1.4.2"
        const val espresso = "3.4.0"
        const val uiAutomator = "2.2.0"
        const val mockito = "3.11.2"
        const val kakao = "2.4.0"
    }

    const val workManager = "androidx.work:work-testing:${Libraries.Versions.workmanager}"

    const val androidXTestExtJunit = "androidx.test.ext:junit:1.1.3"
    const val room = "androidx.room:room-testing:${Libraries.Versions.room}"
    const val mockitoKotlin =
        "org.mockito.kotlin:mockito-kotlin:${Versions.mockito_kotlin}"
    const val mockitoInline = "org.mockito:mockito-inline:${Versions.mockito}"
    const val mockitoCore = "org.mockito:mockito-core:${Versions.mockito}"
    const val assertjCore = "org.assertj:assertj-core:${Versions.assertj}"
    const val liveDataTesting = "com.jraska.livedata:testing:${Versions.liveDataTesting}"
    const val liveDataTestingKtx = "com.jraska.livedata:testing-ktx:${Versions.liveDataTesting}"
    const val coroutinesTest =
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Libraries.Versions.coroutines}"
    const val archComponentsCoreTesting = "androidx.arch.core:core-testing:${Versions.core_testing}"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
    const val junit = "junit:junit:${Versions.junit4}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    const val androidXTestCore = "androidx.test:core:${Versions.androidXTest}"
    const val androidXJunitExt = "androidx.test.ext:junit-ktx:1.1.5"

    const val kakao = "com.agoda.kakao:kakao:${Versions.kakao}"
    const val navigationTest =
        "androidx.navigation:navigation-testing:${BuildPlugins.Versions.navVersion}"
    const val mockitoAndroid = "org.mockito:mockito-android:${Versions.mockito}"
    const val dexMaker = "com.linkedin.dexmaker:dexmaker-mockito-inline:2.28.0"
    const val testRunner = "androidx.test:runner:${Versions.androidXTest}"
    const val testRules = "androidx.test:rules:${Versions.androidXTest}"
    const val testOrchestrator = "androidx.test:orchestrator:${Versions.orchestratorVersion}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val espressoContrib = "androidx.test.espresso:espresso-contrib:${Versions.espresso}"
    const val uiAutomator = "androidx.test.uiautomator:uiautomator:${Versions.uiAutomator}"
    const val disableAnimationsRule = "com.bartoszlipinski:disable-animations-rule:2.0.0"
}
