package gt.devtools.tools.standalone;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.FxTextEditor;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public final class ServerCertificatesToolFx extends DeveloperToolFx {
    private TextField hostField;
    private FxTextEditor outputEditor;

    private ServerCertificatesToolFx(ToolConfiguration config) { super(config); }

    @Override protected void buildUi(BorderPane panel) {
        panel.setPadding(new Insets(8));
        var bar = new HBox(8);
        bar.getChildren().add(new Label("Host:Port:"));
        hostField = new TextField("google.com:443");
        hostField.setPrefColumnCount(30);
        bar.getChildren().add(hostField);
        var fetchBtn = new Button("Fetch Certificates");
        fetchBtn.setOnAction(e -> fetchCerts());
        bar.getChildren().add(fetchBtn);
        panel.setTop(bar);

        outputEditor = new FxTextEditor(FxTextEditor.Mode.OUTPUT);
        panel.setCenter(outputEditor);
    }

    private void fetchCerts() {
        String hostPort = hostField.getText().strip();
        if (hostPort.isEmpty()) return;
        try {
            String[] parts = hostPort.split(":");
            String host = parts[0]; int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 443;
            var tm = new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }};
            var ctx = SSLContext.getInstance("TLS"); ctx.init(null, tm, null);
            var socket = (javax.net.ssl.SSLSocket) ctx.getSocketFactory().createSocket(host, port);
            socket.startHandshake();
            var certs = (X509Certificate[]) socket.getSession().getPeerCertificates();
            var sb = new StringBuilder();
            for (int i = 0; i < certs.length; i++) {
                var cert = certs[i];
                sb.append("Certificate ").append(i + 1).append(":\n");
                sb.append("  Subject: ").append(cert.getSubjectX500Principal()).append("\n");
                sb.append("  Issuer:  ").append(cert.getIssuerX500Principal()).append("\n");
                sb.append("  Valid:   ").append(cert.getNotBefore()).append(" to ").append(cert.getNotAfter()).append("\n");
                sb.append("  Serial:  ").append(cert.getSerialNumber()).append("\n\n");
            }
            socket.close();
            outputEditor.setText(sb.toString());
        } catch (Exception e) { outputEditor.setText("Error: " + e.getMessage()); }
    }

    public static final class Factory implements ToolFxFactory<ServerCertificatesToolFx> {
        public Factory() {} @Override public String getId() { return "certificates-download"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("certificates-download", "Server Certs", "Server Certificates").withGroupId("network"); }
        @Override public ServerCertificatesToolFx create(ToolConfiguration c) { return new ServerCertificatesToolFx(c); }
    }
}
