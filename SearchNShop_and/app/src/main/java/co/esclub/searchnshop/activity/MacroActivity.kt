package co.esclub.searchnshop.activity

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import co.esclub.searchnshop.R
import kotlinx.android.synthetic.main.activity_macro.*
import kotlinx.android.synthetic.main.add_macro.*

class MacroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_macro)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            createDialog()
        }
    }

    fun createDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.add_macro, null)
        val editQuery: EditText = view.findViewById(R.id.editQuery) as EditText
//        val binding: AddMacroBinding = DataBindingUtil.bind<AddMacroBinding>(view)

        AlertDialog.Builder(this)
                .setTitle(getString(R.string.new_item))
                .setMessage(getString(R.string.input_keyword_mall))
                .setView(view)
                .setPositiveButton(R.string.ok, { _: DialogInterface, _: Int ->
                    val str = editQuery.text.toString()
                    val queries = str.split(",")
                    for(query in queries) {
                        Log.d("###","text:" + query.trim())

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show()


    }
}
