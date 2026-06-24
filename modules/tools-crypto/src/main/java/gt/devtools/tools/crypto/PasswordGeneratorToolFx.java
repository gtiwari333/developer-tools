package gt.devtools.tools.crypto;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.OneLineTextGeneratorFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class PasswordGeneratorToolFx extends OneLineTextGeneratorFx {
    private final ValueProperty<Integer> length; private final ValueProperty<Boolean> useUpper, useLower, useDigits, useSymbols;
    private PasswordGeneratorToolFx(ToolConfiguration config) {
        super(config); length = registerConfig("pwdLength", 24);
        useUpper = registerConfig("pwdUpper", true); useLower = registerConfig("pwdLower", true);
        useDigits = registerConfig("pwdDigits", true); useSymbols = registerConfig("pwdSymbols", true); }
    @Override protected void buildConfigurationUi(VBox panel) {
        var r1 = new HBox(8); r1.getChildren().add(new Label("Length:"));
        var spin = new Spinner<Integer>(4, 256, length.get());
        spin.valueProperty().addListener((o, w, v) -> length.set(v)); r1.getChildren().add(spin); panel.getChildren().add(r1);
        var r2 = new HBox(8);
        javafx.scene.control.CheckBox cb;
        cb = new CheckBox("A-Z"); cb.setSelected(useUpper.get()); cb.setOnAction(e -> useUpper.set(cb.isSelected())); r2.getChildren().add(cb);
        var cb2 = new CheckBox("a-z"); cb2.setSelected(useLower.get()); cb2.setOnAction(e -> useLower.set(cb2.isSelected())); r2.getChildren().add(cb2);
        var cb3 = new CheckBox("0-9"); cb3.setSelected(useDigits.get()); cb3.setOnAction(e -> useDigits.set(cb3.isSelected())); r2.getChildren().add(cb3);
        var cb4 = new CheckBox("!@#$"); cb4.setSelected(useSymbols.get()); cb4.setOnAction(e -> useSymbols.set(cb4.isSelected())); r2.getChildren().add(cb4);
        panel.getChildren().add(r2);
    }
    @Override protected String generate() { return PasswordGeneratorTool.generate(length.get(), useUpper.get(), useLower.get(), useDigits.get(), useSymbols.get()); }
    public static final class Factory implements ToolFxFactory<PasswordGeneratorToolFx> {
        public Factory() {} @Override public String getId() { return "password-generator-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("password-generator-fx", "Password Gen (FX)", "Password Generator").withGroupId("crypto"); }
        @Override public PasswordGeneratorToolFx create(ToolConfiguration c) { return new PasswordGeneratorToolFx(c); }
    }
}
