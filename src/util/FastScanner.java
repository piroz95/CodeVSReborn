package util;

import java.io.*;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

@SuppressWarnings("unused")
public class FastScanner implements AutoCloseable {
	private final InputStream in;
	private final byte[] buffer = new byte[1024];
	private int ptr = 0;
	private int buflen = 0;

	public FastScanner() { this(System.in);}

    public FastScanner(final InputStream source) {
        this.in = source;
    }

    public FastScanner(final File file) {
		try {
			this.in = new FileInputStream(file);
        } catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

    private static boolean isPrintableChar(final int c) {
        return 33 <= c && c <= 126;
    }

    private int readByte() {
        if (hasNextByte()) return buffer[ptr++];
        else return -1;
    }

    private static boolean isNewLine(final int c) {
        return c == '\n' || c == '\r';
    }

	private boolean hasNextByte() {
		if (ptr < buflen) {
			return true;
		}else{
			ptr = 0;
			try {
				buflen = in.read(buffer);
            } catch (final IOException e) {
				e.printStackTrace();
			}
			return buflen > 0;
		}
	}

	public boolean hasNext() { while(hasNextByte() && !isPrintableChar(buffer[ptr])) ptr++; return hasNextByte();}

	public boolean hasNextLine() { while(hasNextByte() && isNewLine(buffer[ptr])) ptr++; return hasNextByte();}

	public String next() {
		if (!hasNext()) {
			throw new NoSuchElementException("Expect printable character, reached EOF.");
		}
        final StringBuilder sb = new StringBuilder();
		int b = readByte();
		while(isPrintableChar(b)) {
			sb.appendCodePoint(b);
			b = readByte();
		}
		return sb.toString();
	}

    private char[] nextCharArray(final int len) {
		if (!hasNext()) {
			throw new NoSuchElementException("Expect printable character, reached EOF.");
		}
        final char[] s = new char[len];
		int i = 0;
		int b = readByte();
		while(isPrintableChar(b)) {
			if (i == len) {
				throw new InputMismatchException();
			}
			s[i++] = (char) b;
			b = readByte();
		}
		return s;
	}

	public String nextLine() {
		if (!hasNextLine()) {
			throw new NoSuchElementException("Expect printable character, reached EOF.");
		}
        final StringBuilder sb = new StringBuilder();
		int b = readByte();
		while(!isNewLine(b)) {
			sb.appendCodePoint(b);
			b = readByte();
		}
		return sb.toString();
	}

	public long nextLong() {
		if (!hasNext()) {
			throw new NoSuchElementException("Expect printable character, reached EOF.");
		}
		long n = 0;
		boolean minus = false;
		int b = readByte();
		if (b == '-') {
			minus = true;
			b = readByte();
		}
		if (b < '0' || '9' < b) {
			throw new NumberFormatException();
		}
		while(true){
			if ('0' <= b && b <= '9') {
				n *= 10;
				n += b - '0';
			}else if(b == -1 || !isPrintableChar(b)){
				return minus ? -n : n;
			}else{
				throw new NumberFormatException();
			}
			b = readByte();
		}
	}

	public int nextInt() {
        final long nl = nextLong();
		if (nl < Integer.MIN_VALUE || nl > Integer.MAX_VALUE) {
			throw new NumberFormatException();
		}
		return (int) nl;
	}

	public char nextChar() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		return (char) readByte();
	}

	public double nextDouble() { return Double.parseDouble(next());}

    private int[] nextIntArray(final int n) {
        final int[] a = new int[n];
		for (int i = 0; i < n; i++) a[i] = nextInt();
		return a;
	}

    public long[] nextLongArray(final int n) {
        final long[] a = new long[n];
        for (int i = 0; i < n; i++) a[i] = nextLong();
        return a;
    }

    public double[] nextDoubleArray(final int n) {
        final double[] a = new double[n];
        for (int i = 0; i < n; i++) a[i] = nextDouble();
        return a;
    }

    public void nextIntArrays(final int[]... a) {
        for (int i = 0; i < a[0].length; i++) for (int j = 0; j < a.length; j++) a[j][i] = nextInt();
    }

    public int[][] nextIntMatrix(final int n, final int m) {
        final int[][] a = new int[n][];
        for (int i = 0; i < n; i++) a[i] = nextIntArray(m);
        return a;
    }

    public char[][] nextCharMap(final int n, final int m) {
        final char[][] a = new char[n][];
        for (int i = 0; i < n; i++) a[i] = nextCharArray(m);
        return a;
    }

	public void close() {
		try {
			in.close();
        } catch (final IOException ignored) {
		}
	}
}

