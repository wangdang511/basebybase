package ca.virology.baseByBase.gui;

import ca.virology.lib.io.sequenceData.FeaturedSequence;

public class SequenceComboBoxItem {
    protected FeaturedSequence m_sequence;

    public SequenceComboBoxItem(FeaturedSequence seq) {
        m_sequence = seq;
    }

    public String toString() {
        return (m_sequence.getName());
    }

    public FeaturedSequence getSequence() {
        return (m_sequence);
    }
}
