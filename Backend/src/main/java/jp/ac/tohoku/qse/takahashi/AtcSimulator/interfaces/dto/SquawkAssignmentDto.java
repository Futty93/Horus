package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SquawkAssignmentDto(
        @NotNull(message = "squawk is required")
        @Pattern(regexp = "^[0-7]{4}$", message = "squawk must be 4 octal digits")
        String squawk) {
}
