package kg.alex.abl.enums;

public enum InstallmentPlanTypeCode {

    MONTHLY(true),
    QUARTERLY(true),
    FULL(true),
    OTHER(false);

    private final boolean autoStartDate;

    InstallmentPlanTypeCode(boolean autoStartDate) {
        this.autoStartDate = autoStartDate;
    }

    public boolean isAutoStartDate() {
        return autoStartDate;
    }

    public static InstallmentPlanTypeCode fromString(String value) {
        if (value == null) {
            return OTHER;
        }

        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}