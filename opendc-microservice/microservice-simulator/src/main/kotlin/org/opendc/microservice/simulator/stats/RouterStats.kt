package org.opendc.microservice.simulator.stats


public class RouterStats() {

    private val exeTimeStats: MutableList<Long> = mutableListOf()
    private val waitTimeStats: MutableList<Long> = mutableListOf()
    private val totalTimeStats: MutableList<Long> = mutableListOf()
    private val slowDownStats: MutableList<Long> = mutableListOf()
    private var slaViolations = 0


    public fun saveExeTime(value: Long){
        exeTimeStats.add(value)
    }

    public fun saveWaitTime(value: Long){
        waitTimeStats.add(value)
    }

    public fun saveTotalTime(value: Long){
        totalTimeStats.add(value)
    }

    public fun saveSlowDown(value: Long){
        slowDownStats.add(value)
    }

    public fun addSlaViolation(){
        slaViolations += 1
    }

    override fun toString(): String {
        return "Stats for Router per full request are: \n" +
            "SLA violations = $slaViolations \n" +
            "Execution times (${exeTimeStats.size}) : $exeTimeStats\n" +
            "Wait times (${waitTimeStats.size}): $waitTimeStats \n" +
            "Total times (${totalTimeStats.size}): $totalTimeStats. \n" +
            "Slow down (${slowDownStats.size}): $slowDownStats \n\n"
    }

}
