package util;

/**
 * 幅探索用のキュー。
 * ボクシングと容量を増やさないのとメモリ節約で速い（と思う）
 * あとCircular bufferもしない（面倒だから）
 */
public class FastByteQueue {
    private int head; //次に出る要素のインデックス
    private int tail; //次の要素が入るインデックス
    private final byte[] a;

    public FastByteQueue(final int capacity) {
        head = 0;
        tail = 0;
        a = new byte[capacity];
    }

    public byte poll() {
        return a[head++];
    }

    public void offer(final byte value) {
        a[tail++] = value;
    }

    private int size() {
        return tail - head;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
