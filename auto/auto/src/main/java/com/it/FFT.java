package com.it;

/*
* 定义FFT（快速傅里叶转换）操作，利用递归方法
*
*/

public class FFT {

	// compute the FFT of x[], assuming its length is a power of 2
    //建立一个数组，数组长度是2的整数幂，方便二分
	public static Complex[] fft(Complex[] x) {
		int N = x.length;

		// base case
		if (N == 1)
			return new Complex[] { x[0] };

		// radix 2 Cooley-Tukey FFT
		if (N % 2 != 0) {
			throw new RuntimeException("N is not a power of 2");
		}

		// 偶数项
		Complex[] even = new Complex[N / 2];
		for (int k = 0; k < N / 2; k++) {
			even[k] = x[2 * k];
		}
		Complex[] q = fft(even);

		// 奇数项
		Complex[] odd = even; // reuse the array 为了节省空间
		for (int k = 0; k < N / 2; k++) {
			odd[k] = x[2 * k + 1];
		}
		Complex[] r = fft(odd);

		// combine，底层FFT操作
		Complex[] y = new Complex[N];
		for (int k = 0; k < N / 2; k++) {

			// 使用欧拉公式e^(-i*2pi*k/N) = cos(-2pi*k/N) + i*sin(-2pi*k/N)
			double kth = -2 * k * Math.PI / N;
			Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
			y[k] = q[k].plus(wk.times(r[k]));
			y[k + N / 2] = q[k].minus(wk.times(r[k]));
		}
		return y;
	}
}