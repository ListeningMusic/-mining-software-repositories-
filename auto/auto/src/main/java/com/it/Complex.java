package com.it;

/*
* 定义复数，以及一些复数的加减乘除等操作
*
*/

public class Complex {
	private final double re; // 实部
	private final double im; // 虚部

	// 使用给定的实部和虚部创建一个新对象
	public Complex(double real, double imag) {
		re = real;
		im = imag;
	}

	// 返回调用Complex对象的字符串表示形式
	public String toString() {
		if (im == 0)
			return re + "";
		if (re == 0)
			return im + "i";
		if (im < 0)
			return re + " - " + (-im) + "i";
		return re + " + " + im + "i";
	}

	// 返回绝对值/模数/幅度和角度/相位/参数
	public double abs() {
		return Math.hypot(re, im);
	} // Math.sqrt(re*re + im*im)

	public double phase() {
		return Math.atan2(im, re);
	} // between -pi and pi

	// return a new Complex object whose value is (this + b)
	public Complex plus(Complex b) {
		Complex a = this; // invoking object
		double real = a.re + b.re;
		double imag = a.im + b.im;
		return new Complex(real, imag);
	}

	// return a new Complex object whose value is (this - b)
	public Complex minus(Complex b) {
		Complex a = this;
		double real = a.re - b.re;
		double imag = a.im - b.im;
		return new Complex(real, imag);
	}

	// return a new Complex object whose value is (this * b)
	public Complex times(Complex b) {
		Complex a = this;
		double real = a.re * b.re - a.im * b.im;
		double imag = a.re * b.im + a.im * b.re;
		return new Complex(real, imag);
	}

	// scalar multiplication
	// return a new object whose value is (this * alpha)
	public Complex times(double alpha) {
		return new Complex(alpha * re, alpha * im);
	}

	// return a new Complex object whose value is the conjugate of this
	public Complex conjugate() {
		return new Complex(re, -im);
	}//共轭

	// return a new Complex object whose value is the reciprocal of this
	//倒数
	public Complex reciprocal() {
		double scale = re * re + im * im;
		return new Complex(re / scale, -im / scale);
	}

	// return the real or imaginary part
	public double re() {
		return re;
	}

	public double im() {
		return im;
	}

	// return a / b
	public Complex divides(Complex b) {
		Complex a = this;
		return a.times(b.reciprocal());
	}

	// return a new Complex object whose value is the complex exponential of
	// this
	public Complex exp() {
		return new Complex(Math.exp(re) * Math.cos(im), Math.exp(re)
				* Math.sin(im));
	}

	// return a new Complex object whose value is the complex sine of this
	public Complex sin() {
		return new Complex(Math.sin(re) * Math.cosh(im), Math.cos(re)
				* Math.sinh(im));
	}

	// return a new Complex object whose value is the complex cosine of this
	public Complex cos() {
		return new Complex(Math.cos(re) * Math.cosh(im), -Math.sin(re)
				* Math.sinh(im));
	}

	// tan 正切
	public Complex tan() {
		return sin().divides(cos());
	}

	// 复数相加，实部相加虚部相加
	public static Complex plus(Complex a, Complex b) {
		double real = a.re + b.re;
		double imag = a.im + b.im;
		Complex sum = new Complex(real, imag);
		return sum;
	}
}