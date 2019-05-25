package ai.chainfinder;

import ai.MazAI;
import game.Field;
import game.GameConstants;
import game.GameUtil;
import game.NaiveMutableExtendedField;

public class DeleteChainFinder extends ChainFinder {
    private final int d;
    private final int h;

    public DeleteChainFinder(final int extraDepth, final int h) {
        this.d = extraDepth + 1;
        this.h = h;
    }
    public DeleteChainFinder(final int extraDepth) {
        this(extraDepth, 3);
	}
	
	/*
	 * 外周であって上にブロックが乗っているようなブロックを破壊する。
	 * ブロックA,Bが存在し、Cに空きがあるとき、Aを破壊してみる。
	 * CBC
	 * CAC
	 * CXC
	 * 塔のようなところの高い部分は試さないように、countで制限をかけてる。
	 */
	@Override
    public Chain findChain(final Field f) {
        final NaiveMutableExtendedField b = MazAI.mutableBoard.get();
		int maxChain = 0;
        int maxI = GameConstants.FIELD_HEIGHT - 1;
        int maxJ = GameConstants.FIELD_WIDTH / 2;
		for(int j=0;j<GameConstants.FIELD_WIDTH;j++) {
			int count = 0;
			for(int i=GameConstants.FIELD_HEIGHT-1;i>=1;i--) {
				if (f.get(i-1, j) == 0) continue;
				boolean ok = false;
				LOOP: for(int ni=i-d;ni<=i+1;ni++) {
					for(int nj=j-1;nj<=j+1;nj+=2) {
						if (!GameUtil.isInsideField(ni, nj)) continue;
						if (f.get(ni, nj) == 0) {
							ok = true;
							break LOOP;
						}
					}
				}
				if (!ok) continue;
				count++;
                if (count > h) break;
				if (!GameUtil.isColorBlock(f.get(i, j))) continue;
				b.init(f);
				b.deleteBlock(i, j);
                final int c = b.simulateChain();
				if (maxChain < c) {
					maxChain = c;
					maxI = i;
					maxJ = j;
				}
			}
		}
		return new Chain(GameConstants.CHAIN_SCORE_TABLE[maxChain], maxI, maxJ);
	}
}
