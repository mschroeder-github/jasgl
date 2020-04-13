package com.github.mschroeder.github.jasgl.animator;

import com.github.mschroeder.github.jasgl.animator.FrameAnimator;
import java.util.Arrays;
import java.util.List;

/**
 * Animates a sequence of integers in a fixed time interval.
 * Use {@link #getFrame() } to access the current animated frame.
 * @author Markus Schr&ouml;der
 */
public class SequentialFixedIntervalFrameAnimator extends FrameAnimator {

    private int intervalMs;
    private int stopFrame;
    
    private List<Integer> sequence;
    
    private int index;
    
    public SequentialFixedIntervalFrameAnimator(int intervalMs, int stopFrame, List<Integer> sequence) {
        this.intervalMs = intervalMs;
        this.stopFrame = stopFrame;
        this.sequence = sequence;
        this.index = -1;
    }
    
    public SequentialFixedIntervalFrameAnimator(int intervalMs, int stopFrame, Integer... sequence) {
        this(intervalMs, stopFrame, Arrays.asList(sequence));
    }

    @Override
    public void stop() {
        super.stop();
        index = -1;
    }
    
    @Override
    public double next(boolean first) {
        index++;
        if(index >= sequence.size()) {
            index = 0;
        }
        return intervalMs;
    }

    @Override
    public int getFrame() {
        if(index == -1)
            return stopFrame;
        return sequence.get(index);
    }
    
}
