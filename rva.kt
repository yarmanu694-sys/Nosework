package com.example.my
import adapters.ResultAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.my.models.competitions
import models.ResultItem

class ResultsViewerActivity : AppCompatActivity() {

    private lateinit var listViewResults: ListView
    private lateinit var btnLoadResults: Button
    private lateinit var btnBackToChoice: Button

    private var competitionForResults: competitions? = null
    private val resultList = mutableListOf<ResultItem>()
    private lateinit var adapter: ResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arv)

        initViews()
        loadCompetitionConfig()
        setupClickListeners()
    }

    private fun initViews() {
        listViewResults = findViewById(R.id.list_view_results)
        btnLoadResults = findViewById(R.id.btn_load_results)
        btnBackToChoice = findViewById(R.id.btn_back_to_choice)

        adapter = ResultAdapter(this, resultList)
        listViewResults.adapter = adapter
    }

    private fun loadCompetitionConfig() {
        competitionForResults = json.loadCompetitionFromAssets("competition.json", this)
    }

    private fun setupClickListeners() {
        btnLoadResults.setOnClickListener { loadResultsFromFiles() }
        btnBackToChoice.setOnClickListener { finish() }
    }

    private fun loadResultsFromFiles() {
        resultList.clear()
        val filesDir = filesDir
        val files = filesDir.listFiles { dir, name -> name.startsWith("result_") && name.endsWith(".json") }

        if (files != null) {
            for (file in files) {
                val result = json.loadParticipantResultFromFile(file.absolutePath)
                if (result != null) {
                    val durationStr = if (result.finishTime != null && result.startTime != 0L) {
                        val durationMs = result.finishTime!! - result.startTime
                        val durationSecs = durationMs / 1000
                        String.format("%02d:%02d", durationSecs / 60, durationSecs % 60)
                    } else {
                        "N/A"
                    }

                    val foundMarkers = result.markerDetectionTimes.size
                    val totalMarkers = competitionForResults?.categories?.find { it.name == result.categoryName }?.totalMarkers ?: 0
                    val penaltyPoints = result.penalties.sumOf { it.appliedPoints }

                    val item = ResultItem(
                        result.participantId,
                        result.categoryName,
                        durationStr,
                        foundMarkers,
                        totalMarkers,
                        penaltyPoints
                    )
                    resultList.add(item)
                }
            }
        } else {
            Toast.makeText(this, "Не удалось получить список файлов.", Toast.LENGTH_SHORT).show()
        }

        adapter.notifyDataSetChanged()
    }
}