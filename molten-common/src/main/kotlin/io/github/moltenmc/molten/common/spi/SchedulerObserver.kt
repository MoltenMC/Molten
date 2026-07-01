package io.github.moltenmc.molten.common.spi

import io.github.moltenmc.molten.common.scheduler.JobType
import java.time.Duration

interface SchedulerObserver {
    fun onJobCompleted(jobType: JobType, duration: Duration)

    fun onLongRunningJob(jobType: JobType, duration: Duration)
}
