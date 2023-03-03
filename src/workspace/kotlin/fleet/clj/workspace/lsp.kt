package fleet.clj.workspace

import com.jetbrains.rhizomedb.Entity
import com.jetbrains.rhizomedb.Entrypoint
import fleet.api.*
import fleet.api.workspace.document.MediaType
import fleet.clj.common.cljLspPathSettingKey
import fleet.common.ExecService
import fleet.common.fs.ConfiguratorId
import fleet.common.fs.Pattern
import fleet.common.fs.SharedProjectRootEntity
import fleet.common.fs.SharedWorkspaceRootEntity
import fleet.common.settings.querySettingKeyOnHost
import fleet.kernel.ChangeScope
import fleet.kernel.Kernel
import fleet.util.logging.KLoggers
import fleet.workspace.lsp.*
import fleet.workspace.project.BackendControllerEntity
import fleet.workspace.project.ProjectConfiguratorEntity
import org.eclipse.lsp4j.ClientInfo
import org.eclipse.lsp4j.ConfigurationItem
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.WorkspaceFolder

private val logger by lazy { KLoggers.logger(CljLspConfiguration::class) }

class CljLspConfiguration : LspConfiguration {
    override val identifier = "clj-language-server"
    override val mediaTypes = listOf(MediaType("text", "clj"), MediaType("text", "cljs"))

    override fun createLanguageClient(lifetime: Entity, kernel: Kernel, hostId: HostId): LspLanguageClient {
        return LspLanguageClient(lifetime, kernel, this, hostId)
    }

    override fun createInitializeParams(roots: List<FileAddress>): InitializeParams {
        val initializeParams = InitializeParams()
        initializeParams.capabilities = LspClientCapabilitiesFactory().createClientCapabilities()
        initializeParams.clientInfo = ClientInfo("Fleet")
        initializeParams.rootPath = roots.firstOrNull()?.path?.toString()
        initializeParams.rootUri = roots.firstOrNull()?.path?.uri()
        initializeParams.workspaceFolders = roots.map { fileAddress ->
            WorkspaceFolder(fileAddress.uri(), fileAddress.path.components.last())
        }
        return initializeParams
    }

    override fun workspaceConfigurationFor(configurationItem: ConfigurationItem): Any? = null

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

private val CONFIGURATOR_ID = ConfiguratorId("CLJ")
private const val LABEL = "clj"

interface CljProjectConfigurator : ProjectConfiguratorEntity {
    override val projectFilePatterns: List<Pattern>
        get() = listOf(Pattern("*.clj"), Pattern("*.edn"))

    override suspend fun discoverProjectDirectories(
        matches: Set<FileAddress>,
        root: SharedWorkspaceRootEntity
    ): List<FileAddress> {
        return matches.mapNotNull { it.parent() }.toSet().rootDirectories()
    }

    override val label: String get() = LABEL
    override val id: ConfiguratorId get() = CONFIGURATOR_ID
    override suspend fun serveProjectRoot(projectRootEntity: SharedProjectRootEntity) {}
}

interface CljAnalyzerController : BackendControllerEntity {
    override val projectRootTagLabel: String get() = LABEL
    override suspend fun setUpBackend(hostsToServe: Set<HostId>) {
        updateLspHosts(hostsToServe, LABEL, CljLspConfiguration())
    }
}

@Entrypoint
fun ChangeScope.init() {
    new(CljProjectConfigurator::class)
    new(CljAnalyzerController::class) {
        restartNeeded = false
    }
}
