package com.aj.collection.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import com.aj.Constant.TASK_DES_EXTRA
import kotlinx.android.synthetic.main.activity_task_description.*
import com.aj.collection.R

class TaskDescriptionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_description)
        supportActionBar?.title = "查看任务详情"
        val des = intent.getStringExtra(TASK_DES_EXTRA)
        if (des!=null && des.isNotEmpty() && des.isNotBlank())
            task_description.text = des
        else
            task_description.text = "没有任务描述"
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            finish()
        }
        return super.onKeyUp(keyCode, event)
    }
}
