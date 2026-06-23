# Developer Tools Desktop

A collection of 40+ developer utilities in a single desktop application. Built with **Java 25**, **Swing**, and **FlatLaf** — compiled to native binaries with **GraalVM** for instant startup and low memory usage.

## Download

Pre-built native binaries for Linux, macOS, and Windows are available on the [Releases page](https://github.com/gtiwari333/developer-tools-desktop/releases). No JRE required.

| Platform | Binary |
|----------|--------|
| Linux | `developer-tools` |
| macOS | `developer-tools` |
| Windows | `developer-tools.exe` |

## Tools

### Encoders / Decoders
Base64, Base32, URL Base64, MIME Base64, ASCII/Hex, URL Encoding, JWT (HS256/384/512)

### Text Escape
HTML Entities, Java String, JSON String, CSV, XML, Escape Sequences (`\n`, `\t`, `\\`)

### Cryptography
UUID (v1–v7), Nano ID, ULID, Password Generator, Lorem Ipsum, Hashing (SHA-1/256/384/512, MD5), HMAC

### Text Utilities
Sorting, Case Conversion (camelCase, snake_case, PascalCase, etc.), Filtering, Text Statistics, Text Diff

### Formatters
SQL Formatter, CLI Command Converter (split/join with `\` continuations)

### Standalone Tools
Config Format Converter (JSON ↔ YAML ↔ XML ↔ TOML), Regex Matcher, JSON Path Evaluator, Date/Time Converter, Cron Expression Editor, Color Picker, QR Code Generator, ASCII Art (FIGlet), Unit Converter, Server Certificates, Archive Inspector (ZIP/TAR/TAR.GZ), HTTP Server, Notes, Rubber Duck Debugging

## Features

- **Persistent tabs** — open multiple tools side-by-side, each with independent state
- **Pin tabs** — keep frequently used tools open across sessions
- **Live preview** — many tools support live conversion as you type
- **Dark & light themes** — auto-detects system preference
- **Keyboard shortcuts** — `Ctrl+F` search, `Ctrl+N` new workbench, `Ctrl+W` close tab, `Ctrl+,` settings
- **Auto-update** — checks GitHub Releases for new versions on startup
- **Portable config** — all settings and tool state stored in `~/.developer-tools/`

## Build from Source

### Prerequisites
- [GraalVM JDK 25](https://www.graalvm.org/) (or any JDK 25 for development)
- Gradle 8+ (wrapper included)

### Run in development
```bash
./gradlew :app:run
```

### Build native image
```bash
./gradlew :app:nativeCompile
# Output: app/build/native/nativeCompile/developer-tools
```

### Build for all platforms (CI)
Pushing a tag triggers the GitHub Actions workflow:
```bash
git tag v1.0.0
git push origin v1.0.0
```

Or trigger manually from the **Actions** tab → **Build Native Images** → **Run workflow**.

## Project Structure

```
developer-tools-desktop/
├── app/                          # Main Swing application
├── modules/
│   ├── common/                   # ValueProperty<T>, platform utilities
│   ├── settings/                 # JSON config persistence, update checker
│   ├── tools-api/                # Core framework (DeveloperTool, Converter, etc.)
│   ├── tools-encoders/           # Base64, Base32, JWT, etc.
│   ├── tools-escape/             # HTML, JSON, XML, CSV escapers
│   ├── tools-crypto/             # UUID, NanoID, Password generators
│   ├── tools-text/               # Hashing, sorting, diff, case conversion
│   ├── tools-formatters/         # SQL formatter, CLI converter
│   └── tools-standalone/         # Regex, JSONPath, QR, HTTP server, etc.
├── .github/workflows/            # CI/CD pipeline (GraalVM native images)
└── gradle/                       # Build configuration
```

### Adding a new tool

1. Pick a base class: `EncoderDecoder`, `TextTransformer`, `OneLineTextGenerator`, or plain `DeveloperTool`
2. Implement the tool logic and a `Factory` inner class
3. Register the factory in your module's `ToolProvider`
4. Add the module's `ToolProvider` via `module-info.java` or `META-INF/services`

Example:
```java
public final class MyTool extends EncoderDecoder {
    private MyTool(ToolConfiguration config) { super(config); }
    
    protected byte[] doConvertForward(byte[] input) { /* encode */ }
    protected byte[] doConvertBackward(byte[] input) { /* decode */ }
    
    public static final class Factory implements ToolFactory<MyTool> {
        public Factory() {}
        public String getId() { return "my-tool"; }
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("my-tool", "My Tool", "My Tool").withGroupId("encoders");
        }
        public MyTool create(ToolConfiguration config) { return new MyTool(config); }
    }
}
```

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 25 |
| UI | Swing + FlatLaf 3.6 |
| Editor | RSyntaxTextArea 3.6 |
| JSON/YAML/XML/TOML | Jackson 3.0 |
| JWT | jose4j |
| QR Code | ZXing |
| Diff | java-diff-utils |
| Cron | cron-utils |
| Build | Gradle + GraalVM Native Build Tools |
| CI/CD | GitHub Actions |



## Future plans

#### Low effort

```
┌───────────────────────┬────────────────────────────────────────────────────────────────────┐
│         Tool          │                            What it does                            │
├───────────────────────┼────────────────────────────────────────────────────────────────────┤
│ Markdown Preview      │ Edit markdown → see rendered HTML side-by-side                     │
├───────────────────────┼────────────────────────────────────────────────────────────────────┤
│ Random Data Generator │ Generate names, emails, addresses, phone numbers for testing       │
├───────────────────────┼────────────────────────────────────────────────────────────────────┤
│ String Inspector      │ Show Unicode code points, byte representation, character details   │
├───────────────────────┼────────────────────────────────────────────────────────────────────┤
│ Slug Generator        │ Convert text → URL-friendly slugs ("Hello World!" → "hello-world") │
├───────────────────────┼────────────────────────────────────────────────────────────────────┤
│ Checksum Verifier     │ Compute and verify MD5/SHA checksums for files                     │
├───────────────────────┼────────────────────────────────────────────────────────────────────┤
│ IP Subnet Calculator  │ CIDR notation → IP range, broadcast, subnet mask                   │
├───────────────────────┼────────────────────────────────────────────────────────────────────┤
│ Base64 Image Encoder  │ Drag an image → get data:image/png;base64,... string               │
├───────────────────────┼────────────────────────────────────────────────────────────────────┤
│ CSV Viewer            │ Load CSV → sortable/filterable spreadsheet-like table              │
├───────────────────────┼────────────────────────────────────────────────────────────────────┤
│ Environment Variables │ Read system env vars, format as JSON/shell exports                 │
├───────────────────────┼────────────────────────────────────────────────────────────────────┤
│ Timer / Stopwatch     │ Simple count-up/count-down timer                                   │
└───────────────────────┴────────────────────────────────────────────────────────────────────┘
```

#### Medium Effort
```
┌────────────────────────┬───────────────────────────────────────────────────────────┐
│          Tool          │                       What it does                        │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ API Request Tester     │ Send GET/POST requests, set headers/body, see response    │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ SSH Key Generator      │ Generate RSA/Ed25519 key pairs, show fingerprint          │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ GraphQL Formatter      │ Format/minify GraphQL queries, extract operations         │                              
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ Mock Data Generator    │ Generate realistic JSON arrays of users, orders, products │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ OAuth2 Token Decoder   │ Decode JWT access tokens, show claims and expiry          │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ Code Snippet Generator │ Java/Kotlin: generate equals/hashCode, builder, toString  │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ Docker Run Generator   │ Fill in fields → get docker run command line              │
└────────────────────────┴───────────────────────────────────────────────────────────┘
```

#### Bigger effort
```
┌───────────────────────────┬──────────────────────────────────────────────────┐
│           Tool            │                   What it does                   │
├───────────────────────────┼──────────────────────────────────────────────────┤
│ Database Schema from JSON │ Paste JSON → generate CREATE TABLE SQL           │
├───────────────────────────┼──────────────────────────────────────────────────┤
│ OpenAPI/Swagger Viewer    │ Load OpenAPI spec → browse endpoints and schemas │
├───────────────────────────┼──────────────────────────────────────────────────┤
│ Image Converter           │ Resize, convert PNG↔JPG↔WebP, optimize           │
├───────────────────────────┼──────────────────────────────────────────────────┤
│ WebSocket Test Client     │ Connect to WS endpoint, send/receive messages    │
├───────────────────────────┼──────────────────────────────────────────────────┤
```

## License

MIT License — see [LICENSE](LICENSE) file.
