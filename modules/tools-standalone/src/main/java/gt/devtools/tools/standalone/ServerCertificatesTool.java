package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.TextEditor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.awt.*;
import java.security.cert.X509Certificate;
import java.time.Instant;

/** Fetches and displays TLS server certificates. */
public final class ServerCertificatesTool extends DeveloperTool {

    private JTextField hostField;
    private TextEditor outputEditor;
    private final ValueProperty<String> host;

    private ServerCertificatesTool(ToolConfiguration config) {
        super(config);
        this.host = registerConfig("certHost", "");
    }

    @Override
    protected void buildUi(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setLayout(new BorderLayout(0, 8));

        var configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configPanel.add(new JLabel("Host:Port:"));
        hostField = new JTextField(host.get(), 25);
        configPanel.add(hostField);
        var fetchBtn = new JButton("Fetch Certificate");
        fetchBtn.addActionListener(e -> fetch());
        configPanel.add(fetchBtn);
        panel.add(configPanel, BorderLayout.NORTH);

        outputEditor = new TextEditor(TextEditor.Mode.OUTPUT);
        outputEditor.setSyntaxStyle("text/plain");
        panel.add(outputEditor, BorderLayout.CENTER);
    }

    private void fetch() {
        String h = hostField.getText().strip();
        if (h.isEmpty()) { outputEditor.setText("Enter host:port (e.g., google.com:443)."); return; }
        if (!h.contains(":")) h = h + ":443";
        host.set(h);

        try {
            var ctx = SSLContext.getInstance("TLS");
            var certs = new X509Certificate[1];
            ctx.init(null, new TrustManager[]{new X509TrustManager() {
                @Override public void checkClientTrusted(X509Certificate[] c, String a) {}
                @Override public void checkServerTrusted(X509Certificate[] c, String a) {}
                @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }}, null);

            var sock = ctx.getSocketFactory().createSocket(
                    h.split(":")[0], Integer.parseInt(h.split(":")[1]));
            if (sock instanceof SSLSocket sslSock) {
                sslSock.setSoTimeout(5000);
                sslSock.startHandshake();
                certs = (X509Certificate[]) sslSock.getSession().getPeerCertificates();
                sslSock.close();
            }

            var sb = new StringBuilder();
            for (int i = 0; i < certs.length; i++) {
                X509Certificate cert = certs[i];
                sb.append("=== Certificate ").append(i + 1).append(" ===\n");
                sb.append("Subject:    ").append(cert.getSubjectX500Principal()).append("\n");
                sb.append("Issuer:     ").append(cert.getIssuerX500Principal()).append("\n");
                sb.append("Serial:     ").append(cert.getSerialNumber()).append("\n");
                sb.append("Valid from: ").append(cert.getNotBefore().toInstant()).append("\n");
                sb.append("Valid to:   ").append(cert.getNotAfter().toInstant()).append("\n");
                sb.append("Algorithm:  ").append(cert.getSigAlgName()).append("\n");
                long daysLeft = (cert.getNotAfter().getTime() - System.currentTimeMillis())
                        / (1000 * 60 * 60 * 24);
                sb.append("Days left:  ").append(daysLeft);
                if (daysLeft < 30) sb.append(" ⚠ EXPIRING SOON");
                sb.append("\n\n");
            }
            outputEditor.setText(sb.toString());
        } catch (Exception e) {
            outputEditor.setText("Error fetching certificate: " + e.getMessage());
        }
    }

    public static final class Factory implements ToolFactory<ServerCertificatesTool> {
        public Factory() {}
        @Override public String getId() { return "certificates-download"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("certificates-download",
                    "Server Certificates", "Server Certificates");
        }
        @Override public ServerCertificatesTool create(ToolConfiguration config) { return new ServerCertificatesTool(config); }
    }
}
