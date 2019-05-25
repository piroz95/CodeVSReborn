package ai.search.ne;

import ai.MazAI;
import ai.chainfinder.ChainFinder;
import ai.evaluator.Evaluator;
import ai.evaluator.MLEData;
import ai.evaluator.evaluation.ChainContainer;
import ai.search.DetonationTreeNode;
import ai.search.RootedTree;
import ai.search.SearchUtil;
import ai.searcher.BeamStackSearch;
import common.Parameters;
import game.Board;
import game.Field;
import game.GameUtil;
import util.Util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GlicoPredictor2<T extends ChainContainer> implements Predictor<T, GlicoNode2, GlicoNode2> {
    private final ChainFinder cf;
    private final Evaluator<T> ev;
    private final MLEData dvd;
    private final MLEData dve;

    public GlicoPredictor2(final ChainFinder cf, final Evaluator<T> ev, final Path dir, final String prefix) {
        super();
        this.cf = cf;
        this.ev = ev;
        final Path dvd = dir.resolve(prefix + "dvd.txt");
        final Path dve = dir.resolve(prefix + "dve.txt");
        this.dvd = new MLEData(dvd);
        this.dve = new MLEData(dve);
    }

    private GlicoNode2 nodeMapper(final DetonationTreeNode<T> node) {
        if (node.isRoot()) {
            return GlicoNode2.root(node);
        }
        final GlicoNode2.BoardInfo2 info = calcBoardInfo(node);
        if (node.isDetonationNode()) {
            final AttackDamage damage = AttackDamage.fromTurnResult(node.gameNode.getEdge().get().result);
            return GlicoNode2.detonation(node, info, damage);
        }
        final Board b = node.gameNode.board;
        final Board b2 = new Board(b.game, b.turn, b.field, b.garbage + 99, Math.max(0, b.skill - 34));
        final int searchDepth = 3;
        final RootedTree<DetonationTreeNode<T>> detonationTree = BeamStackSearch.detonationTree(ev, b2, searchDepth, 75);
        final Optional<RootedTree<DetonationTreeNode<T>>> bigChain = SearchUtil.findBigChain2(detonationTree, 999);
        final Optional<RootedTree<DetonationTreeNode<T>>> extension = SearchUtil.findExtension(detonationTree);

        final AttackDamage damage;
        final GlicoNode2.BoardInfo2 bigInfo;
        if (bigChain.isPresent()) {
            damage = AttackDamage.fromTurnResult(bigChain.get().get().gameNode.getEdge().get().result);
            bigInfo = calcBoardInfo(bigChain.get().get());
        } else {
            damage = AttackDamage.zero();
            bigInfo = GlicoNode2.BoardInfo2.dead();
        }

        final GlicoNode2.BoardInfo2 extendInfo;
        if (!extension.isPresent()) {
            extendInfo = GlicoNode2.BoardInfo2.dead();
        } else {
            final ArrayList<RootedTree<DetonationTreeNode<T>>> extendPath = RootedTree.prunedPath(extension.get());
            final int extendDepth = extendPath.size() - 1;
            if (searchDepth > extendDepth) {
                extendInfo = GlicoNode2.BoardInfo2.dead();
            } else {
                extendInfo = calcBoardInfo(extension.get().get());
            }
        }

        return GlicoNode2.extension(node, info, damage, bigInfo, extendInfo);
    }

    private GlicoNode2.BoardInfo2 calcBoardInfo(final DetonationTreeNode<T> nonRootAliveNode) {
        final Field f = nonRootAliveNode.gameNode.board.field;

        final int colorGarbage = f.countColorAndGarbageBlock();
        final int colorBlock = colorGarbage >> 16;
        final int garbage = colorGarbage & 0xffff;

        final long chainDamageScore;
        if (nonRootAliveNode.isExtendNode() && nonRootAliveNode.evaluation.flatMap(ChainContainer::getChain).isPresent()) {
            chainDamageScore = nonRootAliveNode.evaluation.get().getChain().get().score; //NOTE: Chainfinderがchainを見つけることに依存
        } else {
            chainDamageScore = cf.findChain(nonRootAliveNode.gameNode.board.field).score;
        }

        final long bombDamageScore = GameUtil.explosionScore(f.countBlowableBlock());

        return GlicoNode2.BoardInfo2.alive(
                colorBlock,
                garbage,
                nonRootAliveNode.gameNode.board.skill,
                f.countLitBlock(),
                f.countRevealedBlock(),
                AttackDamage.fromGarbageSent(chainDamageScore / 2, bombDamageScore / 2));
    }

    @Override
    public GlicoNode2 myNodeMapper(final DetonationTreeNode<T> node) {
        return nodeMapper(node);
    }

    @Override
    public GlicoNode2 enemyNodeMapper(final DetonationTreeNode<T> node) {
        return nodeMapper(node);
    }

    @Override
    public Optional<Double> predict(GlicoNode2 myNode, GlicoNode2 enemyNode) {
        if (!myNode.isDetonationNode() && !enemyNode.isDetonationNode()) {
            return Optional.empty();
        }

        final boolean flip;
        if (!myNode.isDetonationNode()) {
            final GlicoNode2 temp = myNode;
            myNode = enemyNode;
            enemyNode = temp;
            flip = true;
        } else {
            flip = false;
        }

        final double dot;
        final List<Double> f = myNode.feature();
        if (enemyNode.isDetonationNode()) {
            //dvd
            f.addAll(enemyNode.feature());
            dot = dvd.dot(f);
        } else {
            //dve
            if (enemyNode.isFragileExtensionNode()) {
                dot = Double.POSITIVE_INFINITY;
            } else {
                f.addAll(enemyNode.feature());
                dot = dve.dot(f);
            }
        }
        double p = Util.sigmoid(dot);
        if (flip) {
            p = 1 - p;
        }

        if (Parameters.LOG_LEVEL >= 4) {
            final Consumer<String> writer = MazAI.logger::println;
            if (!flip) {
                writer.accept(myNode.toDetailString() + " " + enemyNode.toDetailString());
            } else {
                writer.accept(enemyNode.toDetailString() + " " + myNode.toDetailString());
            }

            writer.accept((int) (p * 100) + "%");
        }

        return Optional.of(p * 2 - 1);
    }


}
