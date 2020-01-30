/*
 * This file is part of GameDealz.
 *
 * GameDealz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * GameDealz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GameDealz.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * A plugin that applies common configuration to all module projects.
 */
class GameDealzPlugin : Plugin<Project> {

    companion object {
        private const val PLUGIN_KOTLIN = "kotlin"
        private const val PLUGIN_ANDROID_KOTLIN = "kotlin-android"
        private const val PLUGIN_KOTLIN_KAPT = "kotlin-kapt"
    }

    override fun apply(project: Project) {
        project.subprojects {
            applyKotlinCompilerOptions()
            afterEvaluate {
                afterEvaluateProject(this)
            }
        }
    }

    private fun afterEvaluateProject(project: Project) = with(project) {
        if (hasKotlinPlugin()) {
            if (!pluginManager.hasPlugin(PLUGIN_KOTLIN_KAPT)) {
                pluginManager.apply(PLUGIN_KOTLIN_KAPT)
            }
            this.dependencies.add("implementation", Libraries.dagger)
            this.dependencies.add("kapt", Libraries.daggerCompiler)
            this.dependencies.add("compileOnly", Libraries.assistedInjectAnnotations)
            this.dependencies.add("kapt", Libraries.assistedInjectCompiler)

            if (pluginManager.hasPlugin(PLUGIN_ANDROID_KOTLIN)) {
                this.dependencies.add("kaptAndroidTest", Libraries.assistedInjectCompiler)
                this.dependencies.add("kaptAndroidTest", Libraries.daggerCompiler)
            }
        }
    }

    private fun Project.applyKotlinCompilerOptions() {
        tasks.withType(KotlinCompile::class.java).all {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs = listOf(
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xuse-experimental=kotlinx.coroutines.FlowPreview"
            )
        }
    }

    private fun Project.hasKotlinPlugin() =
        pluginManager.hasPlugin(PLUGIN_KOTLIN) || pluginManager.hasPlugin(PLUGIN_ANDROID_KOTLIN)
}
