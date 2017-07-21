package co.esclub.searchnshop.service

import android.content.Context
import co.esclub.searchnshop.model.firebase.FirebaseDBManager
import co.esclub.searchnshop.util.LogCat
import com.firebase.jobdispatcher.*

val TAG = "JOB"

class UpdateService : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        LogCat.d(TAG, "onStopJob")
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        LogCat.d(TAG, "onStartJob")
        FirebaseDBManager.checkForUpdate({
            params?.let {
                jobFinished(params, true)
            }
        })
        return true
    }
}

class UpdateServiceLauncher(val context: Context) {
    val JOB_TAG = "update-service"
    val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))

    fun run() {
        LogCat.d(TAG, "run update service...")
        val job = dispatcher.newJobBuilder().setService(UpdateService::class.java)
                .setTag(JOB_TAG)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(10, 30))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .setConstraints(Constraint.ON_ANY_NETWORK).build()
        dispatcher.mustSchedule(job)
    }

    fun cancel() {
        dispatcher.cancelAll()
    }
}