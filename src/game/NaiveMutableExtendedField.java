package game;

import java.util.Arrays;

public class NaiveMutableExtendedField {
	private static final int P = GameConstants.PACK_SIZE;
	private static final int H = GameConstants.FIELD_HEIGHT;
	private static final int H2 = P + H + 2; //パック+高さ+オジャマ+床
	private static final int W = GameConstants.FIELD_WIDTH;
	private static final int W2 = W + 2; //幅+壁
	private static final int GARBAGE = GameConstants.GARBAGE_ID;
	private static final int WALL = GameConstants.GARBAGE_ID + 1;
	private static final int SUM = GameConstants.ELIMINATION_SUM;
	
	//   2
	//  X3
	//  14
	private static final int[] OFFSETS = {1,H2-1,H2,H2+1};
	private static final int[] OFFSETS_EXPLOSION = {-H2-1,-H2,-H2+1,-1,0,1,H2-1,H2,H2+1};

    private final byte[] map;
    private final boolean[] mark;
    private final boolean[] markColumn;
	
	public NaiveMutableExtendedField() {
		map = new byte[H2*W2];
		mark = new boolean[H2*W2];
		markColumn = new boolean[W2];
		for(int i=0;i<H2;i++) {
			map[field2XPos(i,0)] = WALL;
			map[field2XPos(i,W2-1)] = WALL;
		}
		for(int i=0;i<W2;i++) {
			map[field2XPos(H2-1,i)] = WALL;
		}
	}

    private static int field2XPos(final int i, final int j) {
		return j * H2 + i;
	}

    public void init(final Field f) {
		//デンジャーラインより上をきれいにする
		for(int i=0;i<P+1;i++) {
			for(int j=1;j<=W;j++) {
				map[field2XPos(i,j)] = 0;
			}
		}
		//コピー
		for(int i=0;i<H;i++) {
			for(int j=0;j<W;j++) {
				map[field2XPos(i+P+1,j+1)] = (byte) f.get(i, j);
			}
		}
	}
	
	public void putGarbage() {
		for(int j=1;j<=W;j++) {
			map[field2XPos(2, j)] = GARBAGE;
		}
	}

	public void deleteBlock(final int i, final int j) {
		map[field2XPos(i+P+1, j+1)] = 0;
	}

    public void putPack(final Pack rotatedPack, final int pos) {
		for(int i=0;i<P;i++) {
			for(int j=0;j<P;j++) {
				map[field2XPos(i, pos+1+j)] = (byte) rotatedPack.get(i, j);
			}
		}
	}

    private int simulateChain(final boolean skipFirstFall) {
		int chain = 0;
		
		if (skipFirstFall) {
			Arrays.fill(markColumn, true); //TODO: これはちょっと遅い
		}else {
			Arrays.fill(markColumn, false);
			fall();
		}
		
		while(markForEliminate()) {
			eliminate();
			chain++;
			Arrays.fill(markColumn, false);
			fall();
		}
		
		return chain;
	}
	
	public int simulateChain() {
		return simulateChain(false);
	}

	/**
	 * 落下処理を行い、列に消去判定のマークをつける。
	 * 消去処理に使う場合、先にmarkColumnを初期化すること。
	 */
	private void fall() {
		//落下
		for(int j=1;j<=W;j++) {
			int bottom = field2XPos(H2-2, j);
			int current = field2XPos(H2-2, j);
			for(int i=H2-2;i>=0;i--) {
				if (map[current] != 0) {
					if (current != bottom) {
						map[bottom] = map[current];
						map[current] = 0;
						markColumn[j] = true;
					}
					bottom--;
				}
				current--;
			}
		}
	}

	/**
	 * 先に
	 *
	 * @return 消去されるブロックが存在するならtrue
	 */
	private boolean markForEliminate() {
		boolean marked = false;
		Arrays.fill(mark, false);
		
		for(int j=1;j<=W;j++) {
			if (markColumn[j]) {
				for(int i=0;i<H2-1;i++) {
                    final int x = field2XPos(i, j);
                    final int x2 = x + OFFSETS[0];
					if (map[x] + map[x2] == SUM) {
						marked = mark[x] = mark[x2] = true;
					}
				}
			}
			if (markColumn[j] || markColumn[j+1]) {
				for(int i=0;i<H2-1;i++) {
                    final int x = field2XPos(i, j);
					for(int k=1;k<4;k++) {
                        final int x2 = x + OFFSETS[k];
						if (map[x] + map[x2] == SUM) {
							marked = mark[x] = mark[x2] = true;
						}
					}
				}
			}
		}
		
		return marked;
	}
	
	private void eliminate() {
		for(int i=0;i<H2-1;i++) {
			for(int j=1;j<=W;j++) {
                final int x = field2XPos(i, j);
				if (mark[x]) {
					map[x] = 0;
				}
			}
		}
	}

	public int explosion() {
		markForExplosion();
		int count = 0;
		for(int i = 0; i<H2-1; i++) {
			for(int j = 1; j<=W; j++) {
                final int x = field2XPos(i, j);
				if (mark[x]) {
					count++;
					map[x] = 0;
				}
			}
		}
		return count;
	}

	private void markForExplosion() {
		Arrays.fill(mark, false);
        for (int i = 1; i < H2 - 1; i++) { //爆破ブロックがデンジャーゾーンにあることはないので3からで良さそう（でも怖い）
			for (int j = 1; j <= W; j++) {
                final int x = field2XPos(i, j);
				if (map[x] == 5) {
                    for (final int value : OFFSETS_EXPLOSION) {
                        final int x2 = x + value;
                        if (!GameUtil.isColorBlock(map[x2])) continue;
						mark[x2] = true;
					}
				}
			}
		}
	}
	
	public boolean isDead() {
		for(int j=1;j<=W;j++) {
			if (map[field2XPos(2,j)] >= 1) {
				return true;
			}
		}
		return false;
	}

	public Field toField() {
        final BitBoardField.BitBoard bb = new BitBoardField.BitBoard();
		for(int i = 0; i<H; i++) {
			for(int j = 0; j<W; j++) {
				bb.set(i, j, map[field2XPos(i+P+1,j+1)]);
			}
		}
		return new BitBoardField(bb);
	}

}
