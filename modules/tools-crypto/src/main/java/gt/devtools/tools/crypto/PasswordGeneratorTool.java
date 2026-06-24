package gt.devtools.tools.crypto;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.generator.OneLineTextGenerator;

import javax.swing.*;
import java.awt.*;
// SecureRandom, ArrayList, List used via FQN in static methods

/**
 * Generates random passwords with configurable length and character classes.
 */
public final class PasswordGeneratorTool extends OneLineTextGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?";

    private final ValueProperty<Integer> length;
    private final ValueProperty<Boolean> useUpper;
    private final ValueProperty<Boolean> useLower;
    private final ValueProperty<Boolean> useDigits;
    private final ValueProperty<Boolean> useSymbols;

    private PasswordGeneratorTool(ToolConfiguration config) {
        super(config);
        this.length = registerConfig("pwdLength", 24);
        this.useUpper = registerConfig("pwdUpper", true);
        this.useLower = registerConfig("pwdLower", true);
        this.useDigits = registerConfig("pwdDigits", true);
        this.useSymbols = registerConfig("pwdSymbols", true);
    }

    @Override
    protected void buildConfigurationUi(JPanel configPanel) {
        var row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Length:"));
        var spinner = new JSpinner(new javax.swing.SpinnerNumberModel(
                length.get().intValue(), 4, 256, 1));
        spinner.addChangeListener(e -> length.set((Integer) spinner.getValue()));
        row1.add(spinner);
        configPanel.add(row1);

        var row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        var upperCb = new JCheckBox("A-Z", useUpper.get());
        upperCb.addActionListener(e -> useUpper.set(upperCb.isSelected()));
        row2.add(upperCb);
        var lowerCb = new JCheckBox("a-z", useLower.get());
        lowerCb.addActionListener(e -> useLower.set(lowerCb.isSelected()));
        row2.add(lowerCb);
        var digitsCb = new JCheckBox("0-9", useDigits.get());
        digitsCb.addActionListener(e -> useDigits.set(digitsCb.isSelected()));
        row2.add(digitsCb);
        var symbolsCb = new JCheckBox("!@#$", useSymbols.get());
        symbolsCb.addActionListener(e -> useSymbols.set(symbolsCb.isSelected()));
        row2.add(symbolsCb);
        configPanel.add(row2);
    }

    @Override
    protected String generate() {
        return generate(length.get(), useUpper.get(), useLower.get(),
                useDigits.get(), useSymbols.get());
    }

    // -- public static methods (testable without Swing)

    /** Generates a random password with the given constraints. */
    public static String generate(int length, boolean upper, boolean lower,
                                  boolean digits, boolean symbols) {
        var pools = new java.util.ArrayList<String>();
        if (upper) pools.add(UPPER);
        if (lower) pools.add(LOWER);
        if (digits) pools.add(DIGITS);
        if (symbols) pools.add(SYMBOLS);
        if (pools.isEmpty()) pools.add(LOWER);

        String chars = String.join("", pools);
        var rng = new java.security.SecureRandom();
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /** Returns the pool of characters available given the constraints. */
    public static String charPool(boolean upper, boolean lower,
                                  boolean digits, boolean symbols) {
        var pools = new java.util.ArrayList<String>();
        if (upper) pools.add(UPPER);
        if (lower) pools.add(LOWER);
        if (digits) pools.add(DIGITS);
        if (symbols) pools.add(SYMBOLS);
        if (pools.isEmpty()) pools.add(LOWER);
        return String.join("", pools);
    }

    public static final class Factory implements ToolFactory<PasswordGeneratorTool> {
        public Factory() {}
        @Override public String getId() { return "password-generator"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("password-generator",
                    "Password Generator", "Password Generator").withGroupId("crypto");
        }
        @Override
        public PasswordGeneratorTool create(ToolConfiguration config) { return new PasswordGeneratorTool(config); }
    }
}
