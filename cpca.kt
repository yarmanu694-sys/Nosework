package com.example.my

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.my.models.competitions
import com.example.my.utils.Json

class cpca : AppCompatActivity() {

    private lateinit var etParticipantId: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnStartSession: Button
    private lateinit var btnViewResults: Button

    private var competition: competitions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_participant_category)

        initViews()
        loadCompetitionConfig()
        setupSpinner()
        setupClickListeners()
    }

    private fun initViews() {
        etParticipantId = findViewById(R.id.et_participant_id)
        spinnerCategory = findViewById(R.id.spinner_category)
        btnStartSession = findViewById(R.id.btn_start_session)
        btnViewResults = findViewById(R.id.btn_view_results)
    }

    private fun loadCompetitionConfig() {
        competition = Json.loadCompetitionFromAssets("competition.json", this)
        if (competition == null) {
            Toast.makeText(this, "Ошибка: не удалось загрузить конфигурацию!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupSpinner() {
        competition?.let { comp ->
            val categoryNames = comp.categories.map { it.name }.toTypedArray()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
        }
    }

    private fun setupClickListeners() {
        btnStartSession.setOnClickListener {
            val participantId = etParticipantId.text.toString().trim()
            val selectedCategory = spinnerCategory.selectedItem as? String

            if (participantId.isEmpty()) {
                Toast.makeText(this, "Введите ID участника!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedCategory == null) {
                Toast.makeText(this, "Выберите категорию!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CompetitionSessionActivity::class.java)
            intent.putExtra("PARTICIPANT_ID", participantId)
            intent.putExtra("CATEGORY_NAME", selectedCategory)
            startActivity(intent)
        }

        btnViewResults.setOnClickListener {
            val intent = Intent(this, ResultsViewerActivity::class.java)
            startActivity(intent)
        }
    }
}