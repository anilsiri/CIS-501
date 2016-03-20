package cis501.submission;

import cis501.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class BranchPredFullTest {

    private static IUopFactory uopF = new cis501.submission.UopFactory();

    @RunWith(Parameterized.class)
    public static class BTBTests {

        private final int INDEX_BITS;
        private final String BTB_MSG;
        private final IBranchTargetBuffer btb;

        @Rule
        public final Timeout globalTimeout = Timeout.seconds(2);

        public BTBTests(int idxBits) {
            INDEX_BITS = idxBits;
            BTB_MSG = "[index bits = " + INDEX_BITS + "]";
            btb = new cis501.submission.BranchTargetBuffer(idxBits);
        }

        /** The btb sizes (in index bits) to test. */
        @Parameterized.Parameters
        public static Collection indexBits() {
            // returns a Collection of Object[], each Object[] holds ctor arguments
            return Arrays.asList(new Object[][]{{2}, {4}, {8}});
        }

        //@Points(0.5)
        @Test
        public void testInitialState() {
            assertEquals(BTB_MSG, 0, btb.predict(0));
        }

        //@Points(1)
        @Test
        public void testNewTarget() {
            btb.train(0, 42);
            assertEquals(BTB_MSG, 42, btb.predict(0));
        }

        //@Points(1)
        @Test
        public void testAlias() {
            btb.train(0, 42);
            assertEquals(BTB_MSG, 42, btb.predict(0));
            long alias0 = (long) Math.pow(2, INDEX_BITS);
            btb.train(alias0, 100);
            assertEquals(BTB_MSG, 0, btb.predict(0)); // tag doesn't match
            assertEquals(BTB_MSG, 100, btb.predict(alias0)); // tag match
        }

        //@Points(1)
        @Test
        public void testUnalias() {
            btb.train(0, 42);
            assertEquals(BTB_MSG, 42, btb.predict(0));

            // only one entry should be set to 42
            for (int i = 1; i < (long) Math.pow(2, INDEX_BITS); i++) {
                assertEquals(BTB_MSG, 0, btb.predict(i));
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class BimodalTests {

        private final int INDEX_BITS;
        private final String IB_MSG;
        private final IDirectionPredictor bp;

        @Rule
        public final Timeout globalTimeout = Timeout.seconds(2);

        public BimodalTests(int idxBits) {
            INDEX_BITS = idxBits;
            IB_MSG = "[index bits = " + INDEX_BITS + "]";
            bp = new cis501.submission.DirPredBimodal(idxBits);
        }

        /** The bimodal predictor sizes (in index bits) to test. */
        @Parameterized.Parameters
        public static Collection indexBits() {
            // returns a Collection of Object[], each Object[] holds ctor arguments
            return Arrays.asList(new Object[][]{{2}, {4}, {8}});
        }

        //@Points(0.5)
        @Test
        public void testInitialState() {
            assertEquals(IB_MSG, Direction.NotTaken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testTaken() {
            bp.train(0, Direction.Taken);
            assertEquals(IB_MSG, Direction.NotTaken, bp.predict(0));
            bp.train(0, Direction.Taken);
            assertEquals(IB_MSG, Direction.Taken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testTakenSaturation() {
            for (int i = 0; i < 10; i++) {
                bp.train(0, Direction.Taken);
            }
            bp.train(0, Direction.NotTaken);
            bp.train(0, Direction.NotTaken);
            assertEquals(IB_MSG, Direction.NotTaken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testNotTakenSaturation() {
            for (int i = 0; i < 10; i++) {
                bp.train(0, Direction.NotTaken);
            }
            bp.train(0, Direction.Taken);
            bp.train(0, Direction.Taken);
            assertEquals(IB_MSG, Direction.Taken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testAlias() {
            bp.train(0, Direction.Taken);
            assertEquals(IB_MSG, Direction.NotTaken, bp.predict(0));
            bp.train((long) Math.pow(2, INDEX_BITS), Direction.Taken);
            assertEquals(IB_MSG, Direction.Taken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testUnalias() {
            bp.train(0, Direction.Taken);
            bp.train(0, Direction.Taken);
            assertEquals(IB_MSG, Direction.Taken, bp.predict(0));

            // only one counter should be set to t
            for (int i = 1; i < (long) Math.pow(2, INDEX_BITS); i++) {
                assertEquals(IB_MSG, Direction.NotTaken, bp.predict(i));
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class GshareNoHistoryTests {

        private final int INDEX_BITS;
        private final int HIST_BITS = 0;
        private final String GS_MSG;
        private final IDirectionPredictor bp;

        @Rule
        public final Timeout globalTimeout = Timeout.seconds(2);

        public GshareNoHistoryTests(int idxBits) {
            INDEX_BITS = idxBits;
            GS_MSG = "[index bits = " + INDEX_BITS + ", history bits = " + HIST_BITS + "]";
            bp = new cis501.submission.DirPredGshare(INDEX_BITS, HIST_BITS);
        }

        /** The gshare predictor sizes to test. */
        @Parameterized.Parameters
        public static Collection indexBits() {
            // returns a Collection of Object[], each Object[] holds ctor arguments
            return Arrays.asList(new Object[][]{{2}, {4}, {8}});
        }

        //@Points(0.5)
        @Test
        public void testInitialState() {
            assertEquals(GS_MSG, Direction.NotTaken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testTaken() {
            bp.train(0, Direction.Taken);
            assertEquals(GS_MSG, Direction.NotTaken, bp.predict(0));
            bp.train(0, Direction.Taken);
            assertEquals(GS_MSG, Direction.Taken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testTakenSaturation() {
            for (int i = 0; i < 10; i++) {
                bp.train(0, Direction.Taken);
            }
            bp.train(0, Direction.NotTaken);
            bp.train(0, Direction.NotTaken);
            assertEquals(GS_MSG, Direction.NotTaken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testNotTakenSaturation() {
            for (int i = 0; i < 10; i++) {
                bp.train(0, Direction.NotTaken);
            }
            bp.train(0, Direction.Taken);
            bp.train(0, Direction.Taken);
            assertEquals(GS_MSG, Direction.Taken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testAlias() {
            bp.train(0, Direction.Taken);
            assertEquals(GS_MSG, Direction.NotTaken, bp.predict(0));
            bp.train((long) Math.pow(2, INDEX_BITS), Direction.Taken);
            assertEquals(GS_MSG, Direction.Taken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testUnalias() {
            bp.train(0, Direction.Taken);
            bp.train(0, Direction.Taken);
            assertEquals(GS_MSG, Direction.Taken, bp.predict(0));

            // only one counter should be set to t
            for (int i = 1; i < (int) Math.pow(2, INDEX_BITS); i++) {
                assertEquals(GS_MSG, Direction.NotTaken, bp.predict(i));
            }
        }

    }

    @RunWith(Parameterized.class)
    public static class GshareHistoryTests {

        @Rule
        public final Timeout globalTimeout = Timeout.seconds(2);

        private final int INDEX_BITS;
        private final int HIST_BITS;
        private final int HIST_BITS_MASK;
        private final String GS_MSG;

        private final IDirectionPredictor bp;

        public GshareHistoryTests(int idxBits, int histBits) {
            INDEX_BITS = idxBits;
            HIST_BITS = histBits;
            HIST_BITS_MASK = (1 << histBits) - 1;
            GS_MSG = "[index bits = " + INDEX_BITS + ", history bits = " + HIST_BITS + "]";
            bp = new cis501.submission.DirPredGshare(INDEX_BITS, HIST_BITS);
        }

        /** The gshare predictor and history sizes to test. */
        @Parameterized.Parameters
        public static Collection indexHistoryBits() {
            // returns a Collection of Object[], each Object[] holds ctor arguments
            return Arrays.asList(new Object[][]{{2, 2}, {4, 2}, {4, 3}, {4, 4}, {8, 2}, {8, 3}});
        }

        //@Points(0.5)
        @Test
        public void testInitialState() {
            assertEquals(GS_MSG, Direction.NotTaken, bp.predict(0));
        }

        private int history = 0;

        /**
         * Train the counter at index i with direction dir. Takes history into account to synthesize
         * an address that will map to i.
         */
        private void trainIndex(int i, Direction dir) {
            bp.train(i ^ history, dir);
            history <<= 1;
            if (Direction.Taken == dir) {
                history |= 1;
            }
            history &= HIST_BITS_MASK;
        }

        private Direction predictIndex(int i) {
            return bp.predict(i ^ history);
        }

        //@Points(1)
        @Test
        public void testTaken() {
            trainIndex(0, Direction.Taken);
            trainIndex(0, Direction.Taken);
            assertEquals(GS_MSG, predictIndex(0), Direction.Taken);
        }

        //@Points(1)
        @Test
        public void testTakenSaturation() {
            for (int i = 0; i < 10; i++) {
                trainIndex(0, Direction.Taken);
            }
            trainIndex(0, Direction.NotTaken);
            trainIndex(0, Direction.NotTaken);
            assertEquals(GS_MSG, Direction.NotTaken, predictIndex(0));
        }

        //@Points(1)
        @Test
        public void testNotTakenSaturation() {
            for (int i = 0; i < 10; i++) {
                trainIndex(0, Direction.NotTaken);
            }
            trainIndex(0, Direction.Taken);
            trainIndex(0, Direction.Taken);
            assertEquals(GS_MSG, Direction.Taken, predictIndex(0));
        }

        //@Points(1)
        @Test
        public void testAlias() {
            trainIndex(0, Direction.Taken);
            assertEquals(GS_MSG, Direction.NotTaken, predictIndex(0));
            trainIndex((int) Math.pow(2, INDEX_BITS), Direction.Taken);
            assertEquals(GS_MSG, Direction.Taken, predictIndex(0));
        }

        //@Points(1)
        @Test
        public void testUnalias() {
            trainIndex(0, Direction.Taken);
            trainIndex(0, Direction.Taken);
            assertEquals(GS_MSG, Direction.Taken, predictIndex(0));

            // only one counter should be set to t
            for (int i = 1; i < (int) Math.pow(2, INDEX_BITS); i++) {
                assertEquals(GS_MSG, Direction.NotTaken, predictIndex(i));
            }
        }

    }

    public static class TournamentBimodalTests {

        @Rule
        public final Timeout globalTimeout = Timeout.seconds(2);

        private static IDirectionPredictor bmNT;
        private static IDirectionPredictor bmT;
        private static IDirectionPredictor tournament;

        @Before
        public void setup() {
            final int indexBits = 2;
            bmNT = new cis501.submission.DirPredBimodal(indexBits);
            bmT = new cis501.submission.DirPredBimodal(indexBits);
            tournament = new cis501.submission.DirPredTournament(indexBits, bmNT, bmT);
        }

        /** Ensure that both sub-predictors get trained. */
        //@Points(1)
        @Test
        public void testTrainBoth() {
            tournament.train(0, Direction.Taken);
            tournament.train(0, Direction.Taken);

            assertEquals(Direction.Taken, tournament.predict(0));
            assertEquals(Direction.Taken, bmNT.predict(0));
            assertEquals(Direction.Taken, bmT.predict(0));
        }

        /** Ensure that chooser doesn't get trained when sub-predictors agree. */
        //@Points(1)
        @Test
        public void testChooserUnchangedWhenSubpredictorsAgree() {
            // moves both bimodals to t, chooser is still at N
            tournament.train(0, Direction.Taken);
            tournament.train(0, Direction.Taken);

            bmNT.train(0, Direction.NotTaken); // moves bmNT to n
            assertEquals(Direction.NotTaken, tournament.predict(0)); // should use bmNT
        }

    }

    @RunWith(Parameterized.class)
    public static class TournamentAlwaysNeverTests {

        @Rule
        public final Timeout globalTimeout = Timeout.seconds(2);

        private final int CHOOSER_INDEX_BITS;
        private final String MSG;

        /**
         * selects NT when chooser is NT, and T when chooser is T. Turns the Tournament predictor
         * into a bimodal predictor.
         */
        private final IDirectionPredictor bp;

        public TournamentAlwaysNeverTests(int chooserIndexBits) {
            CHOOSER_INDEX_BITS = chooserIndexBits;
            MSG = "[chooser index bits = " + CHOOSER_INDEX_BITS + "]";
            IDirectionPredictor never = new cis501.submission.DirPredNeverTaken();
            IDirectionPredictor always = new cis501.submission.DirPredAlwaysTaken();
            bp = new cis501.submission.DirPredTournament(CHOOSER_INDEX_BITS, never/*NT*/, always/*T*/);
        }

        /** The chooser table sizes to test. */
        @Parameterized.Parameters
        public static Collection indexBits() {
            // returns a Collection of Object[], each Object[] holds ctor arguments
            return Arrays.asList(new Object[][]{{2}, {4}, {8}});
        }

        //@Points(1)
        @Test
        public void testInitialState() {
            assertEquals(MSG, Direction.NotTaken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testTaken() {
            bp.train(0, Direction.Taken);
            assertEquals(MSG, Direction.NotTaken, bp.predict(0));
            bp.train(0, Direction.Taken);
            assertEquals(MSG, Direction.Taken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testTakenSaturation() {
            for (int i = 0; i < 10; i++) {
                bp.train(0, Direction.Taken);
            }
            bp.train(0, Direction.NotTaken);
            bp.train(0, Direction.NotTaken);
            assertEquals(MSG, Direction.NotTaken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testNotTakenSaturation() {
            for (int i = 0; i < 10; i++) {
                bp.train(0, Direction.NotTaken);
            }
            bp.train(0, Direction.Taken);
            bp.train(0, Direction.Taken);
            assertEquals(MSG, Direction.Taken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testAlias() {
            bp.train(0, Direction.Taken);
            assertEquals(MSG, Direction.NotTaken, bp.predict(0));
            bp.train((long) Math.pow(2, CHOOSER_INDEX_BITS), Direction.Taken);
            assertEquals(MSG, Direction.Taken, bp.predict(0));
        }

        //@Points(1)
        @Test
        public void testUnalias() {
            bp.train(0, Direction.Taken);
            bp.train(0, Direction.Taken);
            assertEquals(MSG, Direction.Taken, bp.predict(0));

            // only one counter should be set to t
            for (int i = 1; i < (long) Math.pow(2, CHOOSER_INDEX_BITS); i++) {
                assertEquals(MSG, Direction.NotTaken, bp.predict(i));
            }
        }

    }


    private static final int BMISPRED_LAT = 2;

    private static Uop makeBr(long pc, Direction dir, long fallthruPC, long targetPC) {
        return uopF.create(1, 2, 3, null, 1, pc,
                Flags.IgnoreFlags, dir, 0, 0,
                fallthruPC, targetPC, "", "");
    }

    private static Uop makeOp(int src1, int src2, int dst, MemoryOp mop, long pc) {
        return uopF.create(src1, src2, dst, mop, 1, pc,
                Flags.IgnoreFlags, null, 0, 0,
                pc + 1, 0, "", "");
    }

    @RunWith(Parameterized.class)
    public static class PipelineIntegrationTests {

        @Rule
        public final Timeout globalTimeout = Timeout.seconds(2);

        private final int ADDL_MEM_LAT;
        private final String MSG;

        private final IInorderPipeline<Uop> pipe;
        /** Keep btb accessible to tests, if they need to fiddle with its state. */
        private final IBranchTargetBuffer btb;

        public PipelineIntegrationTests(int memLat) {
            ADDL_MEM_LAT = memLat;
            MSG = "[mem lat = " + ADDL_MEM_LAT + "]";
            IDirectionPredictor never = new cis501.submission.DirPredNeverTaken();
            btb = new cis501.submission.BranchTargetBuffer(3);
            pipe = new cis501.submission.InorderPipeline<>(ADDL_MEM_LAT, new BranchPredictor(never, btb));
        }

        /** The memory latencies to test. */
        @Parameterized.Parameters
        public static Collection memLat() {
            // returns a Collection of Object[], each Object[] holds ctor arguments
            return Arrays.asList(new Object[][]{{0}, {1}, {2}});
        }

        //@Points(2)
        @Test
        public void testCorrectPred() {
            List<Uop> uops = new LinkedList<>();
            uops.add(makeBr(0, Direction.NotTaken, 1, 40));
            uops.add(makeBr(1, Direction.NotTaken, 2, 40));
            pipe.run(uops);

            assertEquals(MSG, 2, pipe.getInsns());
            // 123456789
            // fdxmw |
            //  fdxmw|
            assertEquals(MSG, 7, pipe.getCycles());
        }

        //@Points(2)
        @Test
        public void testMispredicted() {
            List<Uop> uops = new LinkedList<>();
            uops.add(makeBr(0, Direction.Taken, 1, 40));  // mispredicted
            uops.add(makeBr(40, Direction.NotTaken, 41, 60));
            pipe.run(uops);

            assertEquals(MSG, 2, pipe.getInsns());
            // 123456789
            // fdxmw   |
            //  ..fdxmw|
            assertEquals(MSG, 7 + BMISPRED_LAT, pipe.getCycles());
        }

        //@Points(2)
        @Test
        public void test2Mispredicted() {
            List<Uop> uops = new LinkedList<>();
            uops.add(makeBr(0, Direction.Taken, 1, 40));  // mispredicted
            uops.add(makeBr(40, Direction.Taken, 41, 60));  // mispredicted
            uops.add(makeBr(60, Direction.NotTaken, 61, 80));
            pipe.run(uops);

            assertEquals(MSG, 3, pipe.getInsns());
            // 123456789abcd
            // fdxmw      |
            //  ..fdxmw   |
            //   ....fdxmw|
            assertEquals(MSG, 8 + (BMISPRED_LAT * 2), pipe.getCycles());
        }

        //@Points(2)
        @Test
        public void testMemOp() {
            List<Uop> uops = new LinkedList<>();
            uops.add(makeBr(0, Direction.Taken, 1, 40));  // mispredicted
            uops.add(makeOp(1, 2, 3, MemoryOp.Load, 40));
            pipe.run(uops);

            assertEquals(MSG, 2, pipe.getInsns());
            // 123456789abc
            // fdxmw      |
            //  ..fdxmmmmw|
            assertEquals(MSG, 7 + BMISPRED_LAT + ADDL_MEM_LAT, pipe.getCycles());
        }

        //@Points(2)
        @Test
        public void testBranchMem() {
            List<Uop> uops = new LinkedList<>();
            uops.add(makeBr(0, Direction.Taken, 1, 40)); // mispredicted
            uops.add(makeOp(1, 2, 3, null, 40));
            uops.add(makeOp(4, 5, 6, MemoryOp.Load, 41));
            pipe.run(uops);

            assertEquals(MSG, 3, pipe.getInsns());
            // 123456789abcd
            // fdxmw       |
            //  ..fdxmw    |
            //     fdxmmmmw|
            assertEquals(MSG, 8 + BMISPRED_LAT + ADDL_MEM_LAT, pipe.getCycles());
        }

        //@Points(2)
        @Test
        public void testMemBranch() {
            List<Uop> uops = new LinkedList<>();
            uops.add(makeOp(1, 2, 3, MemoryOp.Load, 40));
            uops.add(makeBr(41, Direction.Taken, 42, 50)); // mispredicted
            uops.add(makeOp(4, 5, 6, null, 50));
            pipe.run(uops);

            assertEquals(MSG, 3, pipe.getInsns());
            // 123456789abcd aml=0, mispred is exposed
            // fdxmw    |
            //  fdxmw   |
            //   ..fdxmw|
            // 123456789abcd aml=2, mispred hides in shadow of miss
            // fdxmmmw  |
            //  fdx  mw |
            //   ..fdxmw|
            assertEquals(MSG, 8 + Math.max(BMISPRED_LAT, ADDL_MEM_LAT), pipe.getCycles());
        }

        //@Points(2)
        @Test
        public void testLoadUse() {
            List<Uop> uops = new LinkedList<>();
            uops.add(makeBr(0, Direction.Taken, 1, 40));  // mispredicted
            uops.add(makeOp(1, 2, 3, MemoryOp.Load, 40));
            uops.add(makeOp(3, 4, 5, null, 41)); // load-use
            pipe.run(uops);

            assertEquals(MSG, 3, pipe.getInsns());
            // 123456789abcdef
            // fdxmw        |
            //  ..fdxmmmmw  |
            //     fd    xmw|
            assertEquals(MSG, 8 + BMISPRED_LAT + ADDL_MEM_LAT + 1/*lu*/, pipe.getCycles());
        }
    }

    public static class PipelineIntegrationTestsBtb {

        @Rule
        public final Timeout globalTimeout = Timeout.seconds(2);

        private final IInorderPipeline<Uop> pipe;
        /** Keep btb accessible to tests, if they need to fiddle with its state. */
        private final IBranchTargetBuffer btb;

        public PipelineIntegrationTestsBtb() {
            final IDirectionPredictor always;
            always = new cis501.submission.DirPredAlwaysTaken();
            btb = new cis501.submission.BranchTargetBuffer(3);
            pipe = new cis501.submission.InorderPipeline<>(0,
                    new BranchPredictor(always, btb));
        }

        //@Points(1)
        @Test
        public void testTrainBtb() {
            List<Uop> uops = new LinkedList<>();
            uops.add(makeBr(4, Direction.Taken, 5, 3)); // mispredicted b/c BTB is empty
            uops.add(makeOp(1, 2, 3, null, 3));
            uops.add(makeBr(4, Direction.Taken, 5, 3)); // predicted correctly this time
            uops.add(makeOp(1, 2, 3, null, 3));
            pipe.run(uops);

            assertEquals(4, pipe.getInsns());
            // 123456789ab
            // fdxmw     | mispred
            //  ..fdxmw  |
            //     fdxmw |
            //      fdxmw|
            assertEquals(6 + 3 + BMISPRED_LAT, pipe.getCycles());
        }

    }

    @RunWith(Parameterized.class)
    public static class TraceTests {

        @Rule
        public final Timeout globalTimeout = Timeout.seconds(2);

        private final int ADDL_MEM_LAT;
        private final double expectedCycles;
        private final String MSG;

        private IInorderPipeline<Uop> subm;

        public TraceTests(int memLat, BranchPredictor bp, String bpDesc, double expCycles) throws Exception {
            ADDL_MEM_LAT = memLat;
            MSG = "[mem lat = " + ADDL_MEM_LAT + " bpred = " + bpDesc + "]";
            expectedCycles = expCycles;

            final String TRACE_FILE = "path/to/go-10M.trace.gz";
            final int UOP_LIMIT = -1;

            cis501.UopIterator submi = new cis501.UopIterator(TRACE_FILE, UOP_LIMIT,
                    new cis501.submission.UopFactory());
            subm = new cis501.submission.InorderPipeline<>(ADDL_MEM_LAT, bp);
            System.out.println("Running submission from " + Arrays.toString(subm.groupMembers()));
            subm.run(submi);
        }

        /** The (memory latency,predictor) pairs to test. */
        @Parameterized.Parameters
        public static Collection params() {
            // returns a Collection of Object[], each Object[] holds ctor arguments
            IDirectionPredictor dp;
            IBranchTargetBuffer btb;

            dp = new cis501.submission.DirPredBimodal(10);
            btb = new cis501.submission.BranchTargetBuffer(10);
            BranchPredictor bimodal = new BranchPredictor(dp, btb);
            String bimodalDesc = "bimodal10i_btb10i";

            dp = new cis501.submission.DirPredGshare(10, 5);
            btb = new cis501.submission.BranchTargetBuffer(10);
            BranchPredictor gshare = new BranchPredictor(dp, btb);
            String gshareDesc = "gshare10i5h_btb10i";

            dp = new cis501.submission.DirPredTournament(8,
                    new cis501.submission.DirPredBimodal(8),
                    new cis501.submission.DirPredGshare(9, 5));
            btb = new cis501.submission.BranchTargetBuffer(10);
            BranchPredictor tourn = new BranchPredictor(dp, btb);
            String tournDesc = "tourn8i_NT:bimodal8i_T:gshare9i5h_btb10i";

            return Arrays.asList(new Object[][]{
                    {1, bimodal, bimodalDesc, 1.633E7},
                    {1, gshare, gshareDesc, 1.635E7},
                    {1, tourn, tournDesc, 1.642E7}});
        }

        //@Points(3)
        @Test
        public void testCyclesWithin5Perc() {
            assertEquals(MSG, expectedCycles, subm.getCycles(), 0.05 * expectedCycles);
        }

    }

}

