package game;

import util.FastByteQueue;
import util.Util;

public abstract class Field {
	
	public abstract int get(int i, int j);
	
	public abstract long longHashCode();

	public boolean equals(final Field f) {
		for(int i=0;i<GameConstants.FIELD_HEIGHT;i++) {
			for(int j=0;j<GameConstants.FIELD_WIDTH;j++) {
				if (get(i,j) != f.get(i, j)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * @return colorBlock << 16 | garbageBlock
	 */
	public int countColorAndGarbageBlock() {
		int color = 0;
		int garbage = 0;
		for(int j=0;j<GameConstants.FIELD_WIDTH;j++) {
			for(int i=GameConstants.FIELD_HEIGHT-1;i>=0;i--) {
				final int b = get(i, j);
				if (b == 0) break;
				if (b == GameConstants.GARBAGE_ID) {
					garbage++;
				}else {
					color++;
				}
			}
		}
		return (color << 16) | garbage;
	}
	
	public int countBlowableBlock() {
		int nextToFive = 0;
		for(int j=0;j<GameConstants.FIELD_WIDTH;j++) {
			for(int i=GameConstants.FIELD_HEIGHT-1;i>=0;i--) {
				final int b = get(i, j);
				if (b == 0) break;
				LOOP: for(int ni=i-1;ni<=i+1;ni++) {
					for(int nj=j-1;nj<=j+1;nj++) {
						if (ni == 0 && nj == 0) continue;
						if (!GameUtil.isInsideField(ni, nj)) continue;
						if (get(ni, nj) == 5) {
							nextToFive++;
							break LOOP;
						}
					}
				}
			}
		}
		return nextToFive;
	}

    public int countLitBlock() {
        int count = 0;
        for (int j = 0; j < GameConstants.FIELD_WIDTH; j++) {
            for (int i = 0; i < GameConstants.FIELD_HEIGHT; i++) {
				final int b = get(i, j);
                if (b == GameConstants.GARBAGE_ID) {
                    break;
                }
                if (GameUtil.isColorBlock(b)) {
                    count++;
                }
            }
        }
        return count;
    }

    //NOTE: はばたんするから定数倍重い
    public int countRevealedBlock() {
        final int g = GameConstants.GARBAGE_ID;
        final int h = GameConstants.FIELD_HEIGHT;
        final int w = GameConstants.FIELD_WIDTH;
        final boolean[] used = new boolean[h * w];
        final FastByteQueue q = new FastByteQueue(h * w);
        for (int j = 0; j < GameConstants.FIELD_WIDTH; j++) {
            if (get(0, j) != g) {
                q.offer((byte) j);
                used[j] = true;
            }
        }
        int count = 0;
        while (!q.isEmpty()) {
			final int x = Byte.toUnsignedInt(q.poll());
            final int i = x / w;
            final int j = x % w;
			if (GameUtil.isColorBlock(get(i, j))) {
				count++;
			}
            for (int di = -1; di <= 1; di++) {
                for (int dj = -1; dj <= 1; dj++) {
                    if (di == 0 && dj == 0) continue;
					final int ni = i + di;
					final int nj = j + dj;
					final int nx = ni * w + nj;
                    if (GameUtil.isInsideField(ni, nj) && get(ni, nj) != g && !used[nx]) {
                        q.offer((byte) nx);
                        used[nx] = true;
                    }
                }
            }
        }
        return count;
    }

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for(int i=0;i<GameConstants.FIELD_HEIGHT;i++) {
			for(int j=0;j<GameConstants.FIELD_WIDTH;j++) {
				sb.append(Util.blockToChar(get(i,j)));
			}
			sb.append('\n');
		}
		return sb.toString();
	}
}
