package jp.techacademy.thion.maikeru.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import io.realm.*
import java.util.*
import android.widget.Toast

const val EXTRA_TASK="jp.techacademy.thion.maikeru.taskapp.TASK"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            val intent=Intent(this,InputActivity::class.java)
            startActivity(intent)
        }

        // Realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this)


        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, view, position, id ->
            val task=parent.adapter.getItem(position) as Task
            val intent= Intent(this,InputActivity::class.java)
            intent.putExtra(EXTRA_TASK,task.id)
            startActivity(intent)
            // 入力・編集する画面に遷移させる
        }


        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText:String):Boolean{
                val searchRealmResults = mRealm.where(Task::class.java).equalTo("category",newText).findAll()
                if(searchRealmResults!=null){
                    mTaskAdapter.mTaskList = mRealm.copyFromRealm(searchRealmResults)

                    // TaskのListView用のアダプタに渡す
                    listView1.adapter = mTaskAdapter
                    mTaskAdapter.notifyDataSetChanged()}
                else{
                    Toast.makeText(this@MainActivity, "No Match found",Toast.LENGTH_LONG).show()
                }
                return false
            }
            override fun onQueryTextSubmit(query:String):Boolean{
                return false
            }
        })




        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, view, position, id ->
            val task =parent.adapter.getItem(position) as Task
            val builder=AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(task.title+"を削除しますか")

            builder.setPositiveButton("OK"){_,_->
                val results=mRealm.where(Task::class.java).equalTo("id",task.id).findAll()
                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent=Intent(applicationContext,TaskAlarmReceiver::class.java)
                val resultPendingIntent=PendingIntent.getBroadcast(
                    this,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager=getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }

            builder.setNegativeButton("CANCEL",null)
            val dialog=builder.create()
            dialog.show()
            // タスクを削除する
            true
        }
        reloadListView()
    }



    private fun reloadListView() {
        // Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        // 上記の結果を、TaskListとしてセットする
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }

}

/*private fun <E> RealmQuery<E>.equalTo(s: String, query: CharSequence?): Any {


}*/
