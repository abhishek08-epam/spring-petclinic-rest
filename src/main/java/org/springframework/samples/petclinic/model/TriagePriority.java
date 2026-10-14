package org.springframework.samples.petclinic.model;

public enum TriagePriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int level;

    TriagePriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static TriagePriority fromSeverity(SymptomSeverity severity) {
        return switch (severity) {
            case SEVERE -> HIGH;
            case MODERATE -> MEDIUM;
            case MILD -> LOW;
        };
    }
}
