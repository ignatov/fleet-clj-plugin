module fleet.clj.workspace {
    requires com.google.gson;
    requires fleet.clj.common;
    requires fleet.code.protocol;
    requires fleet.common;
    requires fleet.kernel;
    requires fleet.lsp;
    requires fleet.run.common;
    requires fleet.workspace;
    requires org.eclipse.lsp4j.jsonrpc;
    requires org.eclipse.lsp4j;

    exports fleet.clj.workspace;
}