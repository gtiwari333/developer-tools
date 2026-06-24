package gt.devtools.tools.standalone;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Parses, describes, and shows next execution times for cron expressions. */
public final class CronExpressionTool extends TextTransformer {

    private CronExpressionTool(ToolConfiguration config) { super(config); }

    @Override
    protected void buildUi(JPanel panel) {
        super.buildUi(panel);
    }

    @Override
    protected String getTransformLabel() { return "Describe"; }

    @Override
    protected String doTransform(String input) {
        if (input.isBlank()) return "Enter a cron expression (e.g., 0 0 * * *).";
        try {
            var definition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
            var parser = new CronParser(definition);
            var cron = parser.parse(input.trim());
            var descriptor = CronDescriptor.instance(Locale.ENGLISH);
            var execTime = ExecutionTime.forCron(cron);

            var sb = new StringBuilder();
            sb.append("Expression:  ").append(cron.asString()).append("\n");
            sb.append("Description: ").append(descriptor.describe(cron)).append("\n\n");
            sb.append("Next 5 executions:\n");
            ZonedDateTime next = ZonedDateTime.now();
            var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
            for (int i = 0; i < 5; i++) {
                var opt = execTime.nextExecution(next);
                if (opt.isEmpty()) break;
                next = opt.get();
                sb.append("  ").append(next.format(fmt)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Invalid cron expression: " + e.getMessage();
        }
    }

    public static final class Factory implements ToolFactory<CronExpressionTool> {
        public Factory() {}
        @Override public String getId() { return "cron-expression"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("cron-expression",
                    "Cron Expression", "Cron Expression Editor")
                    .withGroupId("formatters");
        }
        @Override public CronExpressionTool create(ToolConfiguration config) { return new CronExpressionTool(config); }
    }
}
