package gt.devtools.tools.standalone;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class CronExpressionToolFx extends TextTransformerFx {
    private CronExpressionToolFx(ToolConfiguration config) { super(config); }
    @Override protected String getTransformLabel() { return "Describe"; }
    @Override protected String doTransform(String input) {
        if (input.isBlank()) return "Enter a cron expression (e.g., 0 0 * * *).";
        try {
            var def = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
            var cron = new CronParser(def).parse(input.trim());
            var desc = CronDescriptor.instance(Locale.ENGLISH);
            var exec = ExecutionTime.forCron(cron);
            var sb = new StringBuilder();
            sb.append("Expression:  ").append(cron.asString()).append("\n");
            sb.append("Description: ").append(desc.describe(cron)).append("\n\nNext 5 executions:\n");
            ZonedDateTime next = ZonedDateTime.now();
            var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
            for (int i = 0; i < 5; i++) {
                var opt = exec.nextExecution(next); if (opt.isEmpty()) break;
                next = opt.get(); sb.append("  ").append(next.format(fmt)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) { return "Invalid cron expression: " + e.getMessage(); }
    }
    public static final class Factory implements ToolFxFactory<CronExpressionToolFx> {
        public Factory() {} @Override public String getId() { return "cron-expression"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("cron-expression", "Cron Expression", "Cron Expression Editor").withGroupId("formatters"); }
        @Override public CronExpressionToolFx create(ToolConfiguration c) { return new CronExpressionToolFx(c); }
    }
}
