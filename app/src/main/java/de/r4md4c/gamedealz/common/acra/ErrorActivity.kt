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

package de.r4md4c.gamedealz.common.acra

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.r4md4c.gamedealz.BuildConfig
import de.r4md4c.gamedealz.R
import de.r4md4c.gamedealz.databinding.ActivityErrorBinding
import kotlinx.parcelize.Parcelize
import org.acra.ReportField
import org.acra.data.CrashReportData
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class ErrorInfo(
    val stacktrace: String,
    val deviceModel: String,
    val appVersion: String,
    val appVersionCode: String,
    val androidVersion: String,
    val packageName: String
) : Parcelable

class ErrorActivity : AppCompatActivity() {

    private val errorInfo by lazy { intent.getParcelableExtra<ErrorInfo>(EXTRA_ERROR_INFO) }

    private val currentTimestamp by lazy {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        df.timeZone = TimeZone.getTimeZone("GMT")
        df.format(Date())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setTitle(R.string.title_submit_error_report)

        binding.reportButton.setOnClickListener {
            reportBugReport(binding)
        }
        binding.errorMessageView.text = formString()
    }

    private fun reportBugReport(binding: ActivityErrorBinding) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$ERROR_EMAIL_ADDRESS")
            putExtra(Intent.EXTRA_SUBJECT, ERROR_EMAIL_SUBJECT)
            putExtra(
                Intent.EXTRA_TEXT,
                errorInfo.toJsonObject(binding).toString(INDENT_SPACES_NUMBER)
            )
        }
        kotlin.runCatching {
            startActivity(intent)
        }.onFailure { throwable ->
            Toast.makeText(this, throwable.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun formString(): String =
        with(errorInfo) {
            StringBuilder()
                .append("Phone Model: $deviceModel\n")
                .append("Android Version: $androidVersion\n")
                .append("App Version Name: $appVersion\n")
                .append("App Version Code: $appVersionCode\n")
                .append("Current Timestamp: $currentTimestamp\n")
                .append("Package Name: $packageName\n")
                .append('\n')
                .append("Stacktrace: =====================================\n")
                .append(stacktrace)
                .append("============================================")
                .append('\n')
                .toString()
        }

    private fun ErrorInfo.toJsonObject(binding: ActivityErrorBinding): JSONObject =
        JSONObject().apply {
            put("phone_model", deviceModel)
            put("android_version", androidVersion)
            put("app_version_name", appVersion)
            put("app_version_code", appVersionCode)
            put("current_timestamp", currentTimestamp)
            put("package_name", packageName)
            Locale.getDefault()?.let { put("locale", it) }
            put("comment", binding.yourCommentEditText.text.toString())
            put("stacktrace", stacktrace)
        }

    companion object {
        fun toIntent(context: Context, errorCrashReportData: CrashReportData): Intent =
            Intent(context, ErrorActivity::class.java).apply {

                with(errorCrashReportData) {
                    val info = ErrorInfo(
                        getString(ReportField.STACK_TRACE),
                        getString(ReportField.PHONE_MODEL),
                        getString(ReportField.APP_VERSION_NAME),
                        getString(ReportField.APP_VERSION_CODE),
                        getString(ReportField.ANDROID_VERSION),
                        getString(ReportField.PACKAGE_NAME)
                    )

                    putExtra(EXTRA_ERROR_INFO, info)
                }
            }

        const val ERROR_EMAIL_ADDRESS = "gamedealz@protonmail.com"
        const val ERROR_EMAIL_SUBJECT = "Exception in GameDealz " + BuildConfig.VERSION_NAME

        private const val INDENT_SPACES_NUMBER = 3
        private const val EXTRA_ERROR_INFO = "error_info"
    }
}
