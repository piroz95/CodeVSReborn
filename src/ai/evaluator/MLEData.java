package ai.evaluator;

import util.FastScanner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//MLEvaluator用のパラメータを格納するクラス
//TrainingResultとほぼ同じなんだけど
//TODO: 最初の次元を定数としてるけど大丈夫？
public class MLEData {
    private final double[] w;
    private final double[] sigma;
    private final double[] m;
    private final double sigmay;
    private final double my;
    private final double constant;
	public final int d;

    public MLEData(final Path path) {
        final FastScanner sc = new FastScanner(path.toFile());
		this.my = sc.nextDouble();
		this.sigmay = sc.nextDouble();
        final ArrayList<Double> wList = new ArrayList<>();
        final ArrayList<Double> mList = new ArrayList<>();
        final ArrayList<Double> sigmaList = new ArrayList<>();
		while(sc.hasNextLine()) {
            final String line = sc.nextLine().trim();
            final String[] s = line.split("\\s");
			wList.add(Double.parseDouble(s[0]));
			mList.add(Double.parseDouble(s[1]));
			sigmaList.add(Double.parseDouble(s[2]));
		}
		this.d = wList.size() - 1;
		this.constant = wList.get(0);
        final double[] w = new double[d];
        final double[] m = new double[d];
        final double[] sigma = new double[d];
		for(int i=0;i<d;i++) {
			w[i] = wList.get(i+1);
			m[i] = mList.get(i+1);
			sigma[i] = sigmaList.get(i+1);
		}
		this.w = w;
		this.m = m;
		this.sigma = sigma;
		sc.close();
	}

    public double dot(final double[] f) {
		double sum = constant;
		for(int i=0;i<d;i++) {
			sum += (f[i] - m[i]) / sigma[i] * w[i];
		}
        return my + sum * sigmay;
	}

    public double dot(final List<Double> f) {
		double sum = constant;
		for(int i=0;i<d;i++) {
			sum += (f.get(i) - m[i]) / sigma[i] * w[i];
		}
        return my + sum * sigmay;
	}
}
