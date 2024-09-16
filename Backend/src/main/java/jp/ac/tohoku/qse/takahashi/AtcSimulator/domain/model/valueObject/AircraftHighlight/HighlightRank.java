package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftHighlight;

public class HighlightRank {
    final int highlightRank;

    public HighlightRank(int highlightRank) {
        if (highlightRank < 0 || highlightRank > 2) {
            throw new IllegalArgumentException("ハイライトランクは0~2です。");
        }
        this.highlightRank = highlightRank;
    }

    /**
     * ハイライトランクを更新する
     * @param newHighlightRank
     * @return
     */
    public HighlightRank changeHighlightRank(final int newHighlightRank) {
        return new HighlightRank(newHighlightRank);
    }

    @Override
    public String toString() {
        return String.valueOf(this.highlightRank);
    }
}
