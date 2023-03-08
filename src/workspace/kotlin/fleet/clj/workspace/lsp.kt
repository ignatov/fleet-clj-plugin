package fleet.clj.workspace

import com.jetbrains.rhizomedb.Entity
import com.jetbrains.rhizomedb.Entrypoint
import fleet.api.HostId
import fleet.api.ProjectRoot
import fleet.api.ProtocolPath
import fleet.api.workspace.document.MediaType
import fleet.clj.common.cljLspPathSettingKey
import fleet.common.ExecService
import fleet.common.fs.Pattern
import fleet.common.settings.querySettingKeyOnHost
import fleet.kernel.ChangeScope
import fleet.kernel.Kernel
import fleet.util.logging.KLoggers
import fleet.workspace.lsp.*

private val logger by lazy { KLoggers.logger(CljLspConfiguration::class) }

@Entrypoint
fun ChangeScope.init() {
    new(CljLspController::class) {
        restartNeeded = false
    }
}

interface CljLspController : LspControllerEntity {
    override val projectFilePatterns: List<Pattern> get() = listOf(Pattern("deps.edn")) // using the pattern Fleet will start clj-lsp
    override val label: String get() = "clj" // label to mark a corresponding LSP in UI, should be unique 
    override val lspConfiguration: LspConfiguration get() = CljLspConfiguration()
}

class CljLspConfiguration : LspConfiguration {
    override val identifier = "clj-language-server"
    override val mediaTypes = listOf( MediaType("text", "clj"), MediaType("text", "cljs")) // support file types

    override fun createLanguageClient(lifetime: Entity, kernel: Kernel, hostId: HostId): LspLanguageClient {
        return LspLanguageClient(lifetime, kernel, this, hostId)
    }

    override suspend fun findExecutable(
        exec: ExecService,
        projectRoots: List<ProjectRoot>,
        progressReporter: LspProgressReporter
    ): LspExecutable {
        val configuredPath = exec.querySettingKeyOnHost(cljLspPathSettingKey)
        val lspServerPath = if (configuredPath.isNotEmpty()) {
            logger.debug { "Using custom clj server path: $configuredPath" }
            configuredPath
        } else {
            null
        }

        if (lspServerPath == null) {
            error("Can't find clj lsp server, specify it with \"clj.lsp.path\" in ~/.fleet/settings.json")
        }

        return LspExecutable(ProtocolPath.of(lspServerPath), emptyList())
    }
}