package net.gazeplay.games.gazeplayEval.config;

public enum ResultsOutputType {
    CSV, XLS, ALL;

    // function for getting extension name
    public String getExtension() {
        return switch (this) {
            case CSV -> ".csv";
            case XLS -> ".xls";
            default -> throw new IllegalArgumentException("No Output set or wrong statement");
        };
    }
}
