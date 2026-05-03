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
        adapter = NotebookAdapter(notebooks, { file ->
            val intent = Intent(this, MathNotepadEditorActivity::class.java)
            intent.putExtra("FILE_PATH", file.absolutePath)
            startActivity(intent)
        }, { file ->
            showOptionsDialog(file)
        })
        rvNotebooks.adapter = adapter

        findViewById<ExtendedFloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showCreateDialog()
        }

        checkPermissionsAndLoad()
    }

    private fun checkPermissionsAndLoad() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ doesn't need READ/WRITE_EXTERNAL_STORAGE for app-specific or some public docs
            // But if the user wants to be asked anyway for the "feel" of permission or if using legacy paths:
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val needsPermission = permissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (needsPermission) {
                ActivityCompat.requestPermissions(this, permissions, 100)
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

    private fun showOptionsDialog(file: File) {
        val options = arrayOf("Rename", "Delete")
        MaterialAlertDialogBuilder(this)
            .setTitle(file.nameWithoutExtension)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenameDialog(file)
                    1 -> showDeleteConfirmDialog(file)
                }
            }
            .show()
    }

    private fun showRenameDialog(file: File) {
        val view = layoutInflater.inflate(R.layout.dialog_new_notebook, null)
        val input = view.findViewById<TextInputEditText>(R.id.etNotebookName)
        input.setText(file.nameWithoutExtension)

        MaterialAlertDialogBuilder(this)
            .setTitle("Rename Notebook")
            .setView(view)
            .setPositiveButton("Rename") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != file.nameWithoutExtension) {
                    val newFile = File(file.parentFile, "$newName.txt")
                    if (file.renameTo(newFile)) {
                        loadNotebooks()
                    } else {
                        Toast.makeText(this, "Rename failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmDialog(file: File) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Notebook")
            .setMessage("Are you sure you want to delete '${file.nameWithoutExtension}'?")
            .setPositiveButton("Delete") { _, _ ->
                if (file.delete()) {
                    loadNotebooks()
                } else {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
