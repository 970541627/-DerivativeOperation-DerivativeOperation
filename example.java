  public static void main(String[] args) {
        Derivative derivative = new Derivative();
        //表示2的x次方，加上一个 以4为底x为真数的log值 乘以x 加上一个cos（x）
        double v = derivative.parseExpression("2^x,l4(x)*x+cx,", 5);
        System.out.println(v);
    }
