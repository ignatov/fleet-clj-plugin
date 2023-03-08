package fleet.clj.common

import com.jetbrains.rhizomedb.Entrypoint
import fleet.common.settings.SettingsEP
import fleet.common.settings.SettingsKey
import fleet.common.settings.SettingsLocation
import fleet.kernel.ChangeScope
import fleet.kernel.register

val cljLspPathSettingKey = SettingsKey(
    key = "clj.lsp.path",
    defaultValue = "",
    supportContexts = false,
    presentableName = "Path of the Clojure language-server executable",
    locations = setOf(SettingsLocation.HOST)
)

@Entrypoint
fun ChangeScope.settings() {
    register {
        SettingsEP.register(cljLspPathSettingKey.key) { cljLspPathSettingKey }
    }
}