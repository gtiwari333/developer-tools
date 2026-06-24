package gt.devtools.tools.standalone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UnitConverterTool static methods")
class UnitConverterToolTest {

    @Test @DisplayName("convertDataSize: base-1024 conversion")
    void dataSize1024() {
        var result = UnitConverterTool.convertDataSize(1024, 1024);
        assertThat(result).containsEntry("bytes", 1024.0);
        assertThat(result).containsEntry("KB", 1.0);
        assertThat(result).containsEntry("MB", 1024.0 / (1024.0 * 1024.0));
    }

    @Test @DisplayName("convertDataSize: base-1000 conversion")
    void dataSize1000() {
        var result = UnitConverterTool.convertDataSize(1000, 1000);
        assertThat(result).containsEntry("bytes", 1000.0);
        assertThat(result).containsEntry("KB", 1.0);
    }

    @Test @DisplayName("convertDataSize: zero bytes")
    void dataSizeZero() {
        var result = UnitConverterTool.convertDataSize(0, 1024);
        assertThat(result.get("bytes")).isEqualTo(0.0);
        assertThat(result.get("KB")).isEqualTo(0.0);
    }

    @Test @DisplayName("convertDataSize: all units present")
    void dataSizeAllUnits() {
        var result = UnitConverterTool.convertDataSize(1, 1024);
        assertThat(result).containsKeys("bytes", "KB", "MB", "GB", "TB", "PB");
    }

    @Test @DisplayName("convertNumberBases: returns all representations")
    void numberBases() {
        var result = UnitConverterTool.convertNumberBases(255);
        assertThat(result).hasSize(5);
        assertThat(result.get(0)).isEqualTo("Binary:     11111111");
        assertThat(result.get(1)).isEqualTo("Octal:      377");
        assertThat(result.get(2)).isEqualTo("Decimal:    255");
        assertThat(result.get(3)).isEqualTo("Hex:        FF");
        assertThat(result.get(4)).isEqualTo("Hex (0x):   0xFF");
    }

    @Test @DisplayName("convertNumberBases: zero")
    void numberBasesZero() {
        var result = UnitConverterTool.convertNumberBases(0);
        assertThat(result.get(0)).isEqualTo("Binary:     0");
        assertThat(result.get(3)).isEqualTo("Hex:        0");
    }
}
