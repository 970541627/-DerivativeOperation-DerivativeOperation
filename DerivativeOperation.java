import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DerivativeOperation {
    private final double DELTA_X = 0.00001;
    List<Double> saveResult = new ArrayList<>(2);
    Map<Integer, String> saveIndex = new ConcurrentHashMap<>(5);
    int startRead = 0;
    Stack numStack = new Stack();
    Stack operateStack = new Stack();
    int cycleTimes = 2;
    public double parseExpression(String s, double v) {
        //扫描原式子，检测指数类型，以逗号作为当前指数类型的结束符
        int len = s.length();
        List<Double> saveOrdinaryParameter = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            if (i + 1 < len) {
                if (s.charAt(i) == 'x') {
                    if (s.charAt(i + 1) == '^') {
                        StringBuffer stringBuffer = new StringBuffer();
                        char c = ' ';
                        for (int j = i + 2; j < len; j++) {
                            c = s.charAt(j);
                            if (c == ',') {
                                saveOrdinaryParameter.add(saveOrdinaryParameter.size(), Double.parseDouble(stringBuffer.substring(0)));
                                s = s.substring(0, i) + "d" + s.substring(j + 1);
                                len = s.length();
                                break;
                            }
                            stringBuffer.append(c);
                        }
                    }
                } else if (Character.isDigit(s.charAt(i))) {
                    //如果当前常数是指数
                    //1.指数含有未知数
                    //2.指数不含未知数
                    StringBuffer index = new StringBuffer();
                    StringBuffer digit = new StringBuffer();
                    int st = i;
                    digit.append(s.charAt(st));
                    char nextChar = s.charAt(st + 1);
                    while (Character.isDigit(nextChar)) {
                        digit.append(nextChar);
                        st++;
                        nextChar = s.charAt(st + 1);
                    }
                    if (s.charAt(st + 1) == '^') {
                        char c;
                        for (int j = st + 2; j < len; j++) {
                            c = s.charAt(j);
                            if (c == ',') {
                                saveIndex.put(startRead, digit.substring(0) + " " + index.substring(0));
                                s = s.substring(0, i) + "i" + s.substring(j + 1);
                                len = s.length();
                                startRead++;
                                break;
                            }

                            index.append(c);
                        }

                    }

                } else if (s.charAt(i) == 'l') {
                    StringBuffer index = new StringBuffer();
                    StringBuffer digit = new StringBuffer();
                    char c;
                    for (int j = i + 1; j < len; j++) {
                        c = s.charAt(j);
                        if (s.charAt(j) == '(') {
                            for (int k = j + 1; k < len; k++) {
                                c = s.charAt(k);
                                if (c == ')') {
                                    saveIndex.put(startRead, digit + " " + index);
                                    s = s.substring(0, i + 1) + s.substring(k + 1);
                                    len = s.length();
                                    startRead++;
                                    break;
                                }
                                index.append(c);
                            }
                            break;
                        }
                        digit.append(c);
                    }

                } else if (s.charAt(i) == 's' || s.charAt(i) == 'c' || s.charAt(i) == 't') {
                    StringBuffer digit = new StringBuffer();
                    char c;
                    for (int j = i + 1; j < len; j++) {
                        c = s.charAt(j);
                        if (c == ',') {
                            saveIndex.put(startRead, digit.toString());
                            s = s.substring(0, i + 1) + s.substring(j + 1);
                            len = s.length();
                            startRead++;
                            break;
                        }
                        digit.append(c);
                    }
                }

            }
        }
        while (cycleTimes != 0) {
            putAndPopStack(len, s, v, saveOrdinaryParameter, true);
            cycleTimes--;
            v = v + DELTA_X;
        }
        double v1 = (saveResult.get(0) - saveResult.get(1)) / DELTA_X;
        saveResult.clear();
        return Double.parseDouble(new DecimalFormat("#.00").format(v1));
    }

    public double putAndPopStack(int len, String s, double v, List list, boolean isMainExpression) {
        String[] all = new String[len];
        int offset = 0;
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c) && c != '^') {
                if (stringBuffer.length() != 0) {
                    all[offset] = stringBuffer.substring(0);
                    stringBuffer.delete(0, stringBuffer.length());
                    offset++;
                }
                all[offset] = String.valueOf(c);
                offset++;
            } else if (c == '^') {
                StringBuffer temporaryBuffer = new StringBuffer();
                int j;
                for (j = i + 1; j < len; j++) {
                    char getChar = s.charAt(j);
                    if (Character.isDigit(getChar)) {
                        temporaryBuffer.append(getChar);
                    }
                }
                String getRes = calculateSpecialIndex(all[offset - 1], temporaryBuffer.substring(0), v);
                return Double.parseDouble(getRes);
            } else {
                stringBuffer.append(c);
            }
            if (stringBuffer.length() != 0) {
                all[len - 1] = stringBuffer.substring(0);
            }
        }

        startRead = 0;
        String s1;
        for (int i = 0; i < all.length; i++) {
            s1 = all[i];
            if (s1.matches("[0-9]*") ||
                    s1.equals("d") ||
                    s1.equals("x") ||
                    s1.equals("i") ||
                    s1.equals("l") ||
                    s1.equals("s") ||
                    s1.equals("c") ||
                    s1.equals("t")) {
                numStack.push(all[i]);
            } else if (s1.equals(")")) {
                numStack.push(operateStack.pop());
                operateStack.pop();
            } else {
                if (s1.equals("+") || s1.equals("-")) {
                    int index = 0;
                    String[] strings = new String[numStack.size()];
                    while (operateStack.size() != 0 && (operateStack.peek().equals("*") || operateStack.peek().equals("/"))) {
                        strings[index] = (String) operateStack.pop();
                        numStack.push(strings[index]);
                        index++;
                    }
                } else if (operateStack.size() != 0 && (s1.equals("*") || s1.equals("/"))) {
                    if (operateStack.peek().equals("*") || operateStack.peek().equals("/")) {
                        numStack.push(operateStack.pop());
                    }
                }
                operateStack.push(s1);
            }
        }
        int oSize = operateStack.size();
        if (oSize != 0) {
            for (int i = 0; i < oSize; i++) {
                numStack.push(operateStack.pop());
            }
        }
        calculate(v, list, numStack);
        double peek = (double) numStack.pop();
        if (isMainExpression) {
            saveResult.add(0, peek);
        }
        return peek;
    }

    private void calculate(double v, List<Double> list, Stack numStack) {
        Iterator<Map.Entry<Integer, String>> iterator = saveIndex.entrySet().stream().iterator();
        Stack stack = new Stack();
        int nSize = numStack.size();
        int index = 0;
        double num1;
        double num2;
        for (int i = 0; i < nSize; i++) {
            stack.push(numStack.pop());
        }
        for (int i = 0; i < nSize; i++) {
            String s1 = (String) stack.pop();
            if (s1.matches("^[0-9]+(.[0-9]+)?$")) {
                numStack.push(Double.parseDouble(s1));
            } else if (s1.equals("d")) {
                numStack.push(Math.pow(v, list.get(index)));
                index++;
            } else if (s1.equals("x")) {
                numStack.push(v);
            } else if (s1.equals("i")) {
                Map.Entry<Integer, String> next = iterator.next();
                String value = next.getValue();
                String[] splitValue = value.split(" ");
                int length = splitValue[1].length();
                switch (length) {
                    case 1:
                        if (splitValue[1].equals("x")) {
                            numStack.push(Math.pow(Double.parseDouble(value), v));
                        } else {
                            numStack.push(Math.pow(Double.parseDouble(value), Double.parseDouble(splitValue[1])));
                        }
                        break;
                    default:
                        double v1 = putAndPopStack(length, splitValue[1], v, null, false);
                        numStack.push(Math.pow(Double.parseDouble(splitValue[0]), v1));
                        break;
                }

            } else if (s1.equals("l")) {
                Map.Entry<Integer, String> next = iterator.next();
                String value = next.getValue();
                String[] splitValue = value.split(" ");
                int length = splitValue[1].length();
                switch (length) {
                    case 1:
                        if (splitValue[1].equals("x")) {
                            numStack.push(calculateLog(splitValue[0], "x", v));
                        } else {
                            numStack.push(calculateLog(splitValue[0], splitValue[1], 0));
                        }
                        break;
                    default:
                        double v1 = putAndPopStack(length, splitValue[1], v, null, false);
                        numStack.push(calculateLog(splitValue[0], String.valueOf(v1), v));
                        break;
                }
            } else if (s1.equals("s") || s1.equals("c") || s1.equals("t")) {
                Map.Entry<Integer, String> next = iterator.next();
                String value = next.getValue();
                int length = value.length();
                switch (length) {
                    case 1:
                        double v2 = calculateTrigonometricFunction(s1, value, v);
                        numStack.push(v2);
                        break;
                    default:
                        double v1 = putAndPopStack(length, value, v, null, false);
                        numStack.push(calculateTrigonometricFunction(s1, String.valueOf(v1), v));
                        break;
                }
            } else {
                num2 = (double) numStack.pop();
                num1 = (double) numStack.pop();
                double res = 0;
                if (s1.equals("+")) {
                    res = num1 + num2;
                } else if (s1.equals("-")) {
                    res = num1 - num2;
                } else if (s1.equals("*")) {
                    res = num1 * num2;
                } else if (s1.equals("/")) {
                    res = num1 / num2;
                }
                numStack.push(res);
            }
        }
    }

    public String calculateSpecialIndex(String power, String index, double v) {
        if (power.equals("x")) {
            return String.valueOf(Math.pow(Double.parseDouble(power.replace("x", v + "")), Double.parseDouble(index)));
        }
        return String.valueOf(Math.pow(Double.parseDouble(power), Double.parseDouble(index)));
    }

    public double calculateLog(String power, String index, double v) {
        if (power.equals("x")) {
            return Math.log(Double.parseDouble(index)) / Math.log(v);
        } else if (index.equals("x")) {
            return Math.log(v) / Math.log(Double.parseDouble(power));
        }
        return Math.log(Double.parseDouble(index)) / Math.log(Double.parseDouble(power));
    }

    public double calculateTrigonometricFunction(String triType, String param, double v) {
        param = param.replace("x", v + "");
        switch (triType) {
            case "s":
                double sin = Math.sin(Double.parseDouble(param));
                return sin;
            case "c":
                return Math.cos(Double.parseDouble(param));

            case "t":
                return Math.tan(Double.parseDouble(param));

            default:
                throw new RuntimeException("UnKnown Type ====》 " + triType);
        }
    }
}