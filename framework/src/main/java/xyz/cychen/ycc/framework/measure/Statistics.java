package xyz.cychen.ycc.framework.measure;

public final class Statistics {
    public static String[] header = new String[]{
        "buildTime", "evalTime", "genTime"
    };

    private long buildTime;
    private long evalTime;
    private long genTime;
    private long overheadTime;

    protected Statistics(long buildTime, long evalTime, long genTime) {
        this.buildTime = buildTime;
        this.evalTime = evalTime;
        this.genTime = genTime;
        this.overheadTime = 0;
    }

    protected Statistics(long buildTime, long evalTime, long genTime, long overheadTime) {
        this.buildTime = buildTime;
        this.evalTime = evalTime;
        this.genTime = genTime;
        this.overheadTime = overheadTime;
    }

    public Statistics() {
        buildTime = 0;
        evalTime = 0;
        genTime = 0;
        overheadTime = 0;
    }

    public long getBuildTime() {
        return buildTime;
    }

    public long getEvalTime() {
        return evalTime;
    }

    public long getGenTime() {
        return genTime;
    }

    public void updateBuildTime(long inc) {
        buildTime += inc;
    }

    public void updateGenTime(long inc) {
        genTime += inc;
    }

    public void updateEvalTime(long inc) {
        evalTime += inc;
    }

    public void  updateOverheadTime(long inc) {
        overheadTime += inc;
    }

    public void update(Statistics another) {
        this.updateBuildTime(another.buildTime);
        this.updateEvalTime(another.evalTime);
        this.updateGenTime(another.genTime);
        this.updateOverheadTime(another.overheadTime);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(buildTime / 1000000).append(",");
        builder.append(evalTime / 1000000).append(",");
        builder.append(genTime / 1000000).append(",");
        return builder.toString();
    }
}
