package com.liferlighdow.calculator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class MathNotepadActivity : AppCompatActivity() {

    private lateinit var rvNotebooks: RecyclerView
    private lateinit var llEmptyState: android.view.View
    private lateinit var adapter: NotebookAdapter
    private val notebooks = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math_notepad)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        llEmptyState = findViewById(R.id.llEmptyState)
        rvNotebooks = findViewById(R.id.rvNotebooks)
        rvNotebooks.layoutManager = LinearLayoutManager(this)
        adapter = NotebookAdapter(notebooks) { file ->
            val intent = Intent(this, MathNotepadEditorActivity::class.java)
            intent.putExtra("FILE_PATH", file.absolutePath)
            startActivity(intent)
        }
        rvNotebooks.adapter = adapter

        findViewById<ExtendedFloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showCreateDialog()
        }

        checkPermissionsAndLoad()
    }

    private fun checkPermissionsAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            } else {
                loadNotebooks()
            }
        } else {
            loadNotebooks()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadNotebooks()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadNotebooks() {
        val mathDir = getMathDir()
        if (!mathDir.exists()) {
            mathDir.mkdirs()
        }
        val files = mathDir.listFiles { _, name -> name.endsWith(".txt") }
        notebooks.clear()
        if (files != null) {
            notebooks.addAll(files.sortedByDescending { it.lastModified() })
        }
        adapter.notifyDataSetChanged()
        llEmptyState.visibility = if (notebooks.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun getMathDir(): File {
        // Try public Documents folder first, fallback to external files dir
        val publicDoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val mathDir = File(publicDoc, "Math")
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || Environment.isExternalStorageLegacy()) {
            mathDir
        } else {
            File(getExternalFilesDir(null), "Math")
        }
    }

    private fun showCreateDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_new_notebook, null)
        val input = view.findViewById<TextInputEditText>(R.id.etNotebookName)

        MaterialAlertDialogBuilder(this)
            .setView(view)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val file = File(getMathDir(), "$name.txt")
                    try {
                        if (!file.exists()) {
                            file.createNewFile()
                            loadNotebooks()
                            // Auto open after creation
                            val intent = Intent(this, MathNotepadEditorActivity::class.java)
                            intent.putExtra("FILE_PATH", file.absolutePath)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "File already exists", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Failed to create file: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
