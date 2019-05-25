package game;

public class BitBoardField extends Field {
    private final BitBoard b;

    public BitBoardField(final BitBoard b) {
        this.b = b;
    }

    @Override
    public int get(final int i, final int j) {
        return b.get(i, j);
    }

    @Override
    public long longHashCode() {
        return b.longHashCode();
    }

    public static class BitBoard {
        //4bitずつ1ブロックのデータを入れる。フィールドの高さが16なのでなんとかなる。
        private final long[] data = new long[GameConstants.FIELD_WIDTH];

        public BitBoard() {

        }

        int get(final int i, final int j) {
            return (int) (data[j] >>> (i << 2)) & 0xf;
        }

        public void set(final int i, final int j, final int x) {
            data[j] &= ~(0xfL << (i << 2));
            data[j] |= (long) x << (i << 2);
        }

        long longHashCode() {
            long l = 0;
            for (int i = 0; i < GameConstants.FIELD_WIDTH; i++) {
                l = l * 31 + data[i];
            }
            return l;
        }
    }
}
