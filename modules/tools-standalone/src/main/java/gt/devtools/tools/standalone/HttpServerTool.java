package gt.devtools.tools.standalone;

import com.sun.net.httpserver.HttpServer;
import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.TextEditor;

import javax.swing.*;
import java.awt.*;
import java.net.InetSocketAddress;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Runs a simple local HTTP server for testing webhooks, APIs, and static content. */
public final class HttpServerTool extends DeveloperTool {

    private final ValueProperty<Integer> port;
    private HttpServer server;
    private JButton startStopBtn;
    private JLabel statusLabel;
    private TextEditor logEditor;
    private boolean running;

    private HttpServerTool(ToolConfiguration config) {
        super(config);
        this.port = registerConfig("httpPort", 8080);
    }

    @Override
    protected void buildUi(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setLayout(new BorderLayout(0, 8));

        var configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configPanel.add(new JLabel("Port:"));
        var portSpinner = new JSpinner(new javax.swing.SpinnerNumberModel(
                port.get().intValue(), 1024, 65535, 1));
        portSpinner.addChangeListener(e -> port.set((Integer) portSpinner.getValue()));
        configPanel.add(portSpinner);

        startStopBtn = new JButton("Start");
        startStopBtn.addActionListener(e -> toggleServer());
        configPanel.add(startStopBtn);

        statusLabel = new JLabel("  Stopped");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        configPanel.add(statusLabel);
        panel.add(configPanel, BorderLayout.NORTH);

        logEditor = new TextEditor(TextEditor.Mode.OUTPUT);
        logEditor.setSyntaxStyle("text/plain");
        panel.add(logEditor, BorderLayout.CENTER);
    }

    private void toggleServer() {
        if (running) stopServer(); else startServer();
    }

    private void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(port.get()), 0);
            server.createContext("/", exchange -> {
                String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String log = String.format("[%s] %s %s from %s\n",
                        time, exchange.getRequestMethod(),
                        exchange.getRequestURI(), exchange.getRemoteAddress());
                SwingUtilities.invokeLater(() -> logEditor.setText(logEditor.getText() + log));
                String body = exchange.getRequestURI().getPath().equals("/health")
                        ? "{\"status\":\"ok\"}" : "Hello from Developer Tools HTTP Server";
                exchange.getResponseHeaders().set("Content-Type",
                        exchange.getRequestURI().getPath().endsWith(".json")
                                ? "application/json" : "text/plain");
                exchange.sendResponseHeaders(200, body.length());
                try (var os = exchange.getResponseBody()) {
                    os.write(body.getBytes());
                }
            });
            server.setExecutor(null); // default executor
            server.start();
            running = true;
            startStopBtn.setText("Stop");
            statusLabel.setText("  Running on http://localhost:" + port.get());
        } catch (Exception e) {
            logEditor.setText(logEditor.getText() + "Error: " + e.getMessage() + "\n");
        }
    }

    private void stopServer() {
        if (server != null) { server.stop(0); server = null; }
        running = false;
        startStopBtn.setText("Start");
        statusLabel.setText("  Stopped");
    }

    @Override public void dispose() { stopServer(); super.dispose(); }

    public static final class Factory implements ToolFactory<HttpServerTool> {
        public Factory() {}
        @Override public String getId() { return "http-server"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("http-server", "HTTP Server", "HTTP Server")
                    .withGroupId("network");
        }
        @Override public HttpServerTool create(ToolConfiguration config) { return new HttpServerTool(config); }
    }
}
