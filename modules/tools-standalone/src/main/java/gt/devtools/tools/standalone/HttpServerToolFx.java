package gt.devtools.tools.standalone;

import com.sun.net.httpserver.HttpServer;
import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.FxTextEditor;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import java.net.InetSocketAddress;

public final class HttpServerToolFx extends DeveloperToolFx {
    private final ValueProperty<Integer> port;
    private HttpServer server;
    private Label statusLabel;
    private FxTextEditor logEditor;
    private Button startStopBtn;

    private HttpServerToolFx(ToolConfiguration config) {
        super(config); this.port = registerConfig("httpPort", 8080);
    }

    @Override protected void buildUi(BorderPane panel) {
        panel.setPadding(new Insets(8));
        var bar = new HBox(8);
        bar.getChildren().add(new Label("Port:"));
        var spinner = new Spinner<Integer>(1024, 65535, port.get());
        spinner.getValueFactory().setValue(port.get());
        spinner.valueProperty().addListener((o, w, v) -> port.set(v));
        bar.getChildren().add(spinner);

        startStopBtn = new Button("Start");
        startStopBtn.setOnAction(e -> toggleServer());
        bar.getChildren().add(startStopBtn);

        statusLabel = new Label("Stopped");
        statusLabel.setFont(Font.font("Monospaced", 12));
        bar.getChildren().add(statusLabel);
        panel.setTop(bar);

        logEditor = new FxTextEditor(FxTextEditor.Mode.OUTPUT);
        panel.setCenter(logEditor);
    }

    private void toggleServer() {
        if (server != null) { stopServer(); } else { startServer(); }
    }

    private void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(port.get()), 0);
            server.createContext("/", ex -> {
                String resp = "Hello from Developer Tools HTTP Server on port " + port.get();
                ex.sendResponseHeaders(200, resp.length());
                var os = ex.getResponseBody(); os.write(resp.getBytes()); os.close();
            });
            server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(2));
            server.start();
            Platform.runLater(() -> { statusLabel.setText("Running on :" + port.get()); startStopBtn.setText("Stop"); });
            log("Server started on port " + port.get());
        } catch (Exception e) { log("Error: " + e.getMessage()); }
    }

    private void stopServer() {
        if (server != null) { server.stop(0); server = null; }
        Platform.runLater(() -> { statusLabel.setText("Stopped"); startStopBtn.setText("Start"); });
        log("Server stopped.");
    }

    private void log(String msg) { Platform.runLater(() -> logEditor.setText(logEditor.getText() + msg + "\n")); }

    @Override public void dispose() { super.dispose(); if (server != null) server.stop(0); }

    public static final class Factory implements ToolFxFactory<HttpServerToolFx> {
        public Factory() {} @Override public String getId() { return "http-server-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("http-server-fx", "HTTP Server (FX)", "HTTP Server").withGroupId("network"); }
        @Override public HttpServerToolFx create(ToolConfiguration c) { return new HttpServerToolFx(c); }
    }
}
