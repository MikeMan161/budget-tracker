public class InputValidation {

    public static boolean isValidArgument(String amountText) {
        if (amountText == null || amountText.trim().isEmpty()) {
            return false;
        }

        try {
            double amount = Double.parseDouble(amountText.trim());
            return amount > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static double parseAmount(String amountText) {
        return Double.parseDouble(amountText.trim());
    }

    public static boolean isValidCategory(String category) {
        return category != null && !category.trim().isEmpty();
    }

    public static boolean isValidDescription(String description) {
        return description != null && !description.trim().isEmpty();
    }

    public static boolean isValidType(String type) {
        return type != null &&
                (type.equalsIgnoreCase("Income") || type.equalsIgnoreCase("Expense"));
    }
}
