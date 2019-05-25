package ai.search.ne;

import ai.search.DetonationTreeNode;
import com.sun.istack.internal.Nullable;
import common.Parameters;
import game.Action;
import util.Pair;
import util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GlicoNode2 implements ActionContainer {
    /*
     * 欲しいデータ：
     *
     * 共通
     *   0.色ブロック
     *   1.お邪魔ブロック
     *   2.スキルゲージ
     *   3.盤面連鎖火力
     *   4.盤面爆破火力
     * 発火ノードのとき
     *   5.連鎖火力
     *   6.爆破火力
     * 伸ばしノードのとき
     *   お邪魔を+999,スキルを-34(11+連鎖を想定)して探索する
     *     Xターン以内に発火可能 (できないなら0)
     *       5.連鎖火力
     *       6.爆破火力
     *     Xターン以内の最大発火ノード
     *       7.色ブロック
     *       8.お邪魔ブロック
     *       9.スキルゲージ
     *       10.盤面連鎖火力
     *       11.盤面爆破火力
     *     Xターン目の伸ばしノード
     *       12-16
     *
     * 発火ノードが7次元、伸ばしノードが17次元
     *
     * 火力について
     *   指数的に増えるので、200以上はlog取って多項式にしたほうが良さそう
     */

    /*
     *          info futureInfo damage
     * Root     X    X          X
     * Detonate O    X          O
     * Extend   O    O          ?
     */

    private final DetonationTreeNode node;
    @Nullable
    private final BoardInfo2 info;
    @Nullable
    private final AttackDamage damage;
    @Nullable
    private final Pair<BoardInfo2, BoardInfo2> futureInfo;

    private GlicoNode2(final DetonationTreeNode node, final BoardInfo2 info, final AttackDamage damage,
                       final Pair<BoardInfo2, BoardInfo2> futureInfo) {
        super();
        this.node = node;
        this.info = info;
        this.damage = damage;
        this.futureInfo = futureInfo;
    }

    public static GlicoNode2 root(final DetonationTreeNode node) {
        return new GlicoNode2(node, null, null, null);
    }

    static GlicoNode2 detonation(final DetonationTreeNode node, final BoardInfo2 info, final AttackDamage damage) {
        return new GlicoNode2(node, info, damage, null);
    }

    static GlicoNode2 extension(final DetonationTreeNode node, final BoardInfo2 info, final AttackDamage damage, final BoardInfo2 bigInfo, final BoardInfo2 extendInfo) {
        return new GlicoNode2(node, info, damage, new Pair<>(bigInfo, extendInfo));
    }

    private static void write(final List<Double> f, final BoardInfo2 info) {
        f.add((double) info.colorBlock);
        f.add((double) info.garbageBlock);
        f.add((double) info.skill);
        f.add(info.attackDamage.chainDamageScore);
        f.add(info.attackDamage.bombDamageScore);
        final double skillScore = Util.sigmoid((info.skill - 80) * 0.5);
        f.add(skillScore);
        f.add(skillScore * info.attackDamage.bombDamageScore);
        if (Parameters.PREDICTOR_VERSION >= 31) {
            f.add((double) info.litColorBlock);
            f.add((double) info.revealedColorBlock);
        }
    }

    private static void write(final List<Double> f, final AttackDamage atk) {
        f.add(atk.chainDamageScore);
        f.add(atk.bombDamageScore);
    }

    public boolean isDetonationNode() {
        return info != null && futureInfo == null;
    }

    public List<Double> feature() {
        if (node.isRoot()) {
            throw new RuntimeException("root");
        }
        final List<Double> f = new ArrayList<>();
        write(f, info);
        write(f, damage);
        if (node.isExtendNode()) {
            write(f, futureInfo.first);
            write(f, futureInfo.second);
        }
        return f;
    }

    public String toString() {
        final AttackDamage dmg = damage == null ? AttackDamage.zero() : damage;
        return dmg.toString();
    }

    public Action getLastAction() {
        final Optional<Action> a = node.gameNode.lastAction();
        if (a.isPresent()) {
            return a.get();
        }
        throw new RuntimeException("No Action???");
    }

    public boolean isFragileExtensionNode() {
        return futureInfo != null && (futureInfo.first.isDead || futureInfo.second.isDead);
    }

    public static class BoardInfo2 {
        final boolean isDead;
        final int skill;
        final AttackDamage attackDamage;
        final int colorBlock;
        final int garbageBlock;
        final int litColorBlock; //色ブロックであって、上方向のいずれかのマスにおじゃまブロックがあるようなものの数。
        final int revealedColorBlock; //色ブロックであって、お邪魔ブロックを壁だと思って8近傍に移動したときに天井にたどり着けないものの数。

        private BoardInfo2(final int colorBlock, final int garbageBlock, final int skill, final int litColorBlock, final int revealedColorBlock, final AttackDamage attackDamage, final boolean isDead) {
            super();
            this.colorBlock = colorBlock;
            this.garbageBlock = garbageBlock;
            this.skill = skill;
            this.attackDamage = attackDamage;
            this.litColorBlock = litColorBlock;
            this.revealedColorBlock = revealedColorBlock;
            this.isDead = isDead;
        }

        static BoardInfo2 alive(final int colorBlock, final int garbageBlock, final int skill, final int shadowedColorBlock, final int buriedColorBlock, final AttackDamage attackDamage) {
            return new BoardInfo2(colorBlock, garbageBlock, skill, shadowedColorBlock, buriedColorBlock, attackDamage, false);
        }

        static BoardInfo2 dead() {
            return new BoardInfo2(0, 0, 0, 0, 0, null, true);
        }

        public String toString() {
            if (!isDead) {
                return String.format("Cl:%d Gb:%d Sk:%d Lit:%d Rev%d %s", colorBlock, garbageBlock, skill, litColorBlock, revealedColorBlock, attackDamage.toString());
            }
            return "DEAD";
        }
    }

    String toDetailString() {
        if (isDetonationNode()) {
            return "DTN(" + info.toString() + " <" + damage.toString() + ">)";
        } else {
            return "EXT(" + info.toString() + " <" + damage.toString() + "> " + futureInfo.first.toString() + " " + futureInfo.second.toString() + ")";
        }
    }
}
