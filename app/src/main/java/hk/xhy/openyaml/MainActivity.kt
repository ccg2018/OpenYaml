package hk.xhy.openyaml

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import kotlinx.android.synthetic.main.content_main.*
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val READ_REQUEST_CODE = 42;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        btnOpenYaml.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if (v?.id == btnOpenYaml.id) {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/*"
            }, READ_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val uri = it.data?.apply {
                    editDocument(this)
                }
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setData(uri)
                })
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun editDocument(uri: Uri) {
        LogUtils.iTag("DEBUG", uri.toString())
        try {
            val read = contentResolver.openFileDescriptor(uri, "r")?.let {
                ParcelFileDescriptor.createPipe()
                val input = FileInputStream(it.fileDescriptor)
                val string = input.readBytes().toString(Charsets.UTF_8)
                input.close()
                string
            }
            LogUtils.iTag("DEBUG", read)
            contentResolver.openOutputStream(uri, "w")?.let {
                val sb = StringBuilder("# test\n").append(read)
                it.write(sb.toString().toByteArray())
                it.flush()
                it.close()
            }
        } catch (e: Exception) {
            LogUtils.eTag("DEBUG", e)
        }
    }
}