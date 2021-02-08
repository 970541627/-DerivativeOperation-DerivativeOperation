import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Derivative {

    private final double DELTA_X = 0.00001;
    private double[] saveResult = new double[2];
    private int in = 0;
    //用于保存指数相关的map集合
    private List<String> saveIndex = new ArrayList<>();
    private int startRead = 0;
    //存放数字的栈
    private Stack numStack = new Stack();
    //存放符号的栈
    private Stack operateStack = new Stack();
    //保存指数的字符串
    StringBuffer index = new StringBuffer();
    //保存幂
    StringBuffer digit = new StringBuffer();

    //解析字符串将，并对字符串进行转换
    public double parseExpression(String s, double v) {
        //扫描原式子，检测指数类型，以逗号作为当前指数类型的结束符
        int len = s.length();
        List<Double> saveOrdinaryParameter = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            if (i + 1 < len) {
                //当前字符若为x
                if (s.charAt(i) == 'x') {
                    //进一步判断是否为指数
                    if (s.charAt(i + 1) == '^') {
                        StringBuffer stringBuffer = new StringBuffer();
                        char c = ' ';
                        for (int j = i + 2; j < len; j++) {
                            c = s.charAt(j);
                            //当前字符为逗号，终止循环
                            if (c == ',') {
                                saveOrdinaryParameter.add(saveOrdinaryParameter.size(), Double.parseDouble(stringBuffer.substring(0)));
                                s = s.substring(0, i) + "d" + s.substring(j + 1);
                                len = s.length();
                                break;
                            }
                            //拼接多位数
                            stringBuffer.append(c);
                        }
                    }
                    //Character.isDigit()表示当前字符串是否为数字
                } else if (Character.isDigit(s.charAt(i))) {
                    //如果当前常数是指数
                    //1.指数含有未知数
                    //2.指数不含未知数
                    int st = i;
                    //先将当前数字写入digit
                    digit.append(s.charAt(st));
                    //获取下一个字符
                    char nextChar = s.charAt(st + 1);

                    //循环判断当前字符是否为数字
                    while (Character.isDigit(nextChar)) {
                        digit.append(nextChar);
                        st++;
                        nextChar = s.charAt(st + 1);
                    }
                    //判断当前字符是否含有指数符号
                    if (s.charAt(st + 1) == '^') {
                        char c;
                        for (int j = st + 2; j < len; j++) {
                            c = s.charAt(j);

                            //逗号为结束符
                            if (c == ',') {
                                saveIndex.add(startRead, digit.substring(0) + " " + index.substring(0));
                                s = s.substring(0, i) + "i" + s.substring(j + 1);
                                len = s.length();
                                startRead++;
                                digit.setLength(0);
                                index.setLength(0);
                                break;
                            }
                            //拼接指数符号后面的指数
                            index.append(c);
                        }

                    }

                    //l表示这个部分是不是对数 格式例子 l4（x+3） 等同于 log以4为底真数为x+3
                } else if (s.charAt(i) == 'l') {
                    char c;
                    for (int j = i + 1; j < len; j++) {
                        c = s.charAt(j);
                        //当前字符为 “(” ，开始解析括号里面的真数
                        if (s.charAt(j) == '(') {
                            for (int k = j + 1; k < len; k++) {
                                c = s.charAt(k);
                                // “)” 为结束符
                                if (c == ')') {
                                    saveIndex.add(startRead, digit + " " + index);
                                    s = s.substring(0, i + 1) + s.substring(k + 1);
                                    len = s.length();
                                    startRead++;
                                    digit.setLength(0);
                                    index.setLength(0);
                                    break;
                                }
                                index.append(c);
                            }
                            break;
                        }
                        digit.append(c);
                    }

                    //判断当前字符是否是三角函数
                } else if (s.charAt(i) == 's' || s.charAt(i) == 'c' || s.charAt(i) == 't') {
                    char c;
                    for (int j = i + 1; j < len; j++) {
                        c = s.charAt(j);
                        if (c == ',') {
                            saveIndex.add(startRead, digit.toString());
                            s = s.substring(0, i + 1) + s.substring(j + 1);
                            len = s.length();
                            startRead++;
                            digit.setLength(0);
                            break;
                        }
                        digit.append(c);
                    }
                }

            }
        }
        //根据导数的定义式，所以要运行两次
        for (int i = 0; i <2 ; i++) {
            putAndPopStack(len, s, v, saveOrdinaryParameter, true);
            v = v + DELTA_X;
        }

        double v1 = (saveResult[1] - saveResult[0]) / DELTA_X;
        return Double.parseDouble(new DecimalFormat("#.00").format(v1));
    }

    //入栈出栈操作，涉及 后缀运算法之栈的实现知识，
    private double putAndPopStack(int len, String s, double v, List list, boolean isMainExpression) {
        String[] all = new String[len];
        int offset = 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            //如果当前不为数字和不是指数符号
            if (!Character.isDigit(c) && c != '^') {
                if (digit.length() != 0) {
                    all[offset] = digit.substring(0);
                    digit.setLength(0);
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
                //当前字符为数字，添加，即用来处理多位数和小数；
                digit.append(c);
            }
            if (digit.length() != 0) {
                all[len - 1] = digit.substring(0);
            }
        }

        startRead = 0;
        String s1;

        //开始入栈出栈
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
            saveResult[in] = peek;
            in++;
        }
        return peek;
    }

    //后缀计算法开始
    private void calculate(double v, List<Double> list, Stack numStack) {
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
                //Map.Entry<Integer, String> next = iterator.next();
                String value = saveIndex.get(0);
                startRead++;
                String[] splitValue = value.split(" ");
                int length = splitValue[1].length();
                switch (length) {
                    case 1:
                        if (splitValue[1].equals("x")) {
                            numStack.push(Math.pow(Double.parseDouble(splitValue[0]), v));
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
                //Map.Entry<Integer, String> next = iterator.next();
                String value = saveIndex.get(0);
                startRead++;
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
                // Map.Entry<Integer, String> next = iterator.next();
                String value = saveIndex.get(0);
                startRead++;
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

    //计算指数的指数的方法，比如有2^x^2这个式子，这个方法就是用来计算x^2这个部分
    private String calculateSpecialIndex(String power, String index, double v) {
        if (power.equals("x")) {
            return String.valueOf(Math.pow(Double.parseDouble(power.replace("x", v + "")), Double.parseDouble(index)));
        }
        return String.valueOf(Math.pow(Double.parseDouble(power), Double.parseDouble(index)));
    }

    //换底公式转换，java提供的Math.log方法是以e为底的
    private double calculateLog(String power, String index, double v) {
        if (power.equals("x")) {
            return Math.log(Double.parseDouble(index)) / Math.log(v);
        } else if (index.equals("x")) {
            return Math.log(v) / Math.log(Double.parseDouble(power));
        }
        return Math.log(Double.parseDouble(index)) / Math.log(Double.parseDouble(power));
    }

    //三角函数计算，传入的值v，需要是角度值，用Math.toRadians(int x)这个方法换，这个转换原理高中课本有
    private double calculateTrigonometricFunction(String triType, String param, double v) {
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