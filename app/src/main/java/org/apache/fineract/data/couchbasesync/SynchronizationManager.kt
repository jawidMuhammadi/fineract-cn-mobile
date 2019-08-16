package org.apache.fineract.data.couchbasesync

import android.util.Log
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseOptions
import com.couchbase.lite.Manager
import com.couchbase.lite.android.AndroidContext
import com.couchbase.lite.auth.BasicAuthenticator
import com.couchbase.lite.replicator.Replication
import com.google.gson.Gson
import org.apache.fineract.FineractApplication
import org.apache.fineract.data.local.PreferencesHelper
import org.apache.fineract.injection.ConfigPersistent
import org.apache.fineract.utils.ConstantKeys
import java.net.URL
import kotlin.collections.HashMap


/*
 * Created by saksham on 04/August/2019
*/
class SynchronizationManager : Replication.ChangeListener {

    //@Inject
    //lateinit var preferencesHelper : PreferencesHelper

    companion object {
        private const val SYNC_GATEWAY_ENDPOINT = "http://192.168.43.166:4984/fineract-cn/"
        private const val FINERACT_CN_LOCAL_SERVER_ENDPOINT = "http://localhost:8091/"
        private const val DATABASE_NAME = "fineract-cn" //is it bucket or not
        //private var TAG = SynchronizationManager.javaClass.kotlin.toString()
        private lateinit var preferencesHelper: PreferencesHelper
        private lateinit var manager: Manager
        private lateinit var database: Database
        private var syncManager = SynchronizationManager()

        @JvmStatic
        fun initialization() {
            manager = Manager(AndroidContext(FineractApplication.getContext()), Manager.DEFAULT_OPTIONS)
            var options = DatabaseOptions()
            options.isCreate = true
            preferencesHelper = PreferencesHelper(FineractApplication.getContext())
            database = manager.openDatabase(DATABASE_NAME, options)
            Log.d("TAG", "document count = " + database.documentCount.toString())
        }

        @JvmStatic
        fun getSynchronizationManager(): SynchronizationManager {
            return syncManager
        }

        /**
         *
         * Saves the Basic Authentication in SharedPreference
         *
         */
        @JvmStatic
        fun saveBasicAuthentication(username: String, password: String) {
            preferencesHelper.putString(ConstantKeys.BASIC_AUTH_KEY, Gson().toJson(BasicAuthenticator(username, password)))
        }

        @JvmStatic
        fun getBasicAuthentication(): BasicAuthenticator {
            return Gson().fromJson(preferencesHelper.getString(ConstantKeys.BASIC_AUTH_KEY, null), BasicAuthenticator::class.java)
        }
    }

    //call in LoginPresenter after successful login
    fun startPushAndPullReplicationForCurrentUser() {
        var syncUrl = URL(SYNC_GATEWAY_ENDPOINT)

        var pullReplication = database.createPullReplication(syncUrl)
        pullReplication.isContinuous = true
        Log.d("TAG", getBasicAuthentication().toString())
        pullReplication.authenticator = getBasicAuthentication()

        var pushReplication = database.createPushReplication(syncUrl)
        pushReplication.isContinuous = true
        Log.d("TAG", getBasicAuthentication().toString())
        pushReplication.authenticator = getBasicAuthentication()

        pullReplication.start()
        pushReplication.start()

        pullReplication.addChangeListener(this)
        pushReplication.addChangeListener(this)
    }

    /**
     * stops syncing after this method is called, but buffer if any syncing is left
     * and sync it when syncing process is started next time automatically
     *
     */
    fun closeSync() {
        manager.close()
    }

    override fun changed(event: Replication.ChangeEvent?) {
        var replication = event?.source
        if (!replication!!.isRunning) {

        } else {
            var processed = replication.completedChangesCount
            var total = replication.changesCount
            Log.d("TAG", String.format("Replicator processed %d / %d", processed, total))
        }
        Log.d("TAG", "change Count => " + event?.changeCount.toString())
        Log.d("TAG", "completed Change Count => " + event?.completedChangeCount.toString())
        Log.d("TAG", "source => " + event?.source.toString())
        Log.d("TAG", "transition => " + event?.transition.toString())

        if (event?.error != null) {
            Log.d("TAG", "Error -> " + event.error.toString() + "------------------")
        } else {
            Log.d("TAG", "no error" + database.documentCount)
        }
    }

    /**
     * pushes the document to the Couchbase Server
     *
     */
    @Throws(CouchbaseLiteException::class)
    fun <T> createDocument(properties: HashMap<String, T>) {
        database.createDocument().putProperties(properties as Map<String, T>)
        //throw CouchbaseLiteException(0)
    }
}