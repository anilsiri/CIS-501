package cis501.submission;

import cis501.ITraceAnalyzer;
import cis501.Uop;

public class TraceAnalyzer<T extends Uop> implements ITraceAnalyzer<T> {

    @Override
    public String author() {
        return "<your name here>";
    }

    @Override
    public void run(Iterable<T> uiter) {
        for (Uop uop : uiter) {
            // TODO: your code here
        }
    }

    @Override
    public double avgInsnSize() {
        return 0.0;
    }

    @Override
    public double fractionOfBranchTargetsLteNBits(int bits) {
        return 0;
    }

    @Override
    public double fractionOfInsnsLteNBytes(int bytes) {
        return 0;
    }

    @Override
    public String mostCommonUopCategory() {
        return null;
    }

}
