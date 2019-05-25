# CodeVSReborn
https://codevs.jp/ 
CODE VS Rebornで決勝戦に提出したAI。

コードを覗かれてもある程度大丈夫なように不要なファイルを削った。

## ビルド・実行方法
lp_solveが必須。

Windows+Javaでのlp_solveの環境構成には
https://www.largevocalmix.jp/code/2018/03/08
が参考になった。

うまく実行可能jarを作れたら
java -jar Ma2ai.jar
みたいに実行できるはず

## オプション
実行時引数またはma2ai.iniに書く。

### brain
- brain=TSpineplus
- brain=aggro6

aggro6は最速連鎖をするだけのAI。下の多くのオプションはaggro6には効かない。

### wdir
- wdir=.

ワーキングディレクトリ。うまく動かない時は実行ファイルの絶対パスを入れるとなんとかなることが多い。

### log
- log=0

ログレベル。0で無し、1でエラー出力のみ、2でファイル、3以上のときgraphvizが必要。

### logdir
- logdir=log

ログを保存するディレクトリ。wdirの相対パス。

### thinktime
- thinktime=10000

思考時間。10000だと連鎖1回につき20秒とちょっと。

### thinktimei
- thinktimei=500

詰み探索の思考時間(ms)

### thinktimee
- thinktimee=500

敵盤面での探索時間(ms)

### depth
- depth=20

探索深さの初期値

### depthe
- depthe=4

敵盤面の探索深さ。

### threshold
- threshold=45

最低連鎖威力。

### thresholds
- thresholds=105

これ以上の連鎖が見つかったら枝刈りする。実質最大連鎖威力。

### thresholde
- thresholde=30

予測する敵の最低火力。

### async
- async=true

入力待ち中に思考するか？

### thread
- thread=1

マルチスレッド数

### random
- random=0

最初のn手をランダマイズする。

### tengen
- tengen=true

最初は"4 0"に置くか？
