package top.realme.mc.precipitate_power.util;

/**
 * 公式解析器，用于将配置文件中的「沉淀等级」对应的发电量/tick进行，当然也可以进行别的计算
 */
public final class FormulaParser {
    private FormulaParser() {
    }

    /**
     * 传入沉淀等级x，并执行计算得到tick产电量
     * @param formula 公式字符串
     * @param x 沉淀等级（当然也可以是别的）
     * @return 嘟嘟
     */
    public static double evaluate(String formula, double x) {
        try {
            Parser parser = new Parser(formula, x);
            double value = parser.parseExpression();
            parser.skipWhitespace();
            if (!parser.isAtEnd()) {
                throw new IllegalArgumentException("Trailing input");
            }
            return value;
        } catch (RuntimeException exception) {
            return 0.0D;
        }
    }


    /**
     * AI写的，我不会写（真的不会吗宝宝）
     */
    private static final class Parser {
        private final String input;
        private final double x;
        private int index;

        private Parser(String input, double x) {
            this.input = input;
            this.x = x;
        }

        private double parseExpression() {
            double value = parseTerm();
            while (true) {
                skipWhitespace();
                if (match('+')) {
                    value += parseTerm();
                } else if (match('-')) {
                    value -= parseTerm();
                } else {
                    return value;
                }
            }
        }

        private double parseTerm() {
            double value = parseFactor();
            while (true) {
                skipWhitespace();
                if (match('*')) {
                    value *= parseFactor();
                } else if (match('/')) {
                    value /= parseFactor();
                } else {
                    return value;
                }
            }
        }

        private double parseFactor() {
            double value = parseUnary();
            skipWhitespace();
            if (match('^')) {
                value = Math.pow(value, parseFactor());
            }
            return value;
        }

        private double parseUnary() {
            skipWhitespace();
            if (match('+')) {
                return parseUnary();
            }
            if (match('-')) {
                return -parseUnary();
            }
            return parsePrimary();
        }

        private double parsePrimary() {
            skipWhitespace();
            if (match('(')) {
                double value = parseExpression();
                expect(')');
                return value;
            }

            if (!isAtEnd()) {
                char current = input.charAt(index);
                if (current == 'x' || current == 'X') {
                    index++;
                    return x;
                }
            }

            int start = index;
            while (!isAtEnd()) {
                char current = input.charAt(index);
                if ((current >= '0' && current <= '9') || current == '.') {
                    index++;
                    continue;
                }
                break;
            }
            if (start == index) {
                throw new IllegalArgumentException("Expected number");
            }
            return Double.parseDouble(input.substring(start, index));
        }

        private void expect(char expected) {
            skipWhitespace();
            if (!match(expected)) {
                throw new IllegalArgumentException("Expected " + expected);
            }
        }

        private boolean match(char expected) {
            if (!isAtEnd() && input.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void skipWhitespace() {
            while (!isAtEnd() && Character.isWhitespace(input.charAt(index))) {
                index++;
            }
        }

        private boolean isAtEnd() {
            return index >= input.length();
        }
    }
}
